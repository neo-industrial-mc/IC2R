package me.halfcooler.ic2r.api.recipe;

import me.halfcooler.ic2r.api.util.FluidContainerOutputMode;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;

public interface IFillFluidContainerRecipeManager extends IMachineRecipeManager<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input>
{
	MachineRecipeResult<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input> apply(
		IFillFluidContainerRecipeManager.Input var1, FluidContainerOutputMode var2, boolean var3
	);

	record Input(ItemStack container, Ic2rFluidStack fluid)
		{
		}
}
