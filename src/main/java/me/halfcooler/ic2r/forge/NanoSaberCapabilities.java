package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.api.item.INanoSaberState;
import me.halfcooler.ic2r.core.item.tool.AbstractItemNanoSaber;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.CapabilityManager;
import net.neoforged.neoforge.capabilities.CapabilityToken;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.LazyOptional;

public final class NanoSaberCapabilities {

    public static final Capability<INanoSaberState> NANO_SABER_STATE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private NanoSaberCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(INanoSaberState.class);
    }

    public static INanoSaberState getState(ItemStack stack) {
        if (StackUtil.isEmpty(stack) || !(stack.getItem() instanceof AbstractItemNanoSaber)) {
            return InactiveNanoSaberState.INSTANCE;
        }
        LazyOptional<INanoSaberState> optional = stack.getCapability(NANO_SABER_STATE, null);
        if (!optional.isPresent()) {
            return InactiveNanoSaberState.INSTANCE;
        }
        return optional.orElse(InactiveNanoSaberState.INSTANCE);
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
