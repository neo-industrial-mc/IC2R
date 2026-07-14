package me.halfcooler.ic2r.core.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public interface Ic2rFluidStack
{
	Ic2rFluidStack EMPTY = create(Fluids.EMPTY, 0);

	static Ic2rFluidStack create(Fluid fluid, int amount)
	{
		if (fluid == null)
		{
			throw new NullPointerException();
		} else
		{
			return FluidHandler.ENV_HANDLER.createFluidStackMb(fluid, amount, null);
		}
	}

	static Ic2rFluidStack get(ItemStack stack)
	{
		Item item = stack.getItem();
		return item instanceof Ic2rFluidItem ? ((Ic2rFluidItem) item).getFluidStack(stack) : FluidHandler.ENV_HANDLER.getFluidStack(stack);
	}

	static Ic2rFluidStack[] getAll(ItemStack stack)
	{
		Item item = stack.getItem();
		return item instanceof Ic2rFluidItem ? new Ic2rFluidStack[] { ((Ic2rFluidItem) item).getFluidStack(stack) } : FluidHandler.ENV_HANDLER.getFluidStacks(stack);
	}

	static Ic2rFluidStack read(CompoundTag nbt)
	{
		return FluidHandler.ENV_HANDLER.readFluidStack(nbt);
	}

	/**
	 * Translation key for this fluid's {@link net.minecraftforge.fluids.FluidType} description.
	 * Prefer {@link #getFluidDisplayName()} when building UI text so nested translation works.
	 */
	default String getFluidTypeKey()
	{
		if (isEmpty())
		{
			return null;
		}

		return getFluid().getFluidType().getDescriptionId();
	}

	/**
	 * Localized fluid name from Forge {@link net.minecraftforge.fluids.FluidType}.
	 */
	default Component getFluidDisplayName()
	{
		if (isEmpty())
		{
			return Component.empty();
		}

		return getFluid().getFluidType().getDescription();
	}

	Ic2rFluidStack copy();

	default Ic2rFluidStack copyWithAmountMb(int amountMb)
	{
		if (this.isEmpty())
		{
			return EMPTY;
		}

		Ic2rFluidStack ret = this.copy();
		ret.setAmountMb(amountMb);
		return ret;
	}

	Fluid getFluid();

	boolean hasExactFluid(Fluid var1);

	boolean hasExactFluid(Ic2rFluidStack var1);

	default boolean isEmpty()
	{
		return this.getAmountMb() == 0;
	}

	int getAmountMb();

	void setAmountMb(int var1);

	default void increaseMb(int amount)
	{
		this.setAmountMb(this.getAmountMb() + amount);
	}

	default void decreaseMb(int amount)
	{
		this.setAmountMb(this.getAmountMb() - amount);
	}

	void toNbt(CompoundTag var1);
}
