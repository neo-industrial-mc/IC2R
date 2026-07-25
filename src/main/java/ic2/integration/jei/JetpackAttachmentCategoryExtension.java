package ic2.integration.jei;

import ic2.api.item.ElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.armor.jetpack.JetpackAttachmentRecipe;
import ic2.core.item.armor.jetpack.JetpackHandler;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Supplies JEI with the dynamic inputs and outputs omitted by the special attachment recipe. */
final class JetpackAttachmentCategoryExtension
    implements ICraftingCategoryExtension<JetpackAttachmentRecipe> {
  private final List<ItemStack> chestplates;
  private final List<ItemStack> attachedChestplates;

  JetpackAttachmentCategoryExtension(IIngredientManager ingredientManager) {
    Map<Item, ItemStack> chestplatesByItem = new LinkedHashMap<>();
    for (ItemStack stack : ingredientManager.getAllItemStacks()) {
      Item item = stack.getItem();
      if (StackUtil.getEquipmentSlotForItem(stack) == EquipmentSlot.CHEST
          && !JetpackAttachmentRecipe.blacklistedItems.contains(item)
          && !JetpackHandler.hasJetpackAttached(stack)) {
        chestplatesByItem.putIfAbsent(item, stack.copyWithCount(1));
      }
    }

    this.chestplates = List.copyOf(chestplatesByItem.values());
    this.attachedChestplates =
        this.chestplates.stream().map(JetpackAttachmentCategoryExtension::attach).toList();
  }

  private static ItemStack attach(ItemStack chestplate) {
    ItemStack output = chestplate.copy();
    JetpackHandler.setJetpackAttached(output, true);
    ItemStack jetpack =
        ElectricItemManager.getCharged(Ic2Items.JETPACK_ELECTRIC, Double.POSITIVE_INFINITY);
    ElectricItem.manager.charge(
        output, ElectricItem.manager.getCharge(jetpack), Integer.MAX_VALUE, true, false);
    return output;
  }

  @Override
  public void setRecipe(
      RecipeHolder<JetpackAttachmentRecipe> holder,
      IRecipeLayoutBuilder builder,
      ICraftingGridHelper craftingGridHelper,
      IFocusGroup focuses) {
    ItemStack jetpack =
        ElectricItemManager.getCharged(Ic2Items.JETPACK_ELECTRIC, Double.POSITIVE_INFINITY);
    craftingGridHelper.createAndSetInputs(
        builder,
        List.of(
            this.chestplates,
            List.of(jetpack),
            List.of(new ItemStack(Ic2Items.JETPACK_ATTACHMENT_PLATE))),
        3,
        1);
    craftingGridHelper.createAndSetOutputs(builder, this.attachedChestplates);
    builder.setShapeless();
  }

  @Override
  public void onDisplayedIngredientsUpdate(
      RecipeHolder<JetpackAttachmentRecipe> holder,
      List<IRecipeSlotDrawable> recipeSlots,
      IFocusGroup focuses) {
    for (IRecipeSlotDrawable input : recipeSlots) {
      if (input.getRole() != RecipeIngredientRole.INPUT) {
        continue;
      }
      ItemStack displayedInput = input.getDisplayedItemStack().orElse(ItemStack.EMPTY);
      for (int i = 0; i < this.chestplates.size(); i++) {
        if (displayedInput.is(this.chestplates.get(i).getItem())) {
          for (IRecipeSlotDrawable output : recipeSlots) {
            if (output.getRole() == RecipeIngredientRole.OUTPUT) {
              output.clearDisplayOverrides();
              output.createDisplayOverrides().addItemStack(this.attachedChestplates.get(i));
              return;
            }
          }
        }
      }
    }
  }

  @Override
  public int getWidth(RecipeHolder<JetpackAttachmentRecipe> holder) {
    return 3;
  }

  @Override
  public int getHeight(RecipeHolder<JetpackAttachmentRecipe> holder) {
    return 1;
  }
}
