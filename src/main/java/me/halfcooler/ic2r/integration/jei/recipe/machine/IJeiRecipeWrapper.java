package me.halfcooler.ic2r.integration.jei.recipe.machine;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IJeiRecipeWrapper
{
	List<List<ItemStack>> getInputs();

	List<ItemStack> getOutputs();
}
