package me.halfcooler.ic2r.core.block.wiring.tileentity;

import me.halfcooler.ic2r.api.network.INetworkClientTileEntityEventListener;
import me.halfcooler.ic2r.api.tile.IEnergyStorage;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.comp.Redstone;
import me.halfcooler.ic2r.core.block.comp.RedstoneEmitter;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotCharge;
import me.halfcooler.ic2r.core.block.invslot.InvSlotDischarge;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntityBlock;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.block.wiring.ContainerElectricBlock;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.sync.BlockEntitySync;
import me.halfcooler.ic2r.core.network.sync.SyncCodecs;
import me.halfcooler.ic2r.core.network.sync.SyncKey;
import me.halfcooler.ic2r.core.energy.profile.ElectricalDisplay;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.LegacyNbt;
import me.halfcooler.ic2r.core.util.StackUtil;

import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityElectricBlock extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener, IEnergyStorage, ServerTicker
{
	public static byte redstoneModes = 7;

	/**
	 * Modern SyncKey for redstone mode (wire: {@code redstone_mode}).
	 * TeUpdate packets still carry {@link #LEGACY_REDSTONE_MODE_FIELD} (G1.1 / G1.5).
	 */
	public static final SyncKey<Byte> KEY_REDSTONE_MODE = SyncKey.of("redstone_mode", SyncCodecs.BYTE);
	/** Legacy TeUpdate / {@code getNetworkedFields()} field name for redstone mode. */
	public static final String LEGACY_REDSTONE_MODE_FIELD = "redstoneMode";
	/** Modern NBT key for redstone mode (G1.5). Writes use this only. */
	public static final String NBT_REDSTONE_MODE = "redstone_mode";
	/** Legacy camelCase NBT key; still readable via {@link LegacyNbt}. */
	public static final String LEGACY_NBT_REDSTONE_MODE = "redstoneMode";

	public final InvSlotCharge chargeSlot;
	public final InvSlotDischarge dischargeSlot;
	public final Energy energy;
	public final Redstone redstone;
	public final RedstoneEmitter rsEmitter;
	public byte redstoneMode = 0;
	/**
	 * @deprecated Use {@link me.halfcooler.ic2r.api.energy.profile.VoltageTier} × 1A ({@code getSourceTier()} voltage) for output EU/t.
	 */
	@Deprecated
	protected double output;

	public TileEntityElectricBlock(BlockEntityType<? extends TileEntityElectricBlock> type, BlockPos pos, BlockState state, int tier, int output, int maxStorage)
	{
		super(type, pos, state);
		this.output = output;
		this.chargeSlot = new InvSlotCharge(this, tier);
		this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.IO, tier, InvSlot.InvSide.BOTTOM);
		this.energy = this.addComponent(new Energy(this, maxStorage, EnumSet.complementOf(EnumSet.of(Direction.DOWN)), EnumSet.of(Direction.DOWN), tier, tier, true).addManagedSlot(this.chargeSlot).addManagedSlot(this.dischargeSlot));
		this.energy.configureStorageBlock();
		this.rsEmitter = this.addComponent(new RedstoneEmitter(this));
		this.redstone = this.addComponent(new Redstone(this));
		this.comparator.setUpdate(this.energy::getComparatorValue);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.energy.configureStorageBlock();
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.redstoneMode = readRedstoneModeNbt(nbt);
		this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing())), EnumSet.of(this.getFacing()));
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		writeRedstoneModeNbt(nbt, this.redstoneMode);
	}

	/** Pure NBT write (snake_case only). Unit-test entry (G1.5). */
	public static void writeRedstoneModeNbt(CompoundTag nbt, byte mode)
	{
		nbt.putByte(NBT_REDSTONE_MODE, mode);
	}

	/** Pure NBT read: prefer {@link #NBT_REDSTONE_MODE}, else legacy {@link #LEGACY_NBT_REDSTONE_MODE}. */
	public static byte readRedstoneModeNbt(CompoundTag nbt)
	{
		return LegacyNbt.getByte(nbt, NBT_REDSTONE_MODE, LEGACY_NBT_REDSTONE_MODE);
	}

	/**
	 * G1.5: registers modern SyncKeys for storage-block network fields.
	 * TeUpdate / writeFieldData resolve values via this table (legacy names aliased).
	 */
	@Override
	protected void registerSyncedData(BlockEntitySync sync)
	{
		super.registerSyncedData(sync);
		bindElectricBlockSync(sync, () -> this.redstoneMode, this::setRedstoneModeSynced);
	}

	/**
	 * Registers electric-block SyncKeys with TeUpdate legacy name aliases (G1.5).
	 * Shared by BE registration and pure unit tests.
	 */
	public static void bindElectricBlockSync(
		BlockEntitySync sync,
		Supplier<Byte> redstoneModeGetter,
		Consumer<Byte> redstoneModeSetter
	)
	{
		sync.add(KEY_REDSTONE_MODE, redstoneModeGetter, redstoneModeSetter, LEGACY_REDSTONE_MODE_FIELD);
	}

	/** Apply redstone mode from modern sync decode (no side effects). */
	protected void setRedstoneModeSynced(byte value)
	{
		this.redstoneMode = value;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.energy.setSendingEnabled(this.shouldEmitEnergy());
		this.rsEmitter.setLevel(this.shouldEmitRedstone() ? 15 : 0);
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerElectricBlock(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerElectricBlock(syncId, inventory, this);
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing())), EnumSet.of(this.getFacing()));
	}

	protected final void superSetFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
	}

	protected boolean shouldEmitRedstone()
	{
		return switch (this.redstoneMode)
		{
			case 1 -> this.energy.getEnergy() >= this.energy.getCapacity() - this.output * 20.0;
			case 2 ->
				this.energy.getEnergy() > this.output && this.energy.getEnergy() < this.energy.getCapacity() - this.output;
			case 3 -> this.energy.getEnergy() < this.energy.getCapacity() - this.output;
			case 4 -> this.energy.getEnergy() < this.output;
			default -> false;
		};
	}

	protected boolean shouldEmitEnergy()
	{
		boolean redstone = this.redstone.hasRedstoneInput();
		if (this.redstoneMode == 5)
		{
			return !redstone;
		} else
		{
			return this.redstoneMode != 6 || !redstone || this.energy.getEnergy() > this.energy.getCapacity() - this.output * 20.0;
		}
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		this.redstoneMode++;
		if (this.redstoneMode >= redstoneModes)
		{
			this.redstoneMode = 0;
		}

		IC2R.sideProxy.messagePlayer(player, this.getRedstoneMode());
	}

	public String getRedstoneMode()
	{
		return this.redstoneMode < redstoneModes && this.redstoneMode >= 0 ? Component.translatable("ic2r.EUStorage.gui.mod.redstone" + this.redstoneMode).getString() : "";
	}

	@Override
	public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing)
	{
		super.onPlaced(stack, placer, facing);
		if (!this.getLevel().isClientSide)
		{
			CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
			this.energy.addEnergy(nbt.getDouble("energy"));
		}
	}

	@Override
	public ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		drop = super.adjustDrop(drop, wrench);
		if (wrench || this.teBlock.getDefaultDrop() == Ic2rTileEntityBlock.DefaultDrop.Self)
		{
			double retainedRatio = IC2RConfig.balance.energyRetainedInStorageBlockDrops.get();
			double totalEnergy = this.energy.getEnergy();
			if (retainedRatio > 0.0 && totalEnergy > 0.0)
			{
				CompoundTag nbt = StackUtil.getOrCreateNbtData(drop);
				nbt.putDouble("energy", totalEnergy * retainedRatio);
			}
		}

		return drop;
	}

	@Override
	public int getOutput()
	{
		return (int) this.output;
	}

	@Override
	public double getOutputEnergyUnitsPerTick()
	{
		return this.output;
	}

	@Override
	public int addEnergy(int amount)
	{
		this.energy.addEnergy(amount);
		return amount;
	}

	@Override
	public int getStored()
	{
		return (int) this.energy.getEnergy();
	}

	@Override
	public void setStored(int energy)
	{
	}

	@Override
	public int getCapacity()
	{
		return (int) this.energy.getCapacity();
	}

	@Override
	public boolean isTeleporterCompatible(Direction side)
	{
		return true;
	}

	@Override
	public void appendItemTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag advanced)
	{
		Ic2rTooltip.add(tooltip, ElectricalDisplay.formatVoltage(this.energy.getWorkingVoltage()));
		Ic2rTooltip.add(tooltip, ElectricalDisplay.formatStorageOutput(this.energy));
		Ic2rTooltip.add(tooltip, Component.translatable("ic2r.item.tooltip.Capacity", this.getCapacity()));
		double stored = StackUtil.getOrCreateNbtData(stack).getDouble("energy");
		Ic2rTooltip.add(tooltip, Component.translatable("ic2r.item.tooltip.Store", (long) stored));
	}
}
