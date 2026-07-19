package ic2.core.item;

import ic2.api.recipe.Recipes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemScrapBox extends Item {
  public ItemScrapBox(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (world.isClientSide) {
      return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    ItemStack drop = Recipes.scrapboxDrops.getDrop(stack, false);
    if (drop == null || player.drop(drop, false) == null) {
      return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    if (!player.getAbilities().instabuild) {
      stack.shrink(1);
    }
    return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
  }
}
