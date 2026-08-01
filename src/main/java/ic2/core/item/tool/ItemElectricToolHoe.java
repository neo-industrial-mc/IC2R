package ic2.core.item.tool;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class ItemElectricToolHoe extends ItemElectricTool {
  private static final float MINING_SPEED = 16.0F;

  public ItemElectricToolHoe(Properties settings) {
    super(settings, 50, Tiers.IRON, List.of(BlockTags.MINEABLE_WITH_HOE));
    this.maxCharge = 10000;
    this.transferLimit = 100;
    this.tier = 1;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    ItemStack stack = context.getItemInHand();
    if (!this.canUse(stack) || context.getClickedFace() == Direction.DOWN) {
      return InteractionResult.PASS;
    }

    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos();
    BlockState tilledState =
        level.getBlockState(pos).getToolModifiedState(context, ItemAbilities.HOE_TILL, false);
    if (tilledState == null) {
      return InteractionResult.PASS;
    }

    Player player = context.getPlayer();
    level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
    if (!level.isClientSide) {
      level.setBlock(pos, tilledState, 11);
      level.gameEvent(
          GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(context.getPlayer(), tilledState));
      this.consumeEnergy(stack, this.operationEnergyCost, player);
    }

    return InteractionResult.SUCCESS;
  }

  @Override
  public float getDestroySpeed(ItemStack stack, BlockState state) {
    float speed = super.getDestroySpeed(stack, state);
    return speed == 1.0F ? speed : speed * MINING_SPEED / Tiers.IRON.getSpeed();
  }

  @Override
  public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
    return ItemAbilities.DEFAULT_HOE_ACTIONS.contains(itemAbility);
  }
}
