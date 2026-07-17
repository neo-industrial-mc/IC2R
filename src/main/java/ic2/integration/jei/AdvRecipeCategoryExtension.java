package ic2.integration.jei;

import ic2.core.recipe.AdvRecipe;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Exposes IC2 shaped-recipe dimensions and layout to JEI's vanilla crafting category. */
final class AdvRecipeCategoryExtension implements ICraftingCategoryExtension<AdvRecipe> {
  @Override
  public void setRecipe(
      RecipeHolder<AdvRecipe> holder,
      IRecipeLayoutBuilder builder,
      ICraftingGridHelper craftingGridHelper,
      IFocusGroup focuses) {
    AdvRecipe recipe = holder.value();
    craftingGridHelper.createAndSetOutputs(builder, List.of(recipe.output));
    craftingGridHelper.createAndSetIngredients(
        builder, recipe.getIngredients(), recipe.getIc2RecipeWidth(), recipe.getIc2RecipeHeight());
  }

  @Override
  public int getWidth(RecipeHolder<AdvRecipe> holder) {
    return holder.value().getIc2RecipeWidth();
  }

  @Override
  public int getHeight(RecipeHolder<AdvRecipe> holder) {
    return holder.value().getIc2RecipeHeight();
  }
}
