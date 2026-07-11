package ic2.core.uu;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class MachineRecipeResolver implements IRecipeResolver {
  private static final double transformCost = 14.0;
  private final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> manager;
  private final Recipes.IGetter<
          ? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>>
      managerGetter;

  public MachineRecipeResolver(
      IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> manager) {
    this.manager = manager;
    this.managerGetter = null;
  }

  public MachineRecipeResolver(
      Recipes.IGetter<? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>>
          managerGetter) {
    this.manager = null;
    this.managerGetter = managerGetter;
  }

  @Override
  public List<RecipeTransformation> getTransformations() {
    IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> manager = this.getManager();
    if (manager == null || !manager.isIterable()) {
      return Collections.emptyList();
    }

    List<RecipeTransformation> ret = new ArrayList<>();

    for (MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe : manager.getRecipes()) {
      try {
        List<List<LeanItemStack>> inputs = RecipeUtil.convertInputs(recipe.getInput().getInputs());
        List<LeanItemStack> outputs = RecipeUtil.convertOutputs(recipe.getOutput());
        ret.add(new RecipeTransformation(14.0, inputs, outputs));
      } catch (IllegalArgumentException e) {
        IC2.log.warn(LogCategory.Uu, e, "invalid recipe");
      }
    }

    return ret;
  }

  private IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> getManager() {
    if (this.manager != null) {
      return this.manager;
    }

    if (this.managerGetter == null || IC2.envProxy.getServer() == null) {
      return null;
    }

    return this.managerGetter.get(IC2.envProxy.getServer().overworld());
  }
}
