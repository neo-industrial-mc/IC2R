package me.halfcooler.ic2r.api.recipe;

import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;

import java.util.Map;

import net.minecraft.world.level.material.Fluid;

public interface IFermenterRecipeManager extends ILiquidAcceptManager
{
	void addRecipe(Fluid var1, int var2, int var3, Fluid var4, int var5);

	IFermenterRecipeManager.FermentationProperty getFermentationInformation(Fluid var1);

	Ic2rFluidStack getOutput(Fluid var1);

	Map<Fluid, IFermenterRecipeManager.FermentationProperty> getRecipeMap();

	record FermentationProperty(int inputAmount, int heat, Fluid output, int outputAmount)
		{

			public Ic2rFluidStack getOutput()
			{
				return this.output == null ? null : Ic2rFluidStack.create(this.output, this.outputAmount);
			}
		}
}
