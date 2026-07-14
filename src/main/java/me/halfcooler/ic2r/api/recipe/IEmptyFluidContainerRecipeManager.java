package me.halfcooler.ic2r.api.recipe;

import me.halfcooler.ic2r.api.util.FluidContainerOutputMode;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public interface IEmptyFluidContainerRecipeManager extends IMachineRecipeManager<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack>
{
	MachineRecipeResult<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack> apply(ItemStack var1, Fluid var2, FluidContainerOutputMode var3, boolean var4);

	record Output(Collection<ItemStack> container, Ic2rFluidStack fluid)
		{
		}
}
