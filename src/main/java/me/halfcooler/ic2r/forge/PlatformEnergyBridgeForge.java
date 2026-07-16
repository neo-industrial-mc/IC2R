package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.energy.EnergyBridgeMath;
import me.halfcooler.ic2r.platform.services.PlatformEnergyBridge;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Forge {@link IEnergyStorage} implementation of {@link PlatformEnergyBridge} (G2.8).
 * <p>
 * Amounts on this SPI are <strong>external FE</strong> units, not EU. EU↔FE conversion lives in
 * common {@link EnergyBridgeMath}; this class only probes and transfers via Forge capabilities.
 * Does not alter IC EnergyNet topology or default machine power to FE.
 */
public final class PlatformEnergyBridgeForge implements PlatformEnergyBridge {

    @Override
    public boolean canReceive(BlockEntity be, @Nullable Direction side) {
        IEnergyStorage storage = getStorage(be, side);
        return storage != null && storage.canReceive();
    }

    @Override
    public boolean canExtract(BlockEntity be, @Nullable Direction side) {
        IEnergyStorage storage = getStorage(be, side);
        return storage != null && storage.canExtract();
    }

    @Override
    public long insert(BlockEntity be, @Nullable Direction side, long amount, boolean simulate) {
        if (amount <= 0L) {
            return 0L;
        }
        IEnergyStorage storage = getStorage(be, side);
        if (storage == null || !storage.canReceive()) {
            return 0L;
        }
        int offer = EnergyBridgeMath.clampToIntEnergy(amount);
        if (offer <= 0) {
            return 0L;
        }
        return storage.receiveEnergy(offer, simulate);
    }

    @Override
    public long extract(BlockEntity be, @Nullable Direction side, long maxAmount, boolean simulate) {
        if (maxAmount <= 0L) {
            return 0L;
        }
        IEnergyStorage storage = getStorage(be, side);
        if (storage == null || !storage.canExtract()) {
            return 0L;
        }
        int request = EnergyBridgeMath.clampToIntEnergy(maxAmount);
        if (request <= 0) {
            return 0L;
        }
        return storage.extractEnergy(request, simulate);
    }

    @Nullable
    private static IEnergyStorage getStorage(BlockEntity be, @Nullable Direction side) {
        if (be == null) {
            return null;
        }
        return be.getCapability(Capabilities.ENERGY, side).orElse(null);
    }
}
