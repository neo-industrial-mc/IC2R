package ic2.core.gametest;

import ic2.core.item.reactor.AbstractDamageableReactorComponent;
import ic2.core.recipe.GradualRecipe;
import ic2.core.ref.Ic2Items;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class GradualRecipeGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  @GameTest(template = EMPTY)
  public static void rshCondensatorGradualRecipeRepairsAndRejectsUndamaged(GameTestHelper helper) {
    AbstractDamageableReactorComponent condensator =
        (AbstractDamageableReactorComponent) Ic2Items.RSH_CONDENSATOR;

    ItemStack damaged = new ItemStack(condensator);
    condensator.setUse(damaged, 15000);
    CraftingInput repairInput = input(damaged);
    CraftingRecipe recipe = findRecipe(helper, repairInput);
    helper.assertTrue(recipe instanceof GradualRecipe, "damaged RSH recipe should be gradual");

    ItemStack repaired = recipe.assemble(repairInput, helper.getLevel().registryAccess());
    helper.assertTrue(
        repaired.getItem() == condensator, "gradual recipe should return the RSH condensator");
    helper.assertValueEqual(
        condensator.getUse(repaired), 5000, "RSH use after one redstone repair");

    ItemStack lightlyDamaged = new ItemStack(condensator);
    condensator.setUse(lightlyDamaged, 5000);
    CraftingInput clampedInput = input(lightlyDamaged);
    CraftingRecipe clampedRecipe = findRecipe(helper, clampedInput);
    helper.assertTrue(
        clampedRecipe instanceof GradualRecipe, "lightly damaged RSH recipe should be gradual");
    ItemStack fullyRepaired =
        clampedRecipe.assemble(clampedInput, helper.getLevel().registryAccess());
    helper.assertValueEqual(
        condensator.getUse(fullyRepaired), 0, "RSH repair should clamp use to zero");

    CraftingInput undamagedInput = input(new ItemStack(condensator));
    helper.assertTrue(
        !recipe.matches(undamagedInput, helper.getLevel()),
        "the gradual recipe must reject an undamaged RSH condensator");
    helper.assertTrue(
        findRecipe(helper, undamagedInput) == null,
        "an undamaged RSH condensator must not match the gradual recipe");

    helper.succeed();
  }

  private static CraftingInput input(ItemStack condensator) {
    return CraftingInput.of(2, 1, List.of(condensator, new ItemStack(Items.REDSTONE)));
  }

  private static CraftingRecipe findRecipe(GameTestHelper helper, CraftingInput input) {
    return helper
        .getLevel()
        .getServer()
        .getRecipeManager()
        .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
        .map(RecipeHolder::value)
        .orElse(null);
  }
}
