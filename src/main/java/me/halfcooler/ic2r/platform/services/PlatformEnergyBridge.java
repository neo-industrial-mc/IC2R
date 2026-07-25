package me.halfcooler.ic2r.platform.services;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * External energy (FE/RF and future Fabric transfer) bridge.
 * <p>
 * IC EU stays in common ({@code core.energy}); this SPI only covers loader capability interop.
 * EU↔FE conversion is pure math in {@code EnergyBridgeMath}. Amounts are external units (typically FE), not EU.
 */
public interface PlatformEnergyBridge
{
	/**
	 * Whether the neighbor can accept external energy from {@code side} of {@code be}.
	 */
	boolean canReceive(BlockEntity be, @Nullable Direction side);

	/**
	 * Whether the neighbor can provide external energy toward {@code side} of {@code be}.
	 */
	boolean canExtract(BlockEntity be, @Nullable Direction side);

	/**
	 * Insert external energy into a neighbor / capability holder.
	 *
	 * @return amount accepted
	 */
	long insert(BlockEntity be, @Nullable Direction side, long amount, boolean simulate);

	/**
	 * Extract external energy from a neighbor / capability holder.
	 *
	 * @return amount extracted
	 */
	long extract(BlockEntity be, @Nullable Direction side, long maxAmount, boolean simulate);
}
