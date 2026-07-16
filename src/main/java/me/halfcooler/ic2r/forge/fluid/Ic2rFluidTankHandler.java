package me.halfcooler.ic2r.forge.fluid;

import me.halfcooler.ic2r.core.fluid.FluidHandler;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public record Ic2rFluidTankHandler(Ic2rFluidTank tank) implements IFluidHandler
{
	public Ic2rFluidTankHandler
	{
		if (tank == null)
		{
			throw new NullPointerException("tank");
		}

	}


	@Override
	public int getTanks()
	{
		return 1;
	}

	@Override
	public @NotNull FluidStack getFluidInTank(int tank)
	{
		this.validateIndex(tank);
		return toForge(this.tank.getFluidStack());
	}

	@Override
	public int getTankCapacity(int tank)
	{
		this.validateIndex(tank);
		return this.tank.getCapacity();
	}

	@Override
	public boolean isFluidValid(int tank, @NotNull FluidStack stack)
	{
		this.validateIndex(tank);
		if (stack.isEmpty())
		{
			return false;
		}

		return this.tank.canFill(stack.getFluid());
	}

	@Override
	public int fill(FluidStack resource, FluidAction action)
	{
		if (resource == null || resource.isEmpty())
		{
			return 0;
		}

		Ic2rFluidStack offer = toDomain(resource);
		return this.tank.fillMb(offer, action.simulate());
	}

	@Override
	public @NotNull FluidStack drain(FluidStack resource, FluidAction action)
	{
		if (resource == null || resource.isEmpty())
		{
			return FluidStack.EMPTY;
		}

		int amount = this.tank.drainMb(toDomain(resource), action.simulate());
		if (amount <= 0)
		{
			return FluidStack.EMPTY;
		}

		FluidStack result = resource.copy();
		result.setAmount(amount);
		return result;
	}

	@Override
	public @NotNull FluidStack drain(int maxDrain, FluidAction action)
	{
		if (maxDrain <= 0)
		{
			return FluidStack.EMPTY;
		}

		return toForge(this.tank.drainMb(maxDrain, action.simulate()));
	}

	private void validateIndex(int tank)
	{
		if (tank != 0)
		{
			throw new IndexOutOfBoundsException("tank index " + tank + " / 1");
		}
	}

	public static Ic2rFluidStack toDomain(FluidStack resource)
	{
		if (resource == null || resource.isEmpty())
		{
			return Ic2rFluidStack.EMPTY;
		}

		CompoundTag tag = resource.getTag();
		return FluidHandler.createFluidStackMb(
			resource.getFluid(),
			resource.getAmount(),
			tag != null ? tag.copy() : null
		);
	}

	public static FluidStack toForge(Ic2rFluidStack fs)
	{
		if (fs == null || fs.isEmpty())
		{
			return FluidStack.EMPTY;
		}

		CompoundTag nbt = FluidHandler.getFluidStackNbt(fs);
		if (nbt == null)
		{
			return new FluidStack(fs.getFluid(), fs.getAmountMb());
		}

		return new FluidStack(fs.getFluid(), fs.getAmountMb(), nbt.copy());
	}
}
