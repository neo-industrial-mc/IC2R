package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.api.item.INanoSaberState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

final class ItemNanoSaberCapImpl implements ICapabilityProvider {

    private final NanoSaberStateImpl state;

    private final LazyOptional<INanoSaberState> optional;

    ItemNanoSaberCapImpl(ItemStack stack) {
        this.state = new NanoSaberStateImpl(stack);
        this.optional = LazyOptional.of(() -> this.state);
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction facing) {
        return capability == NanoSaberCapabilities.NANO_SABER_STATE ? this.optional.cast() : LazyOptional.empty();
    }
}
