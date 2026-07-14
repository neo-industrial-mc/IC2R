package me.halfcooler.ic2r.integration.jei.recipe.machine;

import java.util.List;

import net.minecraft.world.item.ItemStack;

public interface IJeiRecipeWrapper
{
	List<List<ItemStack>> getInputs();

	List<ItemStack> getOutputs();
}
