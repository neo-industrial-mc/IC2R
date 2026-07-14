package me.halfcooler.ic2r.core.block.kineticgenerator.tileentity;

import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.Ic2rExplosion;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumable;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableItemStack;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityCondenser;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.LiquidUtil;
import me.halfcooler.ic2r.core.util.Util;

import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntitySteamKineticGenerator extends TileEntityAbstractKineticGenerator implements IHasGui, IUpgradableBlock, ServerTicker
{
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 1);
	public final InvSlotConsumable turbineSlot = new InvSlotConsumableItemStack(this, "Turbineslot", 1, new ItemStack(Ic2rItems.STEAM_TURBINE));
	protected final Ic2rFluidTank steamTank;
	protected final Ic2rFluidTank distilledWaterTank;
	protected final Fluids fluids;
	private int kuOutput;
	private boolean ventingSteam;
	private boolean throttled;
	private boolean isTurbineFilledWithWater = false;
	private int condensationProgress = 0;

	public TileEntitySteamKineticGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.STEAM_KINETIC_GENERATOR, pos, state);
		this.updateTicker = IC2R.random.nextInt(this.getTickRate());
		this.turbineSlot.setStackSizeLimit(1);
		this.fluids = this.addComponent(new Fluids(this));
		this.steamTank = this.fluids.addTankInsert("steamTank", 21000, Fluids.fluidPredicate(Ic2rFluids.STEAM.still(), Ic2rFluids.SUPERHEATED_STEAM.still()));
		this.distilledWaterTank = this.fluids
			.addTank("distilledWaterTank", 1000, Fluids.fluidPredicate(Ic2rFluids.DISTILLED_WATER.still(), net.minecraft.world.level.material.Fluids.WATER));
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.distilledWaterTank.getCapacity() - this.distilledWaterTank.getFluidAmount() >= 1 && this.isTurbineFilledWithWater)
		{
			this.isTurbineFilledWithWater = false;
		}

		if (this.steamTank.getFluidAmount() > 0 && !this.isTurbineFilledWithWater && !this.turbineSlot.isEmpty())
		{
			if (!this.getActive())
			{
				this.setActive(true);
				needsInvUpdate = true;
			}

			boolean hotSteam = this.steamTank.hasExactFluid(Ic2rFluids.SUPERHEATED_STEAM.still());
			boolean turbineDoneWork = this.turbineDoWork(hotSteam);
			if (this.updateTicker++ >= this.getTickRate())
			{
				if (turbineDoneWork)
				{
					this.turbineSlot.damage(hotSteam ? 1 : 2, false);
				}

				this.updateTicker = 0;
			}
		} else if (this.getActive())
		{
			this.setActive(false);
			needsInvUpdate = true;
			this.kuOutput = 0;
		}

		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		if (needsInvUpdate)
		{
			super.setChanged();
		}
	}

	private int handleSteam(boolean hotSteam)
	{
		int amount = this.steamTank.getFluidAmount();
		assert amount > 0;
		this.steamTank.drainMbUnchecked(amount, false);
		int KUWorkbuffer = amount * 2 * (hotSteam ? 2 : 1);
		if (hotSteam)
		{
			this.outputSteam(amount, true);
		} else
		{
			int condensation = amount / 10;
			this.condensationProgress += condensation;
			this.outputSteam(amount - condensation, false);
		}

		return KUWorkbuffer;
	}

	private boolean turbineDoWork(boolean hotSteam)
	{
		int rawOutput = this.handleSteam(hotSteam);
		int waterAmount = this.distilledWaterTank.getFluidAmount();
		float throttle;
		if (waterAmount == 0)
		{
			this.throttled = false;
			throttle = 1.0F;
		} else
		{
			this.throttled = true;
			throttle = 1.0F - (float) waterAmount / this.distilledWaterTank.getCapacity();
		}

		this.kuOutput = (int) (rawOutput * throttle * IC2RConfig.balance.energy.kineticGenerator.steam.get().floatValue());
		if (this.condensationProgress >= 100)
		{
			if (this.distilledWaterTank.fillMbUnchecked(Ic2rFluidStack.create(Ic2rFluids.DISTILLED_WATER.still(), 1), true) == 1)
			{
				this.condensationProgress -= 100;
				this.distilledWaterTank.fillMbUnchecked(Ic2rFluidStack.create(Ic2rFluids.DISTILLED_WATER.still(), 1), false);
			} else
			{
				this.isTurbineFilledWithWater = true;
			}
		}

		return this.kuOutput > 0;
	}

	private void outputSteam(int amount, boolean hotSteam)
	{
		Level world = this.getLevel();

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity te = world.getBlockEntity(this.worldPosition.relative(dir));
			if (te instanceof TileEntityCondenser || hotSteam && te instanceof TileEntitySteamKineticGenerator)
			{
				int transAmount = LiquidUtil.fillTile(te, dir.getOpposite(), Ic2rFluidStack.create(Ic2rFluids.STEAM.still(), amount), false);
				if (transAmount > 0)
				{
					amount -= transAmount;
					if (amount <= 0)
					{
						break;
					}
				}
			}
		}

		if (amount > 0)
		{
			this.ventingSteam = true;
			if (world.random.nextInt(10) == 0)
			{
				new Ic2rExplosion(world, null, this.worldPosition, 1, 1.0F, Ic2rExplosion.Type.Heat).doExplosion();
			}
		} else
		{
			this.ventingSteam = false;
		}
	}

	public int getKUoutput()
	{
		return this.kuOutput;
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.condensationProgress = nbt.getInt("condensationprogress");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("condensationprogress", this.condensationProgress);
	}

	@Override
	public ContainerBase<TileEntitySteamKineticGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerSteamKineticGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerSteamKineticGenerator(syncId, inventory, this);
	}

	@Override
	public int getConnectionBandwidth(Direction side)
	{
		return side == this.getFacing() ? this.kuOutput : 0;
	}

	@Override
	public int drawKineticEnergy(Direction side, int request, boolean simulate)
	{
		return side == this.getFacing() ? this.kuOutput : 0;
	}

	public int gaugeLiquidScaled(int i, int tank)
	{
		return tank == 0 && this.distilledWaterTank.getFluidAmount() > 0
			? this.distilledWaterTank.getFluidAmount() * i / this.distilledWaterTank.getCapacity()
			: 0;
	}

	@Override
	public double getEnergy()
	{
		return 0.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return false;
	}

	public int getDistilledWaterTankFill()
	{
		return this.distilledWaterTank.getFluidAmount();
	}

	public Ic2rFluidTank getDistilledWaterTank()
	{
		return this.distilledWaterTank;
	}

	public boolean hasTurbine()
	{
		return !this.turbineSlot.isEmpty();
	}

	public boolean isVentingSteam()
	{
		return this.ventingSteam;
	}

	public boolean isThrottled()
	{
		return this.throttled;
	}

	public boolean isTurbineBlockedByWater()
	{
		return this.isTurbineFilledWithWater;
	}

	public int getTickRate()
	{
		return 20;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
	}
}
