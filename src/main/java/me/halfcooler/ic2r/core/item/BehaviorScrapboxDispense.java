package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.api.recipe.Recipes;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;

public class BehaviorScrapboxDispense extends DefaultDispenseItemBehavior {
  @Override
  protected ItemStack execute(BlockSource source, ItemStack stack) {
    ItemStack drop = Recipes.scrapboxDrops.getDrop(stack, true);
    if (drop != null) {
      super.execute(source, drop);
    }
    return stack;
  }
}
