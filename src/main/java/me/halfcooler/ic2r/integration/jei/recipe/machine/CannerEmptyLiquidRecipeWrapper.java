package me.halfcooler.ic2r.integration.jei.recipe.machine;

import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class CannerEmptyLiquidRecipeWrapper
{
	private final ItemStack filledContainer;
	private final List<ItemStack> drainedContainers;
	private final Ic2rFluidStack fluid;

	public CannerEmptyLiquidRecipeWrapper(ItemStack filledContainer, List<ItemStack> drainedContainers, Ic2rFluidStack fluid)
	{
		this.filledContainer = filledContainer;
		this.drainedContainers = drainedContainers;
		this.fluid = fluid;
	}

	public ItemStack getFilledContainer()
	{
		return this.filledContainer;
	}

	public List<ItemStack> getDrainedContainers()
	{
		return this.drainedContainers;
	}

	public FluidStack getFluidOutput()
	{
		return new FluidStack(this.fluid.getFluid(), this.fluid.getAmountMb());
	}
}
