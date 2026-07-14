package me.halfcooler.ic2r.core.block.invslot;

import me.halfcooler.ic2r.api.util.FluidContainerOutputMode;
import me.halfcooler.ic2r.core.block.IInventorySlotHolder;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.util.LiquidUtil;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.mutable.MutableObject;

public class InvSlotConsumableLiquid extends InvSlotConsumable
{
	private InvSlotConsumableLiquid.OpType opType;

	public InvSlotConsumableLiquid(IInventorySlotHolder<?> base1, String name1, int count)
	{
		this(base1, name1, InvSlot.Access.I, count, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain);
	}

	public InvSlotConsumableLiquid(
		IInventorySlotHolder<?> base1, String name1, InvSlot.Access access1, int count, InvSlot.InvSide preferredSide1, InvSlotConsumableLiquid.OpType opType1
	)
	{
		super(base1, name1, access1, count, preferredSide1);
		this.opType = opType1;
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return false;
		}

		if (!LiquidUtil.isFluidContainer(stack))
		{
			return false;
		}

		if (this.opType == InvSlotConsumableLiquid.OpType.Drain || this.opType == InvSlotConsumableLiquid.OpType.Both)
		{
			Ic2rFluidStack fs = Ic2rFluidStack.get(stack);
			if (fs != null && !fs.isEmpty() && this.acceptsLiquid(fs.getFluid()))
			{
				return true;
			}
		}

		return (this.opType == InvSlotConsumableLiquid.OpType.Fill || this.opType == InvSlotConsumableLiquid.OpType.Both)
			&& LiquidUtil.isFillableFluidContainer(stack, this.getPossibleFluids());
	}

	public Ic2rFluidStack drain(Fluid fluid, int maxAmount, MutableObject<ItemStack> output, boolean simulate)
	{
		output.setValue(null);
		if (fluid != null && !this.acceptsLiquid(fluid))
		{
			return null;
		}

		if (this.opType != InvSlotConsumableLiquid.OpType.Drain && this.opType != InvSlotConsumableLiquid.OpType.Both)
		{
			return null;
		}

		ItemStack stack = this.get();
		if (StackUtil.isEmpty(stack))
		{
			return null;
		}

		LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(stack, fluid, maxAmount, FluidContainerOutputMode.EmptyFullToOutput);
		if (result == null)
		{
			return null;
		}

		if (fluid == null && !this.acceptsLiquid(result.fluidChange.getFluid()))
		{
			return null;
		}

		output.setValue(result.extraOutput);
		if (!simulate)
		{
			this.put(result.inPlaceOutput);
		}

		return result.fluidChange;
	}

	public int fill(Ic2rFluidStack fs, MutableObject<ItemStack> output, boolean simulate)
	{
		output.setValue(null);
		if (fs == null || fs.isEmpty())
		{
			return 0;
		}

		if (this.opType != InvSlotConsumableLiquid.OpType.Fill && this.opType != InvSlotConsumableLiquid.OpType.Both)
		{
			return 0;
		}

		ItemStack stack = this.get();
		if (StackUtil.isEmpty(stack))
		{
			return 0;
		}

		LiquidUtil.FluidOperationResult result = LiquidUtil.fillContainer(stack, fs, FluidContainerOutputMode.EmptyFullToOutput);
		if (result == null)
		{
			return 0;
		}

		output.setValue(result.extraOutput);
		if (!simulate)
		{
			this.put(result.inPlaceOutput);
		}

		return result.fluidChange.getAmountMb();
	}

	public boolean transferToTank(Ic2rFluidTank tank, MutableObject<ItemStack> output, boolean simulate)
	{
		if (this.isEmpty())
		{
			return false;
		}

		int space = tank.getCapacity();
		Fluid fluidRequired = null;
		Ic2rFluidStack tankFluid = tank.getFluidStack();
		if (tankFluid != null)
		{
			space -= tankFluid.getAmountMb();
			fluidRequired = tankFluid.getFluid();
		}

		if (space <= 0)
		{
			return false;
		}

		ItemStack stack = this.get();
		LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainerComplete(stack, fluidRequired, space, FluidContainerOutputMode.EmptyFullToOutput);
		if (result == null)
		{
			return false;
		}

		int amount = tank.fillMb(result.fluidChange, simulate);
		if (amount <= 0 || amount != result.fluidChange.getAmountMb())
		{
			return false;
		}

		if (!simulate)
		{
			result = LiquidUtil.drainContainerComplete(stack, fluidRequired, space, FluidContainerOutputMode.EmptyFullToOutput);
			if (result == null)
			{
				return false;
			}

			output.setValue(result.extraOutput);
			this.put(result.inPlaceOutput);
		} else
		{
			output.setValue(result.extraOutput);
		}

		return true;
	}

	public boolean transferFromTank(Ic2rFluidTank tank, MutableObject<ItemStack> output, boolean simulate)
	{
		if (this.isEmpty() || tank.isEmpty())
		{
			return false;
		}

		Ic2rFluidStack tankFluid = tank.getFluidStack();
		if (tankFluid == null || tankFluid.isEmpty())
		{
			return false;
		}

		ItemStack stack = this.get();
		LiquidUtil.FluidOperationResult result = LiquidUtil.fillContainerComplete(stack, tankFluid.copy(), FluidContainerOutputMode.EmptyFullToOutput);
		if (result == null)
		{
			return false;
		}

		if (!simulate)
		{
			result = LiquidUtil.fillContainerComplete(stack, tankFluid.copy(), FluidContainerOutputMode.EmptyFullToOutput);
			if (result == null)
			{
				return false;
			}

			tank.drainMb(result.fluidChange.getAmountMb(), false);
			output.setValue(result.extraOutput);
			this.put(result.inPlaceOutput);
		} else
		{
			output.setValue(result.extraOutput);
		}

		return true;
	}

	public boolean processIntoTank(Ic2rFluidTank tank, InvSlotOutput outputSlot)
	{
		if (this.isEmpty())
		{
			return false;
		}

		MutableObject<ItemStack> output = new MutableObject<>();
		boolean wasChange = false;
		if (this.transferToTank(tank, output, true) && (StackUtil.isEmpty(output.getValue()) || outputSlot.canAdd(output.getValue())))
		{
			wasChange = this.transferToTank(tank, output, false);
			if (!StackUtil.isEmpty(output.getValue()))
			{
				outputSlot.add(output.getValue());
			}
		}

		return wasChange;
	}

	public boolean processFromTank(Ic2rFluidTank tank, InvSlotOutput outputSlot)
	{
		if (!this.isEmpty() && !tank.isEmpty())
		{
			MutableObject<ItemStack> output = new MutableObject<>();
			boolean wasChange = false;
			if (this.transferFromTank(tank, output, true) && (StackUtil.isEmpty(output.getValue()) || outputSlot.canAdd(output.getValue())))
			{
				wasChange = this.transferFromTank(tank, output, false);
				if (!StackUtil.isEmpty(output.getValue()))
				{
					outputSlot.add(output.getValue());
				}
			}

			return wasChange;
		} else
		{
			return false;
		}
	}

	public void setOpType(InvSlotConsumableLiquid.OpType opType1)
	{
		this.opType = opType1;
	}

	protected boolean acceptsLiquid(Fluid fluid)
	{
		return true;
	}

	protected Iterable<Fluid> getPossibleFluids()
	{
		return null;
	}

	public enum OpType
	{
		Drain,
		Fill,
		Both,
		None
	}
}
