package me.halfcooler.ic2r.crop;

import me.halfcooler.ic2r.core.crop.CropGrowthMath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-logic crop growth / storage / cross eligibility (G3.9 / §8.3).
 * No Level / TE / CropCard; mirrors {@link me.halfcooler.ic2r.core.crop.TileEntityCrop}.
 */
class CropGrowthMathTest
{
	// --- base / minimum / scale ---

	@Test
	void baseGrowth_addsRandomAndStat()
	{
		// 3 + nextInt(7)=0 + growth0 → 3; nextInt=6 + growth 10 → 19
		assertEquals(3, CropGrowthMath.baseGrowth(0, 0));
		assertEquals(19, CropGrowthMath.baseGrowth(10, 6));
	}

	@Test
	void minimumQuality_clampsNegativeAndUsesTierStats()
	{
		// tier1: (0)*4 + 0+0+0 → 0; tier3 + stats → (2)*4 + 5+3+1 = 17
		assertEquals(0, CropGrowthMath.minimumQuality(1, 0, 0, 0));
		assertEquals(17, CropGrowthMath.minimumQuality(3, 5, 3, 1));
		// tier 0 would be negative before clamp
		assertEquals(0, CropGrowthMath.minimumQuality(0, 0, 0, 0));
	}

	@Test
	void scaleWeightInfluences_multipliesByFive()
	{
		assertEquals(0, CropGrowthMath.scaleWeightInfluences(0));
		assertEquals(25, CropGrowthMath.scaleWeightInfluences(5));
	}

	// --- sufficient quality growth ---

	@Test
	void totalGrowth_whenQualitySufficient_scalesWithSurplus()
	{
		// base 10, provided == min → 10 * 100/100 = 10
		assertEquals(10, CropGrowthMath.totalGrowthWhenSufficient(10, 20, 20));
		// surplus 50 → 10 * 150/100 = 15
		assertEquals(15, CropGrowthMath.totalGrowthWhenSufficient(10, 70, 20));
		assertTrue(CropGrowthMath.isQualitySufficient(20, 20));
		assertFalse(CropGrowthMath.isQualitySufficient(19, 20));
	}

	// --- deficient quality / reset ---

	@Test
	void qualityDeficit_andResetThreshold()
	{
		// min 50, provided 40 → aux = 40
		assertEquals(40, CropGrowthMath.qualityDeficitAux(50, 40));
		// min 50, provided 20 → aux = 120 > 100
		assertEquals(120, CropGrowthMath.qualityDeficitAux(50, 20));

		// aux ≤ 100 never resets
		assertFalse(CropGrowthMath.shouldResetFromDeficit(100, 0, 31));
		// aux > 100 and roll > resistance → reset
		assertTrue(CropGrowthMath.shouldResetFromDeficit(120, 10, 11));
		// roll ≤ resistance → survive
		assertFalse(CropGrowthMath.shouldResetFromDeficit(120, 10, 10));
	}

	@Test
	void totalGrowth_whenDeficient_clampsToZero()
	{
		// base 10, aux 40 → 10 * 60/100 = 6
		assertEquals(6, CropGrowthMath.totalGrowthWhenDeficient(10, 40));
		// aux ≥ 100 → non-positive before clamp → 0
		assertEquals(0, CropGrowthMath.totalGrowthWhenDeficient(10, 100));
		assertEquals(0, CropGrowthMath.totalGrowthWhenDeficient(10, 150));
	}

	// --- accumulate / age ---

	@Test
	void addGrowthPoints_andReadyToAgeUp()
	{
		assertEquals(15, CropGrowthMath.addGrowthPoints((short) 10, 5));
		assertEquals(0, CropGrowthMath.addGrowthPoints((short) 0, 0));
		assertFalse(CropGrowthMath.readyToAgeUp(99, 100));
		assertTrue(CropGrowthMath.readyToAgeUp(100, 100));
		assertTrue(CropGrowthMath.readyToAgeUp(101, 100));
	}

	// --- cross eligibility ---

	@Test
	void crossEligibilityBase_thresholdsAndPass()
	{
		// base defaults 4
		assertEquals(4, CropGrowthMath.crossEligibilityBase(0, 0));
		// growth ≥ 16 → +1
		assertEquals(5, CropGrowthMath.crossEligibilityBase(16, 0));
		// growth ≥ 30 → +2 total from growth
		assertEquals(6, CropGrowthMath.crossEligibilityBase(30, 0));
		// resistance ≥ 28 → + (27 - resistance); res 28 → -1 net on base 4 = 3
		assertEquals(3, CropGrowthMath.crossEligibilityBase(0, 28));
		// res 31 → 4 + (27-31) = 0
		assertEquals(0, CropGrowthMath.crossEligibilityBase(0, 31));

		assertTrue(CropGrowthMath.passesCrossRoll(4, 4));
		assertTrue(CropGrowthMath.passesCrossRoll(4, 0));
		assertFalse(CropGrowthMath.passesCrossRoll(4, 5));
	}

	// --- storage ---

	@Test
	void acceptIntoStorage_respectsCapacity()
	{
		assertEquals(0, CropGrowthMath.acceptIntoStorage(200, 50, CropGrowthMath.WATER_STORAGE_MAX));
		assertEquals(50, CropGrowthMath.acceptIntoStorage(0, 50, CropGrowthMath.WATER_STORAGE_MAX));
		assertEquals(10, CropGrowthMath.acceptIntoStorage(190, 50, CropGrowthMath.WATER_STORAGE_MAX));
	}

	@Test
	void acceptFixedDose_andWeedExCapacity()
	{
		assertEquals(CropGrowthMath.WEED_EX_MANUAL_MAX, CropGrowthMath.weedExCapacity(true));
		assertEquals(CropGrowthMath.WEED_EX_AUTO_MAX, CropGrowthMath.weedExCapacity(false));
		// space 50, dose 40 → ok; dose 50 → space <= amount → 0
		assertEquals(40, CropGrowthMath.acceptFixedDose(50, 40, 100));
		assertEquals(0, CropGrowthMath.acceptFixedDose(50, 50, 100));
		assertEquals(0, CropGrowthMath.acceptFixedDose(50, 60, 100));
	}
}
