package me.halfcooler.ic2r.platform.services;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * External energy (FE/RF and future Fabric transfer) bridge.
 * <p>
 * IC EU logic stays in common ({@code core.energy}); this SPI only covers loader capability /
 * transfer interop. EU↔FE conversion is pure math in {@code EnergyBridgeMath} (default
 * {@code 2.0} FE/EU, aligned with AE2). Contract: {@code docs/spec/energy_bridge_contract.md}.
 * <p>
 * Units: {@code long} energy amounts are <strong>external</strong> units (typically FE), not EU.
 * Forge: {@code PlatformEnergyBridgeForge} via {@code IEnergyStorage}.
 */
public interface PlatformEnergyBridge
{
	/** Whether the neighbour can accept external energy from {@code side} of {@code be}. */
	boolean canReceive(BlockEntity be, @Nullable Direction side);

	/** Whether the neighbour can provide external energy toward {@code side} of {@code be}. */
	boolean canExtract(BlockEntity be, @Nullable Direction side);

	/**
	 * Insert external energy into a neighbour / capability holder.
	 *
	 * @return amount accepted
	 */
	long insert(BlockEntity be, @Nullable Direction side, long amount, boolean simulate);

	/**
	 * Extract external energy from a neighbour / capability holder.
	 *
	 * @return amount extracted
	 */
	long extract(BlockEntity be, @Nullable Direction side, long maxAmount, boolean simulate);
}
