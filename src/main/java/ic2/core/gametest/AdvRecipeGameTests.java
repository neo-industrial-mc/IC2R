package ic2.core.gametest;

import ic2.core.IC2;
import ic2.core.recipe.AdvRecipe;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class AdvRecipeGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  @GameTest(template = EMPTY)
  public static void hiddenRecipeRetainsDisplayIngredients(GameTestHelper helper) {
    Object loadedRecipe =
        helper
            .getLevel()
            .getServer()
            .getRecipeManager()
            .byKey(IC2.getIdentifier("shaped/gunpowder"))
            .orElseThrow(() -> new IllegalStateException("gunpowder recipe is not loaded"))
            .value();
    helper.assertTrue(loadedRecipe instanceof AdvRecipe, "gunpowder recipe should be an AdvRecipe");

    AdvRecipe recipe = (AdvRecipe) loadedRecipe;
    helper.assertTrue(recipe.hidden, "gunpowder recipe should be marked hidden");
    helper.assertTrue(
        recipe.getIngredients().isEmpty(),
        "hidden ingredients should remain absent from the vanilla recipe book");

    NonNullList<Ingredient> displayIngredients = recipe.getDisplayIngredients();
    helper.assertValueEqual(displayIngredients.size(), 9, "JEI display ingredient count");

    for (int slot = 0; slot < displayIngredients.size(); slot++) {
      ItemStack expected = new ItemStack(slot % 2 == 0 ? Items.REDSTONE : Ic2Items.COAL_DUST);
      helper.assertTrue(
          displayIngredients.get(slot).test(expected),
          "JEI display ingredient should match slot " + slot);
    }

    helper.succeed();
  }
}
