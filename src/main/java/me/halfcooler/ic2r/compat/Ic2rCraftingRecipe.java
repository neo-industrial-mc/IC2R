package me.halfcooler.ic2r.compat;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.common.crafting.IShapedRecipe;

public interface Ic2rCraftingRecipe extends CraftingRecipe, IShapedRecipe<CraftingContainer>
{
	int getIc2rRecipeWidth();

	int getIc2rRecipeHeight();

	@Override
	default int getRecipeHeight()
	{
		return this.getIc2rRecipeHeight();
	}

	@Override
	default int getRecipeWidth()
	{
		return this.getIc2rRecipeWidth();
	}
}
