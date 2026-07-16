package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.api.item.INanoSaberState;
import net.minecraft.world.item.ItemStack;

/**
 * Item-stack nano saber state provider for NeoForge {@link ItemCapability}.
 */
final class ItemNanoSaberCapImpl implements INanoSaberState {

    private final NanoSaberStateImpl state;

    ItemNanoSaberCapImpl(ItemStack stack) {
        this.state = new NanoSaberStateImpl(stack);
    }

    INanoSaberState asState() {
        return this.state;
    }

    @Override
    public boolean isActive() {
        return this.state.isActive();
    }

    @Override
    public void setActive(boolean active) {
        this.state.setActive(active);
    }

    @Override
    public int getEnergyTick() {
        return this.state.getEnergyTick();
    }

    @Override
    public void setEnergyTick(int energyTick) {
        this.state.setEnergyTick(energyTick);
    }
}
