package ic2.forge;

import ic2.api.item.INanoSaberState;
import ic2.core.item.tool.AbstractItemNanoSaber;
import ic2.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;

public final class NanoSaberCapabilities {

  private NanoSaberCapabilities() {}

  public static INanoSaberState getState(ItemStack stack) {
    if (StackUtil.isEmpty(stack) || !(stack.getItem() instanceof AbstractItemNanoSaber)) {
      return InactiveNanoSaberState.INSTANCE;
    }
    INanoSaberState state = stack.getCapability(Ic2Capabilities.NANO_SABER_STATE);
    return state != null ? state : InactiveNanoSaberState.INSTANCE;
  }

  public static boolean isActive(ItemStack stack) {
    return getState(stack).isActive();
  }

  public static void setActive(ItemStack stack, boolean active) {
    getState(stack).setActive(active);
  }

  public static int getEnergyTick(ItemStack stack) {
    return getState(stack).getEnergyTick();
  }

  public static void setEnergyTick(ItemStack stack, int energyTick) {
    getState(stack).setEnergyTick(energyTick);
  }

  private enum InactiveNanoSaberState implements INanoSaberState {
    INSTANCE;

    @Override
    public boolean isActive() {
      return false;
    }

    @Override
    public void setActive(boolean active) {}

    @Override
    public int getEnergyTick() {
      return 0;
    }

    @Override
    public void setEnergyTick(int energyTick) {}
  }
}
