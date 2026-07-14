package me.halfcooler.ic2r.energy;

import me.halfcooler.ic2r.core.energy.EnergyBridgeMath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * G2.8 pure EU↔FE conversion tests (no Forge capability / Level).
 */
class EnergyBridgeMathTest
{
	private static final double EPS = 1e-9;

	@Test
	void euToFe_defaultRatio_ceilAndFloor()
	{
		assertEquals(2.0, EnergyBridgeMath.DEFAULT_FE_PER_EU, EPS);

		// integer EU: ceil and floor agree
		assertEquals(2L, EnergyBridgeMath.euToFeCeil(1.0));
		assertEquals(2L, EnergyBridgeMath.euToFeFloor(1.0));
		assertEquals(10L, EnergyBridgeMath.euToFeCeil(5.0));
		assertEquals(10L, EnergyBridgeMath.euToFeFloor(5.0));

		// fractional EU: ceil rounds up FE request (send path), floor is conservative
		assertEquals(1L, EnergyBridgeMath.euToFeCeil(0.1));   // 0.2 → ceil 1
		assertEquals(0L, EnergyBridgeMath.euToFeFloor(0.1));   // 0.2 → floor 0
		assertEquals(3L, EnergyBridgeMath.euToFeCeil(1.1));    // 2.2 → ceil 3
		assertEquals(2L, EnergyBridgeMath.euToFeFloor(1.1));   // 2.2 → floor 2

		// custom ratio (e.g. hypothetical 4 FE/EU) does not affect default path
		assertEquals(4L, EnergyBridgeMath.euToFeCeil(1.0, 4.0));
		assertEquals(2L, EnergyBridgeMath.euToFeCeil(1.0)); // still default 2.0
	}

	@Test
	void feToEu_defaultRatio_andRoundTrip()
	{
		assertEquals(1.0, EnergyBridgeMath.feToEu(2L), EPS);
		assertEquals(0.5, EnergyBridgeMath.feToEu(1L), EPS);
		assertEquals(5.0, EnergyBridgeMath.feToEu(10L), EPS);

		// 1 EU → 2 FE → 1 EU
		long fe = EnergyBridgeMath.euToFeCeil(1.0);
		assertEquals(1.0, EnergyBridgeMath.feToEu(fe), EPS);

		// odd FE leaves fractional EU
		assertEquals(1.5, EnergyBridgeMath.feToEu(3L), EPS);

		// custom ratio
		assertEquals(1.0, EnergyBridgeMath.feToEu(4L, 4.0), EPS);
	}

	@Test
	void residual_simulatePartialAccept()
	{
		// FE residual: offered 100, accepted 40 → 60 left
		assertEquals(60L, EnergyBridgeMath.residualFe(100L, 40L));
		// full accept
		assertEquals(0L, EnergyBridgeMath.residualFe(100L, 100L));
		// over-report transferred is clamped to 0 residual
		assertEquals(0L, EnergyBridgeMath.residualFe(50L, 80L));
		// none accepted → full residual
		assertEquals(100L, EnergyBridgeMath.residualFe(100L, 0L));

		// EU residual after FE path (AE2-style): offer 10 EU → request 20 FE, accept 10 FE → 5 EU left
		double leftover = EnergyBridgeMath.residualEuAfterFeTransfer(10.0, 10L);
		assertEquals(5.0, leftover, EPS);

		// full FE accept for ceil-requested amount
		long feReq = EnergyBridgeMath.euToFeCeil(10.0); // 20
		assertEquals(0.0, EnergyBridgeMath.residualEuAfterFeTransfer(10.0, feReq), EPS);

		// simulate reject (0 FE accepted) → full EU residual
		assertEquals(7.5, EnergyBridgeMath.residualEuAfterFeTransfer(7.5, 0L), EPS);
	}

	@Test
	void boundary_zeroAndInvalid()
	{
		assertEquals(0L, EnergyBridgeMath.euToFeCeil(0.0));
		assertEquals(0L, EnergyBridgeMath.euToFeFloor(0.0));
		assertEquals(0L, EnergyBridgeMath.euToFeCeil(-1.0));
		assertEquals(0L, EnergyBridgeMath.euToFeFloor(-3.5));
		assertEquals(0.0, EnergyBridgeMath.feToEu(0L), EPS);
		assertEquals(0.0, EnergyBridgeMath.feToEu(-5L), EPS);

		assertEquals(0L, EnergyBridgeMath.residualFe(0L, 0L));
		assertEquals(0L, EnergyBridgeMath.residualFe(0L, 10L));
		assertEquals(0.0, EnergyBridgeMath.residualEuAfterFeTransfer(0.0, 10L), EPS);
		assertEquals(0.0, EnergyBridgeMath.residualEuAfterFeTransfer(-1.0, 10L), EPS);

		// invalid ratios
		assertFalse(EnergyBridgeMath.isValidRatio(0.0));
		assertFalse(EnergyBridgeMath.isValidRatio(-2.0));
		assertFalse(EnergyBridgeMath.isValidRatio(Double.NaN));
		assertFalse(EnergyBridgeMath.isValidRatio(Double.POSITIVE_INFINITY));
		assertTrue(EnergyBridgeMath.isValidRatio(EnergyBridgeMath.DEFAULT_FE_PER_EU));

		assertEquals(0L, EnergyBridgeMath.euToFeCeil(5.0, 0.0));
		assertEquals(0L, EnergyBridgeMath.euToFeFloor(5.0, -1.0));
		assertEquals(0.0, EnergyBridgeMath.feToEu(10L, 0.0), EPS);
		assertEquals(5.0, EnergyBridgeMath.residualEuAfterFeTransfer(5.0, 10L, 0.0), EPS);

		// int clamp for IEnergyStorage
		assertEquals(0, EnergyBridgeMath.clampToIntEnergy(0L));
		assertEquals(0, EnergyBridgeMath.clampToIntEnergy(-1L));
		assertEquals(42, EnergyBridgeMath.clampToIntEnergy(42L));
		assertEquals(Integer.MAX_VALUE, EnergyBridgeMath.clampToIntEnergy(Integer.MAX_VALUE + 1L));
	}
}
