package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.api.item.INanoSaberState;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.item.tool.AbstractItemNanoSaber;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class NanoSaberCapabilities {

    public static final ItemCapability<INanoSaberState, Void> NANO_SABER_STATE =
        ItemCapability.createVoid(IC2R.getIdentifier("nano_saber_state"), INanoSaberState.class);

    private NanoSaberCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        // Item providers are registered in Ic2rCapabilities for items that implement AbstractItemNanoSaber.
    }

    public static INanoSaberState getState(ItemStack stack) {
        if (StackUtil.isEmpty(stack) || !(stack.getItem() instanceof AbstractItemNanoSaber)) {
            return InactiveNanoSaberState.INSTANCE;
        }
        INanoSaberState state = stack.getCapability(NANO_SABER_STATE);
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
        public void setActive(boolean active) {
        }

        @Override
        public int getEnergyTick() {
            return 0;
        }

        @Override
        public void setEnergyTick(int energyTick) {
        }
    }
}
