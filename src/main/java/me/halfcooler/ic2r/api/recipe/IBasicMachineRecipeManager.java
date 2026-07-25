package me.halfcooler.ic2r.api.recipe;

import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public interface IBasicMachineRecipeManager extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	@Deprecated
	RecipeOutput getOutputFor(ItemStack var1, boolean var2);
}
