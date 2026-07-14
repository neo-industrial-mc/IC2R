package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquid;
import me.halfcooler.ic2r.core.block.invslot.InvSlotOutput;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.gui.dynamic.IGuiValueProvider;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.util.LiquidUtil;
import me.halfcooler.ic2r.core.util.PumpUtil;
import me.halfcooler.ic2r.core.util.Util;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPump extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IGuiValueProvider
{
	public final int defaultTier;
	public final int defaultEnergyStorage;
	public final int defaultEnergyConsume;
	public final int defaultOperationLength;
	public final InvSlotConsumableLiquid containerSlot;
	public final InvSlotOutput outputSlot;
	public final InvSlotUpgrade upgradeSlot;
	@GuiSynced
	protected final Ic2rFluidTank fluidTank;
	protected final Fluids fluids;
	public int energyConsume;
	public int operationsPerTick;
	public short progress = 0;
	public int operationLength;
	@GuiSynced
	public float guiProgress;
	private TileEntityMiner miner = null;
	private BlockPos cachedFluidSource = null;

	public TileEntityPump(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.PUMP, pos, state, 20, 1);
		this.containerSlot = new InvSlotConsumableLiquid(this, "input", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Fill);
		this.outputSlot = new InvSlotOutput(this, "output", 1, InvSlot.InvSide.SIDE);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
		this.defaultEnergyConsume = this.energyConsume = 1;
		this.defaultOperationLength = this.operationLength = 20;
		this.defaultTier = 1;
		this.defaultEnergyStorage = this.operationLength;
		this.fluids = this.addComponent(new Fluids(this));
		this.fluidTank = this.fluids.addTankExtract("fluid", 8000);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!Objects.requireNonNull(this.getLevel()).isClientSide)
		{
			this.setUpgradestat();
		}
	}

	@Override
	protected void onUnloaded()
	{
		this.miner = null;
		super.onUnloaded();
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.progress = nbt.getShort("progress");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putShort("progress", this.progress);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.cachedFluidSource = null;
		boolean needsInvUpdate = false;
		// Require only the per-tick cost while working (same as standard machines).
		// Requiring energyConsume * operationLength every tick is wrong: progress already paid
		// for earlier ticks, and under the GT packet model (e.g. LV = 32 EU) a 40 EU buffer
		// often sits at ~19 EU mid-cycle — enough for another tick but below the full 20 EU gate.
		if (this.canOperate())
		{
			if (this.progress < this.operationLength)
			{
				if (this.energy.useEnergy(this.energyConsume))
				{
					this.progress++;
					this.activate(false);
				} else
				{
					this.shutdown(false);
				}
			} else
			{
				// Operation energy was already spent while advancing progress.
				this.progress = 0;
				this.operate(false);
				this.activate(false);
			}
		} else
		{
			this.shutdown(false);
		}

		needsInvUpdate |= this.containerSlot.processFromTank(this.fluidTank, this.outputSlot);
		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		this.guiProgress = (float) this.progress / this.operationLength;
		if (needsInvUpdate)
		{
			super.setChanged();
		}
	}

	public boolean canOperate()
	{
		return this.operate(true);
	}

	public boolean operate(boolean sim)
	{
		if (this.miner == null || this.miner.isRemoved())
		{
			this.miner = null;
			Level world = this.getLevel();
			if (world == null)
			{
				return false;
			}

			for (Direction dir : Util.downSideFacings)
			{
				BlockEntity te = world.getBlockEntity(this.worldPosition.relative(dir));
				if (te instanceof TileEntityMiner)
				{
					this.miner = (TileEntityMiner) te;
					break;
				}
			}
		}

		Ic2rFluidStack liquid = null;
		if (this.miner != null)
		{
			if (this.miner.canProvideLiquid)
			{
				liquid = this.pump(this.miner.liquidPos, sim, this.miner);
			}
		} else
		{
			Direction dir = this.getFacing();
			liquid = this.pump(this.worldPosition.relative(dir), sim, this.miner);
		}

		if (liquid != null && this.fluidTank.fillMbUnchecked(liquid, true) > 0)
		{
			if (!sim)
			{
				this.fluidTank.fillMbUnchecked(liquid, false);
			}

			return true;
		} else
		{
			return false;
		}
	}

	public Ic2rFluidStack pump(BlockPos startPos, boolean sim, TileEntityMiner miner)
	{
		Level world = this.getLevel();
		if (world == null)
		{
			return null;
		}

		int freeSpace = this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount();
		if (miner == null && freeSpace > 0)
		{
			BlockEntity te = world.getBlockEntity(startPos);
			BlockState state = world.getBlockState(startPos);
			Direction side = this.getFacing().getOpposite();
			if (LiquidUtil.isFluidTile(state, te, side))
			{
				if (freeSpace > 1000)
				{
					freeSpace = 1000;
				}

				return LiquidUtil.drainTile(state, world, startPos, side, freeSpace, sim);
			}
		}

		if (freeSpace >= 1000)
		{
			BlockPos cPos;
			if (miner != null && miner.canProvideLiquid)
			{
				assert miner.liquidPos != null;
				cPos = miner.liquidPos;
			} else if (sim)
			{
				cPos = this.cachedFluidSource = PumpUtil.searchFluidSource(world, startPos, true);
			} else if (this.cachedFluidSource != null)
			{
				cPos = this.cachedFluidSource;
			} else
			{
				cPos = PumpUtil.searchFluidSource(world, startPos, false);
			}

			if (cPos != null)
			{
				return LiquidUtil.drainWorldFluidBlock(world, cPos, sim);
			}
		}

		return null;
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if (IC2R.sideProxy.isSimulating())
		{
			this.setUpgradestat();
		}
	}

	public void setUpgradestat()
	{
		int previousOperationLength = Math.max(1, this.operationLength);
		double previousProgress = (double) this.progress / previousOperationLength;
		this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(this.defaultOperationLength);
		this.operationLength = this.upgradeSlot.getOperationLength(this.defaultOperationLength);
		this.energyConsume = this.upgradeSlot.getEnergyDemand(this.defaultEnergyConsume);
		this.energy.setSinkTier(this.upgradeSlot.getTier(this.defaultTier));
		this.dischargeSlot.setTier(this.energy.getSinkTier());
		this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(this.defaultEnergyStorage, this.defaultOperationLength, this.defaultEnergyConsume));
		this.progress = (short) Math.floor(previousProgress * this.operationLength + 0.1);
		this.energy.syncConsumerProfile(this.energyConsume);
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

	@Override
	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2rSoundEvents.MACHINE_PUMP_OPERATE;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
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
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.Processing,
			UpgradableProperty.Transformer,
			UpgradableProperty.EnergyStorage,
			UpgradableProperty.ItemConsuming,
			UpgradableProperty.ItemProducing,
			UpgradableProperty.FluidProducing
		);
	}
}
