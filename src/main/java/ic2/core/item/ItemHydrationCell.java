package ic2.core.item;

import ic2.core.crop.TileEntityCrop;
import ic2.core.util.Ic2Tooltip;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Crop hydration cell. This is a durability-tracked crop item, not a fluid container, and must not
 * expose fluid capabilities.
 */
public class ItemHydrationCell extends Item {
  private static final int CHARGES = 10000;

  public ItemHydrationCell(Properties properties) {
    super(properties);
  }

  @Override
  public @NotNull InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();
    BlockEntity be = level.getBlockEntity(context.getClickedPos());
    if (!(be instanceof TileEntityCrop crop)) {
      return InteractionResult.PASS;
    }

    ItemStack stack = context.getItemInHand();
    if (this.applyToCrop(stack, crop, true)) {
      return InteractionResult.sidedSuccess(level.isClientSide);
    }

    return InteractionResult.PASS;
  }

  /**
   * Applies hydration charges to a crop and mutates {@code stack} when successful.
   *
   * @param manual player use; automatic use caps application per call
   * @return true if any hydration was applied
   */
  public boolean applyToCrop(ItemStack stack, TileEntityCrop crop, boolean manual) {
    int consumed = this.getUsage(stack) + 1;
    int amount = Math.max(0, CHARGES - consumed);
    if (!manual && amount > 180) {
      amount = 180;
    }

    amount = crop.applyHydration(amount, false);
    if (amount <= 0) {
      return false;
    }

    consumed += amount;
    if (consumed >= CHARGES) {
      stack.shrink(1);
    } else {
      this.setUsage(stack, consumed);
    }

    return true;
  }

  private int getUsage(ItemStack stack) {
    CompoundTag nbt = StackUtil.getTag(stack);
    return nbt != null ? nbt.getInt("uses") : 0;
  }

  private void setUsage(ItemStack stack, int uses) {
    if (uses <= 0) {
      StackUtil.setTag(stack, null);
    } else {
      StackUtil.getOrCreateNbtData(stack).putInt("uses", uses);
    }
  }

  private double getChargeLevel(ItemStack stack) {
    return (double) (CHARGES - this.getUsage(stack)) / CHARGES;
  }

  @Override
  public boolean isBarVisible(@NotNull ItemStack stack) {
    return this.getUsage(stack) > 0;
  }

  @Override
  public int getBarWidth(@NotNull ItemStack stack) {
    return (int) Math.round(this.getChargeLevel(stack) * 13.0);
  }

  @Override
  public int getBarColor(@NotNull ItemStack stack) {
    return Mth.hsvToRgb((float) (this.getChargeLevel(stack) / 3.0), 1.0F, 1.0F);
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void appendHoverText(
      @NotNull ItemStack stack,
      Item.TooltipContext world,
      @NotNull List<Component> tooltip,
      @NotNull TooltipFlag advanced) {
    if (stack.getCount() == 1 && advanced.isAdvanced()) {
      Ic2Tooltip.add(
          tooltip,
          Component.translatable("item.durability", CHARGES - this.getUsage(stack), CHARGES));
    }
  }
}
