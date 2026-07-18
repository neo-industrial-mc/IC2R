package me.halfcooler.ic2r.integration.jei;

import java.util.List;

import me.halfcooler.ic2r.core.recipe.AdvRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Exposes visible IC2R shaped recipes to JEI's vanilla crafting category. */
final class AdvRecipeCategoryExtension implements ICraftingCategoryExtension<AdvRecipe>
{
	@Override
	public void setRecipe(RecipeHolder<AdvRecipe> holder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses)
	{
		AdvRecipe recipe = holder.value();
		craftingGridHelper.createAndSetOutputs(builder, List.of(recipe.output));
		craftingGridHelper.createAndSetIngredients(builder, recipe.getDisplayIngredients(), recipe.getIc2rRecipeWidth(), recipe.getIc2rRecipeHeight());
	}

	@Override
	public boolean isHandled(RecipeHolder<AdvRecipe> holder)
	{
		return holder.value().canShow();
	}

	@Override
	public int getWidth(RecipeHolder<AdvRecipe> holder)
	{
		return holder.value().getIc2rRecipeWidth();
	}

	@Override
	public int getHeight(RecipeHolder<AdvRecipe> holder)
	{
		return holder.value().getIc2rRecipeHeight();
	}
}
