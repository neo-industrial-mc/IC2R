package me.halfcooler.ic2r.core.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public class Ic2rFluidTank
{
	private int capacity;
	private Ic2rFluidStack fluidStack;
	/** Lazy Forge {@code IFluidHandler} view (G2.5); not a storage copy. */
	private Ic2rFluidTankHandler fluidHandler;

	public Ic2rFluidTank(int capacity)
	{
		this.capacity = capacity;
	}

	/**
	 * First-class Forge fluid handler over this tank (single tank index 0).
	 * Side masks / multi-tank aggregation remain on {@code Fluids} + BE capability.
	 *
	 * @see Ic2rFluidTankHandler
	 */
	public final Ic2rFluidTankHandler getFluidHandler()
	{
		if (this.fluidHandler == null)
		{
			this.fluidHandler = new Ic2rFluidTankHandler(this);
		}

		return this.fluidHandler;
	}

	public final int getCapacity()
	{
		return this.capacity;
	}

	public final void setCapacity(int capacity)
	{
		this.capacity = capacity;
	}

	public final Ic2rFluidStack getFluidStack()
	{
		return this.fluidStack;
	}

	public final void setFluidStack(Ic2rFluidStack fs)
	{
		this.fluidStack = fs;
	}

	public final boolean hasExactFluid(Fluid fluid)
	{
		return this.fluidStack != null && this.fluidStack.hasExactFluid(fluid);
	}

	public final boolean isEmpty()
	{
		return this.fluidStack == null || this.fluidStack.isEmpty();
	}

	public final int getFluidAmount()
	{
		return this.fluidStack != null ? this.fluidStack.getAmountMb() : 0;
	}

	public final void fromNbt(CompoundTag nbt)
	{
		if (nbt.contains("Empty"))
		{
			this.fluidStack = null;
		} else
		{
			this.fluidStack = Ic2rFluidStack.read(nbt);
		}
	}

	public final CompoundTag toNbt(CompoundTag nbt)
	{
		if (this.fluidStack != null && !this.fluidStack.isEmpty())
		{
			this.fluidStack.toNbt(nbt);
		} else
		{
			nbt.putString("Empty", "");
		}

		return nbt;
	}

	public final Ic2rFluidStack drainMb(int amount, boolean simulate)
	{
		return this.drainMb(amount, simulate, true);
	}

	public final Ic2rFluidStack drainMbUnchecked(int amount, boolean simulate)
	{
		return this.drainMb(amount, simulate, false);
	}

	private Ic2rFluidStack drainMb(int amount, boolean simulate, boolean external)
	{
		int drained = FluidTransferMath.drainMbDelegated(
			this.getFluidAmount(),
			amount,
			external,
			this.canDrain()
		);
		if (drained <= 0 || this.fluidStack == null || this.fluidStack.isEmpty())
		{
			return Ic2rFluidStack.EMPTY;
		}

		Ic2rFluidStack ret;
		if (drained >= this.fluidStack.getAmountMb())
		{
			if (simulate)
			{
				ret = this.fluidStack.copy();
			} else
			{
				ret = this.fluidStack;
				this.fluidStack = null;
			}
		} else
		{
			ret = this.fluidStack.copyWithAmountMb(drained);
			if (!simulate)
			{
				this.fluidStack.decreaseMb(drained);
			}
		}

		return ret;
	}

	public final int drainMb(Ic2rFluidStack toDrain, boolean simulate)
	{
		return this.drainMb(toDrain, simulate, true);
	}

	public final int drainMbUnchecked(Ic2rFluidStack toDrain, boolean simulate)
	{
		return this.drainMb(toDrain, simulate, false);
	}

	private int drainMb(Ic2rFluidStack toDrain, boolean simulate, boolean external)
	{
		boolean tankEmpty = this.fluidStack == null || this.fluidStack.isEmpty();
		boolean sameFluid = !tankEmpty && !toDrain.isEmpty() && this.fluidStack.hasExactFluid(toDrain);
		int ret = FluidTransferMath.drainMbByStackDelegated(
			this.getFluidAmount(),
			toDrain.isEmpty() ? 0 : toDrain.getAmountMb(),
			tankEmpty,
			toDrain.isEmpty(),
			sameFluid,
			external,
			this.canDrain()
		);
		if (ret <= 0)
		{
			return 0;
		}

		if (!simulate)
		{
			if (ret >= this.fluidStack.getAmountMb())
			{
				this.fluidStack = null;
			} else
			{
				this.fluidStack.decreaseMb(ret);
			}
		}

		return ret;
	}

	public final int fillMb(Ic2rFluidStack toFill, boolean simulate)
	{
		return this.fillMb(toFill, simulate, true);
	}

	public final int fillMbUnchecked(Ic2rFluidStack toFill, boolean simulate)
	{
		return this.fillMb(toFill, simulate, false);
	}

	private int fillMb(Ic2rFluidStack toFill, boolean simulate, boolean external)
	{
		boolean tankEmpty = this.fluidStack == null || this.fluidStack.isEmpty();
		boolean sameFluid = !tankEmpty && !toFill.isEmpty() && this.fluidStack.hasExactFluid(toFill);
		boolean canFillFluid = !toFill.isEmpty() && this.canFill(toFill.getFluid());
		int ret = FluidTransferMath.fillMbDelegated(
			this.capacity,
			this.getFluidAmount(),
			toFill.isEmpty() ? 0 : toFill.getAmountMb(),
			tankEmpty,
			sameFluid,
			toFill.isEmpty(),
			external,
			canFillFluid
		);
		if (ret <= 0)
		{
			return 0;
		}

		if (!simulate)
		{
			if (this.fluidStack != null && !this.fluidStack.isEmpty())
			{
				this.fluidStack.increaseMb(ret);
			} else
			{
				this.fluidStack = toFill.copyWithAmountMb(ret);
			}
		}

		return ret;
	}

	protected boolean canDrain()
	{
		return true;
	}

	protected boolean canFill(Fluid fluid)
	{
		return true;
	}
}
