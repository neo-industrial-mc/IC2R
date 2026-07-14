package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

record Ic2rFluidStackImpl(FluidStack parent) implements Ic2rFluidStack
{
	Ic2rFluidStackImpl
	{
		if (parent == null)
		{
			throw new NullPointerException();
		}

	}

	@Override
	public Ic2rFluidStack copy()
	{
		return this.parent.getTag() == null ? Ic2rFluidStack.create(this.parent.getFluid(), this.parent.getAmount()) : new Ic2rFluidStackImpl(this.parent.copy());
	}

	@Override
	public Fluid getFluid()
	{
		return this.parent.getFluid();
	}

	@Override
	public boolean hasExactFluid(Fluid fluid)
	{
		return this.parent.getTag() == null && fluid == this.parent.getFluid();
	}

	@Override
	public boolean hasExactFluid(Ic2rFluidStack fs)
	{
		return fs instanceof Ic2rFluidStackImpl ? this.parent.isFluidEqual(((Ic2rFluidStackImpl) fs).parent) : this.hasExactFluid(fs.getFluid());
	}

	@Override
	public int getAmountMb()
	{
		return this.parent.getAmount();
	}

	@Override
	public void setAmountMb(int amount)
	{
		if (amount < 0)
		{
			throw new IllegalArgumentException();
		}

		this.parent.setAmount(amount);
	}

	@Override
	public void toNbt(CompoundTag nbt)
	{
		this.parent.writeToNBT(nbt);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Ic2rFluidStack)
		{
			return false;
		} else
		{
			Ic2rFluidStack o = (Ic2rFluidStack) obj;
			if (this.parent.getAmount() != o.getAmountMb() || this.parent.getFluid() != o.getFluid())
			{
				return false;
			} else
			{
				return o instanceof Ic2rFluidStackImpl ? Objects.equals(this.parent.getTag(), ((Ic2rFluidStackImpl) o).parent.getTag()) : this.parent.getTag() == null;
			}
		}
	}

	@Override
	public int hashCode()
	{
		return this.parent.getFluid().hashCode() ^ this.parent.getAmount();
	}

	@Override
	public @NotNull String toString()
	{
		Fluid fluid = this.parent.getFluid();
		return String.format(
			"%dx%s@%s",
			this.parent.getAmount(),
			fluid != null ? ForgeRegistries.FLUIDS.getKey(fluid) : "(null)",
			this.parent.getTag() != null ? this.parent.getTag().toString() : "(-)"
		);
	}
}
