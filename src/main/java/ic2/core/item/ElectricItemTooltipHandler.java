package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.core.util.Ic2Tooltip;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ElectricItemTooltipHandler {
  public static void addTooltip(ItemStack stack, List<Component> out) {
    if (stack != null && ElectricItem.manager.getMaxCharge(stack) > 0.0) {
      String tooltip = ElectricItem.manager.getToolTip(stack);
      if (tooltip != null && !tooltip.trim().isEmpty()) {
        Ic2Tooltip.add(out, Component.literal(tooltip));
        if (Screen.hasShiftDown()) {
          Ic2Tooltip.add(
              out,
              Component.translatable(
                  "ic2.item.tooltip.power_tier", ElectricItem.manager.getTier(stack)));
        }
      }
    }
  }
}
