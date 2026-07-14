package me.halfcooler.ic2r.integration.jei.recipe.machine;

import me.halfcooler.ic2r.api.recipe.ICannerEnrichRecipeManager;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class CannerEnrichRecipeWrapper
{
	private final Ic2rFluidStack fluidInput;
	private final List<ItemStack> additiveInputs;
	private final Ic2rFluidStack fluidOutput;

	public CannerEnrichRecipeWrapper(MachineRecipe<ICannerEnrichRecipeManager.Input, Ic2rFluidStack> recipe)
	{
		this.fluidInput = recipe.getInput().fluid();
		this.additiveInputs = recipe.getInput().additive().getInputs();
		this.fluidOutput = recipe.getOutput();
	}

	public FluidStack getFluidInput()
	{
		return new FluidStack(this.fluidInput.getFluid(), this.fluidInput.getAmountMb());
	}

	public List<ItemStack> getAdditiveInputs()
	{
		return this.additiveInputs;
	}

	public FluidStack getFluidOutput()
	{
		return new FluidStack(this.fluidOutput.getFluid(), this.fluidOutput.getAmountMb());
	}
}
