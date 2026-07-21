package me.halfcooler.ic2r.core.block.wiring.tileentity;

import net.minecraft.network.chat.Component;
import org.joml.Vector3f;
import me.halfcooler.ic2r.api.energy.profile.VoltageTier;
import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.wiring.ContainerChargepadBlock;
import me.halfcooler.ic2r.core.energy.EnergyNetMode;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.Util;

import me.halfcooler.ic2r.core.block.tileentity.ClientTicker;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.HolderLookup;

public abstract class TileEntityChargePadBlock extends TileEntityElectricBlock implements ClientTicker
{
	private static final List<AABB> aabbs = List.of(new AABB(0.0, 0.0, 0.0, 1.0, 0.9375, 1.0));
	private static final DustParticleOptions effect = new DustParticleOptions(new Vector3f(0.2F, 0.2F, 1.0F), 1.0F);
	public static byte redstoneModes = 2;
	private int updateTicker;
	private Player player = null;

	public TileEntityChargePadBlock(BlockEntityType<? extends TileEntityChargePadBlock> type, BlockPos pos, BlockState state, int tier, int output, int maxStorage)
	{
		super(type, pos, state, tier, output, maxStorage);
		this.energy.setDirections(EnumSet.complementOf(EnumSet.copyOf(Util.verticalFacings)), EnumSet.of(Direction.DOWN));
		this.updateTicker = IC2R.random.nextInt(this.getTickRate());
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing(), Direction.UP)), EnumSet.of(this.getFacing()));
	}

	@Override
	protected List<AABB> getAabbs(boolean forCollision)
	{
		return aabbs;
	}

	@Override
	protected void onEntityCollision(Entity entity)
	{
		super.onEntityCollision(entity);
		if (!this.getLevel().isClientSide && entity instanceof Player)
		{
			this.updatePlayer((Player) entity);
		}
	}

	private void updatePlayer(Player entity)
	{
		this.player = entity;
	}

	protected int getTickRate()
	{
		return 2;
	}

	@Override
	protected void updateEntityServer()
	{
		boolean needsInvUpdate = false;
		if (this.updateTicker++ % this.getTickRate() == 0)
		{
			if (this.player != null && this.energy.getEnergy() >= 1.0)
			{
				boolean charged = this.getItems(this.player);
				this.player = null;
				if (charged != this.getActive())
				{
					this.setActive(charged);
					needsInvUpdate = true;
				} else if (charged)
				{
					needsInvUpdate = true;
				}
			} else if (this.getActive())
			{
				this.setActive(false);
				needsInvUpdate = true;
			}
		}

		super.updateEntityServer();

		if (needsInvUpdate)
		{
			this.setChanged();
		}
	}

	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		Level world = this.getLevel();
		RandomSource rnd = world.random;
		if (rnd.nextInt(8) == 0)
		{
			if (this.getActive())
			{
				for (int particles = 20; particles > 0; particles--)
				{
					double x = this.worldPosition.getX() + 0.0F + rnd.nextFloat();
					double y = this.worldPosition.getY() + 0.9F + rnd.nextFloat();
					double z = this.worldPosition.getZ() + 0.0F + rnd.nextFloat();
					world.addParticle(effect, x, y, z, 0.0, 0.1, 0.0);
				}
			}
		}
	}

	/**
	 * Charge every eligible electric item on the player at once.
	 * Eligible = chargeable and item tier ≤ chargepad source tier.
	 * Each item is charged at the pad's full output power (shared energy pool).
	 * Scope: offhand, armor, full main inventory (hotbar + backpack; includes main hand).
	 */
	protected boolean getItems(Player player)
	{
		int chargeFactor = (int) this.output;
		boolean charged = false;

		ItemStack stack = player.getOffhandItem();
		if (!stack.isEmpty() && this.chargeItem(stack, chargeFactor))
		{
			charged = true;
		}

		for (int i = player.getInventory().armor.size() - 1; i >= 0; i--)
		{
			stack = player.getInventory().armor.get(i);
			if (!stack.isEmpty() && this.chargeItem(stack, chargeFactor))
			{
				charged = true;
			}
		}

		// items covers hotbar + main inventory (main hand is selected hotbar slot)
		for (int i = 0; i < player.getInventory().items.size(); i++)
		{
			stack = player.getInventory().items.get(i);
			if (!stack.isEmpty() && this.chargeItem(stack, chargeFactor))
			{
				charged = true;
			}
		}

		return charged;
	}

	@Override
	protected boolean shouldEmitRedstone()
	{
		return (this.redstoneMode == 0 && this.getActive()) || (this.redstoneMode == 1 && !this.getActive());
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		this.energy.setDirections(EnumSet.complementOf(EnumSet.of(facing, Direction.UP)), EnumSet.of(facing));
		this.superSetFacing(world, facing);
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerChargepadBlock(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerChargepadBlock(syncId, inventory, this);
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

	@Override
	public String getRedstoneMode()
	{
		return this.redstoneMode <= 1 && this.redstoneMode >= 0 ? Component.translatable("ic2r.blockChargepad.gui.mod.redstone" + this.redstoneMode).getString() : "";
	}

	/**
	 * Charge a single stack at the pad power rate, limited by pad tier.
	 * In GT energy-net mode, transferred EU is quantized to integer amps
	 * (multiples of the pad working voltage).
	 */
	protected boolean chargeItem(ItemStack stack, int chargeFactor)
	{
		if (stack.getItem() == Ic2rItems.DEBUG_ITEM)
		{
			return false;
		}

		int tier = this.energy.getSourceTier();
		// tier gate: only items with voltage tier ≤ pad tier accept charge
		double freeAmount = ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, tier, true, true);
		if (freeAmount <= 0.0)
		{
			return false;
		}

		double charge = Math.min(freeAmount, (double) chargeFactor * this.getTickRate());
		charge = Math.min(charge, this.energy.getEnergy());

		if (EnergyNetMode.fromConfig(IC2RConfig.misc.useGregTechEnergyNet.get()) == EnergyNetMode.GT)
		{
			int voltage = VoltageTier.fromIcTier(tier).getVoltage();
			if (voltage > 0)
			{
				charge = Math.floor(charge / voltage) * voltage;
			}

			if (charge <= 0.0)
			{
				return false;
			}
		}

		double transferred = ElectricItem.manager.charge(stack, charge, tier, true, false);
		if (transferred > 0.0)
		{
			this.energy.useEnergy(transferred);
			return true;
		}

		return false;
	}
}
