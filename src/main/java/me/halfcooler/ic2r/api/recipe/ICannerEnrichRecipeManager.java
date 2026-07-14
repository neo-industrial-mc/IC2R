package me.halfcooler.ic2r.api.recipe;

import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import net.minecraft.world.item.ItemStack;

public interface ICannerEnrichRecipeManager extends IMachineRecipeManager<ICannerEnrichRecipeManager.Input, Ic2rFluidStack, ICannerEnrichRecipeManager.RawInput>
{
	@Deprecated
	RecipeOutput getOutputFor(Ic2rFluidStack var1, ItemStack var2, boolean var3, boolean var4);

	record Input(Ic2rFluidStack fluid, IRecipeInput additive)
		{

			public boolean matches(Ic2rFluidStack fluid, ItemStack additive)
			{
				return this.fluid.hasExactFluid(fluid) && this.additive.matches(additive);
			}
		}

	record RawInput(Ic2rFluidStack fluid, ItemStack additive)
		{
		}
}
