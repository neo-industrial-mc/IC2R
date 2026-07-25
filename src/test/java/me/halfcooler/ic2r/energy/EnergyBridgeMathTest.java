package me.halfcooler.ic2r.energy;

import me.halfcooler.ic2r.core.energy.EnergyBridgeMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * G2.8 pure EU↔FE conversion tests (no NeoForge capability / Level).
 */
class EnergyBridgeMathTest
{
	private static final double EPS = 1e-9;

	@Test
	void euToFe_defaultRatio_ceilAndFloor()
	{
		assertEquals(2.0, EnergyBridgeMath.DEFAULT_FE_PER_EU, EPS);

		assertEquals(2L, EnergyBridgeMath.euToFeCeil(1.0));
		assertEquals(2L, EnergyBridgeMath.euToFeFloor(1.0));
		assertEquals(10L, EnergyBridgeMath.euToFeCeil(5.0));
		assertEquals(10L, EnergyBridgeMath.euToFeFloor(5.0));

		assertEquals(1L, EnergyBridgeMath.euToFeCeil(0.1));
		assertEquals(0L, EnergyBridgeMath.euToFeFloor(0.1));
		assertEquals(3L, EnergyBridgeMath.euToFeCeil(1.1));
		assertEquals(2L, EnergyBridgeMath.euToFeFloor(1.1));

		assertEquals(4L, EnergyBridgeMath.euToFeCeil(1.0, 4.0));
		assertEquals(2L, EnergyBridgeMath.euToFeCeil(1.0));
	}

	@Test
	void feToEu_defaultRatio_andRoundTrip()
	{
		assertEquals(1.0, EnergyBridgeMath.feToEu(2L), EPS);
		assertEquals(0.5, EnergyBridgeMath.feToEu(1L), EPS);
		assertEquals(5.0, EnergyBridgeMath.feToEu(10L), EPS);

		long fe = EnergyBridgeMath.euToFeCeil(1.0);
		assertEquals(1.0, EnergyBridgeMath.feToEu(fe), EPS);

		assertEquals(1.5, EnergyBridgeMath.feToEu(3L), EPS);

		assertEquals(1.0, EnergyBridgeMath.feToEu(4L, 4.0), EPS);
	}

	@Test
	void residual_simulatePartialAccept()
	{
		assertEquals(60L, EnergyBridgeMath.residualFe(100L, 40L));
		assertEquals(0L, EnergyBridgeMath.residualFe(100L, 100L));
		assertEquals(0L, EnergyBridgeMath.residualFe(50L, 80L));
		assertEquals(100L, EnergyBridgeMath.residualFe(100L, 0L));

		double leftover = EnergyBridgeMath.residualEuAfterFeTransfer(10.0, 10L);
		assertEquals(5.0, leftover, EPS);

		long feReq = EnergyBridgeMath.euToFeCeil(10.0);
		assertEquals(0.0, EnergyBridgeMath.residualEuAfterFeTransfer(10.0, feReq), EPS);

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

		assertFalse(EnergyBridgeMath.isValidRatio(0.0));
		assertFalse(EnergyBridgeMath.isValidRatio(-2.0));
		assertFalse(EnergyBridgeMath.isValidRatio(Double.NaN));
		assertFalse(EnergyBridgeMath.isValidRatio(Double.POSITIVE_INFINITY));
		assertTrue(EnergyBridgeMath.isValidRatio(EnergyBridgeMath.DEFAULT_FE_PER_EU));

		assertEquals(0L, EnergyBridgeMath.euToFeCeil(5.0, 0.0));
		assertEquals(0L, EnergyBridgeMath.euToFeFloor(5.0, -1.0));
		assertEquals(0.0, EnergyBridgeMath.feToEu(10L, 0.0), EPS);
		assertEquals(5.0, EnergyBridgeMath.residualEuAfterFeTransfer(5.0, 10L, 0.0), EPS);

		assertEquals(0, EnergyBridgeMath.clampToIntEnergy(0L));
		assertEquals(0, EnergyBridgeMath.clampToIntEnergy(-1L));
		assertEquals(42, EnergyBridgeMath.clampToIntEnergy(42L));
		assertEquals(Integer.MAX_VALUE, EnergyBridgeMath.clampToIntEnergy(Integer.MAX_VALUE + 1L));
	}

	/**
	 * G3.3: huge finite EU×ratio saturates to Long.MAX_VALUE (ceil/floor); residual never negative.
	 * Non-finite product (Infinity) is rejected as 0 — never wraps to negative FE.
	 */
	@Test
	void euToFe_overflowSaturates_andPartialAcceptResidue()
	{
		assertEquals(Long.MAX_VALUE, EnergyBridgeMath.euToFeCeil(5e18, 4.0));
		assertEquals(Long.MAX_VALUE, EnergyBridgeMath.euToFeFloor(5e18, 4.0));

		assertEquals(0L, EnergyBridgeMath.euToFeCeil(Double.MAX_VALUE, 4.0));
		assertEquals(0L, EnergyBridgeMath.euToFeFloor(Double.MAX_VALUE, 4.0));

		assertEquals(1L, EnergyBridgeMath.euToFeCeil(1e-9));

		assertEquals(75.0, EnergyBridgeMath.residualEuAfterFeTransfer(100.0, 50L), EPS);
		assertEquals(0.0, EnergyBridgeMath.residualEuAfterFeTransfer(1.0, 100L), EPS);
	}

	/**
	 * EU→FE converter contracts: push uses floor for offer, residual for spend;
	 * FE→EU mapping exists for extract accounting but receive path must not reverse-create EU.
	 */
	@Test
	void euToFeConverter_pushAccounting_floorAndResidual()
	{
		double maxEu = 2048.0;
		long maxFe = EnergyBridgeMath.euToFeFloor(maxEu);
		assertEquals(4096L, maxFe);

		long accepted = maxFe / 2;
		double leftover = EnergyBridgeMath.residualEuAfterFeTransfer(maxEu, accepted);
		assertEquals(1024.0, leftover, EPS);

		assertEquals(10.0, EnergyBridgeMath.feToEu(20L), EPS);
		assertEquals(9.5, EnergyBridgeMath.feToEu(19L), EPS);
	}
}
