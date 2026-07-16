package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.network.INetworkClientTileEntityEventListener;
import me.halfcooler.ic2r.api.recipe.IEmptyFluidContainerRecipeManager;
import me.halfcooler.ic2r.api.recipe.MachineRecipeResult;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.api.util.FluidContainerOutputMode;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableCanner;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquid;
import me.halfcooler.ic2r.core.block.invslot.InvSlotProcessableCanner;
import me.halfcooler.ic2r.core.block.machine.container.ContainerCanner;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.util.LiquidUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;

public class TileEntityCanner extends TileEntityStandardMachine<Object, Object, Object> implements INetworkClientTileEntityEventListener
{
	public static final int eventSwapTanks = Mode.values.length + 1;
	public final Ic2rFluidTank inputTank;
	public final Ic2rFluidTank outputTank;
	public final InvSlotConsumableCanner canInputSlot;
	protected final Fluids fluids;
	private TileEntityCanner.Mode mode = TileEntityCanner.Mode.BottleSolid;
	/** Last mode for which side-effects (slot op type / sounds) were applied. Used so GUI field resyncs do not stop work sounds. */
	private TileEntityCanner.Mode appliedMode;

	public TileEntityCanner(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.CANNER, pos, state, 4, 200, 1);
		this.inputSlot = new InvSlotProcessableCanner(this, "input", 1);
		this.canInputSlot = new InvSlotConsumableCanner(this, "canInput", 1);
		this.fluids = this.addComponent(new Fluids(this));
		this.inputTank = this.fluids.addTankInsert("inputTank", 8000);
		this.outputTank = this.fluids.addTankExtract("outputTank", 8000);
		// Default mode is BottleSolid; InvSlotConsumableLiquid defaults to Drain.
		this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.None);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.setMode(TileEntityCanner.Mode.values[nbt.getInt("mode")]);
	}

	@Override
	public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.saveAdditional(nbt, registries);
		nbt.putInt("mode", this.mode.ordinal());
	}

	@Override
	public void operateOnce(MachineRecipeResult<Object, Object, Object> result, Collection<ItemStack> processResult)
	{
		super.operateOnce(result, processResult);
		if (this.mode == TileEntityCanner.Mode.EmptyLiquid)
		{
			IEmptyFluidContainerRecipeManager.Output output = (IEmptyFluidContainerRecipeManager.Output) result.getOutput();
			this.getOutputTank().fillMbUnchecked(output.fluid(), false);
		} else if (this.mode == TileEntityCanner.Mode.EnrichLiquid)
		{
			Ic2rFluidStack output = ((Ic2rFluidStack) result.getOutput()).copy();
			LiquidUtil.FluidOperationResult outcome;
			if (!this.canInputSlot.isEmpty())
			{
				do
				{
					outcome = LiquidUtil.fillContainer(this.canInputSlot.get(), output, FluidContainerOutputMode.EmptyFullToOutput);
					if (outcome != null)
					{
						if (outcome.extraOutput != null && !this.outputSlot.canAdd(outcome.extraOutput))
						{
							outcome = null;
						} else
						{
							this.canInputSlot.put(outcome.inPlaceOutput);
							if (outcome.extraOutput != null)
							{
								this.outputSlot.add(outcome.extraOutput);
							}

							output.setAmountMb(output.getAmountMb() - outcome.fluidChange.getAmountMb());
						}
					}
				} while (outcome != null && !output.isEmpty());
			}

			this.getOutputTank().fillMbUnchecked(output, false);
		}
	}

	@Override
	protected Collection<ItemStack> getOutput(Object output)
	{
		if (output instanceof ItemStack)
		{
			return Collections.singletonList((ItemStack) output);
		} else if (output instanceof Ic2rFluidStack)
		{
			return Collections.emptyList();
		} else
		{
			return output instanceof IEmptyFluidContainerRecipeManager.Output ? ((IEmptyFluidContainerRecipeManager.Output) output).container() : super.getOutput(output);
		}
	}

	@Override
	public MachineRecipeResult<Object, Object, Object> getRecipeResult()
	{
		if (this.mode != TileEntityCanner.Mode.EmptyLiquid && this.mode != TileEntityCanner.Mode.BottleLiquid)
		{
			if (this.inputSlot.isEmpty())
			{
				return null;
			}
		} else if (this.canInputSlot.isEmpty())
		{
			return null;
		}

		MachineRecipeResult<Object, Object, Object> result = this.inputSlot.process();
		if (result == null)
		{
			return null;
		}

		if (!this.outputSlot.canAdd(this.getOutput(result.getOutput())))
		{
			return null;
		}

		if (this.mode == TileEntityCanner.Mode.EmptyLiquid)
		{
			IEmptyFluidContainerRecipeManager.Output output = (IEmptyFluidContainerRecipeManager.Output) result.getOutput();
			if (this.getOutputTank().fillMbUnchecked(output.fluid(), true) != output.fluid().getAmountMb())
			{
				return null;
			}
		} else if (this.mode == TileEntityCanner.Mode.EnrichLiquid)
		{
			Ic2rFluidStack output = ((Ic2rFluidStack) result.getOutput()).copy();
			LiquidUtil.FluidOperationResult outcome;
			if (!this.canInputSlot.isEmpty())
			{
				do
				{
					outcome = LiquidUtil.fillContainer(this.canInputSlot.get(), output, FluidContainerOutputMode.EmptyFullToOutput);
					if (outcome != null)
					{
						if (outcome.extraOutput != null && !this.outputSlot.canAdd(outcome.extraOutput))
						{
							outcome = null;
						} else
						{
							output.setAmountMb(output.getAmountMb() - outcome.fluidChange.getAmountMb());
						}
					}
				} while (outcome != null && !output.isEmpty());
			}

			if (this.getOutputTank().fillMbUnchecked(output, true) != output.getAmountMb())
			{
				return null;
			}
		}

		return result;
	}

	public Ic2rFluidTank getInputTank()
	{
		return this.inputTank;
	}

	public Ic2rFluidTank getOutputTank()
	{
		return this.outputTank;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = new ArrayList<>();
		ret.add("canInputSlot");
		ret.addAll(super.getNetworkedFields());
		return ret;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return switch (this.mode)
		{
			case BottleSolid, BottleLiquid -> Ic2rSoundEvents.MACHINE_CANNER_OPERATE.get();
			case EmptyLiquid -> Ic2rSoundEvents.MACHINE_CANNER_REVERSE.get();
			default -> null;
		};
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return switch (this.mode)
		{
			case BottleSolid, BottleLiquid, EmptyLiquid -> Ic2rSoundEvents.MACHINE_INTERRUPT1.get();
			default -> null;
		};
	}

	@Override
	public ContainerBase<TileEntityCanner> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerCanner(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerCanner(syncId, inventory, this);
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		super.onNetworkUpdate(field);
		if (field.equals("mode"))
		{
			this.setMode(this.mode);
		}
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event >= 0 && event < Mode.values.length)
		{
			this.setMode(TileEntityCanner.Mode.values[event]);
		} else if (event == eventSwapTanks)
		{
			this.switchTanks();
		}
	}

	public TileEntityCanner.Mode getMode()
	{
		return this.mode;
	}

	public void setMode(TileEntityCanner.Mode mode)
	{
		// GUI open resyncs "mode" every tick via ContainerBase.broadcastChanges → onNetworkUpdate.
		// Reflection already wrote this.mode before that callback, so compare against appliedMode
		// rather than the previous this.mode value.
		boolean modeChanged = this.appliedMode != mode;
		this.mode = mode;
		this.appliedMode = mode;
		switch (mode)
		{
			case BottleSolid:
				this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.None);
				break;
			case BottleLiquid:
				this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.Fill);
				break;
			case EmptyLiquid:
				this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.Drain);
				break;
			case EnrichLiquid:
				this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.Both);
		}

		// Looping/interrupt sounds depend on mode (operate vs reverse vs none). Only rebuild when
		// the mode actually changes — never on repeated GUI field sync of the same mode.
		if (modeChanged && IC2R.sideProxy.isRendering())
		{
			this.refreshModeDependentSounds();
		}
	}

	/**
	 * Rebuild mode-dependent sound instances and resume looping if the machine is still active.
	 */
	private void refreshModeDependentSounds()
	{
		if (this.loopingSound != null)
		{
			IC2R.soundManager.removeSound(this, this.loopingSound);
			this.loopingSound = null;
		}

		if (this.interruptSound != null)
		{
			IC2R.soundManager.removeSound(this, this.interruptSound);
			this.interruptSound = null;
		}

		this.initSound();
		if (this.shouldSoundActive() && this.loopingSound != null)
		{
			this.playLoopingSound(false);
		}
	}

	private void switchTanks()
	{
		if (this.progress != 0)
		{
			return;
		}

		Ic2rFluidStack inputStack = this.inputTank.getFluidStack();
		Ic2rFluidStack outputStack = this.outputTank.getFluidStack();
		this.inputTank.setFluidStack(outputStack);
		this.outputTank.setFluidStack(inputStack);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
	}

	public enum Mode
	{
		BottleSolid, EmptyLiquid, BottleLiquid, EnrichLiquid;

		public static final TileEntityCanner.Mode[] values = values();
	}
}
