package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.network.INetworkTileEntityEventListener;
import me.halfcooler.ic2r.api.recipe.MachineRecipeResult;
import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.IUpgradeItem;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.invslot.InvSlotOutput;
import me.halfcooler.ic2r.core.block.invslot.InvSlotProcessable;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.gui.dynamic.IGuiValueProvider;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.network.sync.BlockEntitySync;
import me.halfcooler.ic2r.core.network.sync.SyncCodecs;
import me.halfcooler.ic2r.core.network.sync.SyncKey;
import me.halfcooler.ic2r.core.sound.Sound;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityStandardMachine<RI, RO, I>
	extends TileEntityElectricMachine
	implements IHasGui,
	IGuiValueProvider,
	INetworkTileEntityEventListener,
	IUpgradableBlock
{
	protected static final int EventStart = 0;
	protected static final int EventInterrupt = 1;
	protected static final int EventFinish = 2;
	protected static final int EventStop = 3;

	/**
	 * Modern SyncKey for GUI progress (logical wire name: {@code gui_progress}).
	 * TeUpdate packets still carry {@link #LEGACY_GUI_PROGRESS_FIELD} as the string name (G1.1 alias).
	 */
	public static final SyncKey<Float> KEY_GUI_PROGRESS = SyncKey.of("gui_progress", SyncCodecs.FLOAT);
	/**
	 * Modern SyncKey for machine active state (wire: {@code active}).
	 * TeUpdate field name is the same string ({@link #LEGACY_ACTIVE_FIELD}).
	 */
	public static final SyncKey<Boolean> KEY_ACTIVE = SyncKey.of("active", SyncCodecs.BOOLEAN);
	/**
	 * Legacy TeUpdate / {@code getNetworkedFields()} field name for progress.
	 * On-wire name stays camelCase for protocol compatibility; values R/W via Sync (G1.1).
	 */
	public static final String LEGACY_GUI_PROGRESS_FIELD = "guiProgress";
	/** Legacy TeUpdate / {@code getNetworkedFields()} field name for active (same as modern wire). */
	public static final String LEGACY_ACTIVE_FIELD = "active";

	/**
	 * World-save NBT key for process progress (W1.5).
	 * Already a single-segment lowercase / snake_case-legal name — retained; no camelCase legacy key.
	 * Network GUI fraction uses {@link #KEY_GUI_PROGRESS} ({@code gui_progress}), not this key.
	 */
	public static final String NBT_PROGRESS = "progress";

	public final int defaultEnergyConsume;
	public final int defaultOperationLength;
	public final int defaultTier;
	public final int defaultEnergyStorage;
	public final InvSlotOutput outputSlot;
	public final InvSlotUpgrade upgradeSlot;
	public int energyConsume;
	public int operationLength;
	public int operationsPerTick;
	public Sound sound;
	public InvSlotProcessable<RI, RO, I> inputSlot;
	protected Collection<ItemStack> processResult = null;
	protected MachineRecipeResult<RI, RO, I> recipeResult = null;
	protected short progress = 0;
	@GuiSynced
	protected float guiProgress;

	public TileEntityStandardMachine(
		BlockEntityType<? extends TileEntityStandardMachine<RI, RO, I>> type, BlockPos pos, BlockState state, int energyPerTick, int length, int outputSlots
	)
	{
		this(type, pos, state, energyPerTick, length, outputSlots, 1);
	}

	public TileEntityStandardMachine(
		BlockEntityType<? extends TileEntityStandardMachine<RI, RO, I>> type,
		BlockPos pos,
		BlockState state,
		int energyPerTick,
		int length,
		int outputSlots,
		int aDefaultTier
	)
	{
		super(type, pos, state, energyPerTick * length, aDefaultTier);
		this.defaultEnergyConsume = this.energyConsume = energyPerTick;
		this.defaultOperationLength = this.operationLength = length;
		this.defaultTier = aDefaultTier;
		this.defaultEnergyStorage = energyPerTick * length;
		this.outputSlot = new InvSlotOutput(this, "output", outputSlots);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
		this.comparator.setUpdate(() -> this.progress * 15 / this.operationLength);
		this.syncElectricalProfile(this.defaultEnergyConsume);
	}

	private static <RI> boolean isSameRecipeInput(RI a, RI b)
	{
		if (a == b)
		{
			return true;
		}

		if (a == null || b == null)
		{
			return false;
		}

		if (a instanceof ItemStack aStack && b instanceof ItemStack bStack)
		{
			return StackUtil.checkItemEqualityStrict(aStack, bStack);
		}

		return a.equals(b);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.progress = readProgressNbt(nbt);
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		writeProgressNbt(nbt, this.progress);
	}

	/**
	 * Pure NBT write for standard-machine progress (snake_case key {@link #NBT_PROGRESS} only).
	 * Unit-test entry (NS-003).
	 */
	public static void writeProgressNbt(CompoundTag nbt, short progress)
	{
		nbt.putShort(NBT_PROGRESS, progress);
	}

	/**
	 * Pure NBT read for standard-machine progress.
	 * Key {@link #NBT_PROGRESS} is already snake_case-legal; no legacy camelCase fallback.
	 */
	public static short readProgressNbt(CompoundTag nbt)
	{
		return nbt.getShort(NBT_PROGRESS);
	}

	public float getProgress()
	{
		return this.guiProgress;
	}

	/**
	 * G1.1: registers modern SyncKeys for standard-machine network fields.
	 * TeUpdate / writeFieldData resolve values via this table (legacy names aliased).
	 */
	@Override
	protected void registerSyncedData(BlockEntitySync sync)
	{
		super.registerSyncedData(sync);
		bindStandardMachineSync(
			sync,
			this::getActive,
			this::applySyncedActive,
			this::getProgress,
			this::setGuiProgressSynced
		);
	}

	/**
	 * Registers standard-machine SyncKeys with TeUpdate legacy name aliases (NS-005 / G1.1).
	 * Shared by BE registration and pure unit tests.
	 */
	public static void bindStandardMachineSync(
		BlockEntitySync sync,
		Supplier<Boolean> activeGetter,
		Consumer<Boolean> activeSetter,
		Supplier<Float> guiProgressGetter,
		Consumer<Float> guiProgressSetter
	)
	{
		// LEGACY_ACTIVE_FIELD equals wire "active" — alias is a no-op; declared for clarity.
		sync.add(KEY_ACTIVE, activeGetter, activeSetter, LEGACY_ACTIVE_FIELD);
		sync.add(KEY_GUI_PROGRESS, guiProgressGetter, guiProgressSetter, LEGACY_GUI_PROGRESS_FIELD);
	}

	/** Apply GUI progress from modern sync decode (no side effects). */
	protected void setGuiProgressSynced(float value)
	{
		this.guiProgress = value;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (IC2R.sideProxy.isSimulating())
		{
			this.setOverclockRates();
			this.recipeResult = this.getRecipeResult();
		}
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if (IC2R.sideProxy.isSimulating())
		{
			this.setOverclockRates();
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		MachineRecipeResult<RI, RO, I> result;
		if (this.inputSlot == null)
		{
			return;
		}
		if (this.recipeResult == null)
		{
			this.recipeResult = this.getRecipeResult();
		} else if ((result = this.inputSlot.process()) == null
			|| !isSameRecipeInput(result.recipe().getInput(), this.recipeResult.recipe().getInput()))
		{
			this.recipeResult = this.getRecipeResult();
			this.progress = 0;
		}

		if (this.canOperate())
		{
			if (!this.getActive())
			{
				this.activate(false);
			}

			this.progress++;
			if (this.progress >= this.operationLength)
			{
				this.operate();
				needsInvUpdate = true;
				this.progress = 0;
				if (!this.canOperate())
				{
					this.shutdown(false);
				}
			}
		} else
		{
			if (this.getActive())
			{
				this.shutdown(this.progress != 0);
			}

			if (this.recipeResult == null)
			{
				this.progress = 0;
			}
		}

		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		this.guiProgress = (float) this.progress / this.operationLength;
		if (needsInvUpdate)
		{
			super.setChanged();
		}
	}

	public void setOverclockRates()
	{
		this.upgradeSlot.onChanged();
		double previousProgress = (double) this.progress / this.operationLength;
		this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(this.defaultOperationLength);
		this.operationLength = this.upgradeSlot.getOperationLength(this.defaultOperationLength);
		this.energyConsume = this.upgradeSlot.getEnergyDemand(this.defaultEnergyConsume);
		int tier = this.upgradeSlot.getTier(this.defaultTier);
		this.energy.setSinkTier(tier);
		this.dischargeSlot.setTier(tier);
		this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(this.defaultEnergyStorage, this.defaultOperationLength, this.defaultEnergyConsume));
		this.progress = (short) Math.floor(previousProgress * this.operationLength + 0.1);
		this.energy.syncConsumerProfile(this.energyConsume * this.operationsPerTick);
	}

	private boolean canOperate()
	{
		return this.recipeResult != null && this.energy.useEnergy(this.energyConsume);
	}

	private void operate()
	{
		MachineRecipeResult<RI, RO, I> result = this.inputSlot.process();

		for (int i = 0; i < this.operationsPerTick; i++)
		{
			Collection<ItemStack> processRet = this.processResult == null ? this.getOutput(result.getOutput()) : this.processResult;

			for (int j = 0; j < this.upgradeSlot.size(); j++)
			{
				ItemStack stack = this.upgradeSlot.get(j);
				if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IUpgradeItem)
				{
					processRet = ((IUpgradeItem) stack.getItem()).onProcessEnd(stack, this, processRet);
				}
			}

			this.operateOnce(result, processRet);
			result = this.recipeResult = this.getRecipeResult();
			if (result == null)
			{
				break;
			}
		}
	}

	protected Collection<ItemStack> getOutput(RO output)
	{
		return StackUtil.copy((Collection<ItemStack>) output);
	}

	protected void operateOnce(MachineRecipeResult<RI, RO, I> result, Collection<ItemStack> processResult)
	{
		this.inputSlot.consume(result);
		this.outputSlot.add(processResult);
	}

	protected MachineRecipeResult<RI, RO, I> getRecipeResult()
	{
		if (this.inputSlot.isEmpty())
		{
			return null;
		} else
		{
			MachineRecipeResult<RI, RO, I> result = this.inputSlot.process();
			if (result == null)
			{
				return null;
			} else
			{
				Collection<ItemStack> processRet = this.getOutput(result.getOutput());
				if (this.outputSlot.canAdd(processRet))
				{
					this.processResult = processRet;
					return result;
				} else
				{
					return null;
				}
			}
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	@Override
	public void onNetworkEvent(int event)
	{
	}

	@Override
	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

	@Override
	public double getGuiValue(String name)
	{
		if (name.equals("progress"))
		{
			return this.guiProgress;
		} else
		{
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Cannot get value for " + name);
		}
	}
}
