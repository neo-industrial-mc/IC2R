package me.halfcooler.ic2r.integration.jei.recipe.machine;

import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class CannerBottleLiquidRecipeWrapper
{
	private final List<ItemStack> emptyContainers;
	private final Ic2rFluidStack fluid;
	private final ItemStack filledContainer;

	public CannerBottleLiquidRecipeWrapper(List<ItemStack> emptyContainers, Ic2rFluidStack fluid, ItemStack filledContainer)
	{
		this.emptyContainers = emptyContainers;
		this.fluid = fluid;
		this.filledContainer = filledContainer;
	}

	public List<ItemStack> getEmptyContainers()
	{
		return this.emptyContainers;
	}

	public FluidStack getFluidInput()
	{
		return new FluidStack(this.fluid.getFluid(), this.fluid.getAmountMb());
	}

	public ItemStack getFilledContainer()
	{
		return this.filledContainer;
	}
}
