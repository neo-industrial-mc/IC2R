package me.halfcooler.ic2r.compat;

import net.minecraft.world.item.crafting.CraftingRecipe;

public interface Ic2rCraftingRecipe extends CraftingRecipe
{
	int getIc2rRecipeWidth();

	int getIc2rRecipeHeight();

	default int getHeight()
	{
		return this.getIc2rRecipeHeight();
	}

	default int getWidth()
	{
		return this.getIc2rRecipeWidth();
	}
}