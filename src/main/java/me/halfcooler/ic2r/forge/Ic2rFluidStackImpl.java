package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;

import java.util.Objects;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
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
		return this.parent.isComponentsPatchEmpty()
			? Ic2rFluidStack.create(this.parent.getFluid(), this.parent.getAmount())
			: new Ic2rFluidStackImpl(this.parent.copy());
	}

	@Override
	public Fluid getFluid()
	{
		return this.parent.getFluid();
	}

	@Override
	public boolean hasExactFluid(Fluid fluid)
	{
		return this.parent.isComponentsPatchEmpty() && fluid == this.parent.getFluid();
	}

	@Override
	public boolean hasExactFluid(Ic2rFluidStack fs)
	{
		return fs instanceof Ic2rFluidStackImpl other
			? FluidStack.isSameFluidSameComponents(this.parent, other.parent)
			: this.hasExactFluid(fs.getFluid());
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
		// FluidStack is component-based; store a simplified representation for IC2R internal NBT.
		nbt.putString("FluidName", BuiltInRegistries.FLUID.getKey(this.parent.getFluid()).toString());
		nbt.putInt("Amount", this.parent.getAmount());
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Ic2rFluidStack o))
		{
			return false;
		}
		if (this.parent.getAmount() != o.getAmountMb() || this.parent.getFluid() != o.getFluid())
		{
			return false;
		}
		return o instanceof Ic2rFluidStackImpl other
			? FluidStack.isSameFluidSameComponents(this.parent, other.parent)
			: this.parent.isComponentsPatchEmpty();
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
			fluid != null ? BuiltInRegistries.FLUID.getKey(fluid) : "(null)",
			this.parent.isComponentsPatchEmpty() ? "(-)" : this.parent.getComponentsPatch().toString()
		);
	}
}
