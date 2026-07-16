package me.halfcooler.ic2r.compat;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;


public interface Ic2rCraftingRecipe extends CraftingRecipe, ShapedRecipe
{
	int getIc2rRecipeWidth();

	int getIc2rRecipeHeight();

	@Override
	default int getHeight()
	{
		return this.getIc2rRecipeHeight();
	}

	@Override
	default int getWidth()
	{
		return this.getIc2rRecipeWidth();
	}
}
