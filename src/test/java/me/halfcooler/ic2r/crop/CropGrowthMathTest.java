package me.halfcooler.ic2r.crop;

import me.halfcooler.ic2r.core.crop.CropGrowthMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic crop growth / storage / cross eligibility (G3.9 / §8.3).
 * No Level / TE / CropCard; mirrors {@link me.halfcooler.ic2r.core.crop.TileEntityCrop}.
 */
class CropGrowthMathTest
{

	@Test
	void baseGrowth_addsRandomAndStat()
	{
		assertEquals(3, CropGrowthMath.baseGrowth(0, 0));
		assertEquals(19, CropGrowthMath.baseGrowth(10, 6));
	}

	@Test
	void minimumQuality_clampsNegativeAndUsesTierStats()
	{
		assertEquals(0, CropGrowthMath.minimumQuality(1, 0, 0, 0));
		assertEquals(17, CropGrowthMath.minimumQuality(3, 5, 3, 1));
		assertEquals(0, CropGrowthMath.minimumQuality(0, 0, 0, 0));
	}

	@Test
	void scaleWeightInfluences_multipliesByFive()
	{
		assertEquals(0, CropGrowthMath.scaleWeightInfluences(0));
		assertEquals(25, CropGrowthMath.scaleWeightInfluences(5));
	}


	@Test
	void totalGrowth_whenQualitySufficient_scalesWithSurplus()
	{
		assertEquals(10, CropGrowthMath.totalGrowthWhenSufficient(10, 20, 20));
		assertEquals(15, CropGrowthMath.totalGrowthWhenSufficient(10, 70, 20));
		assertTrue(CropGrowthMath.isQualitySufficient(20, 20));
		assertFalse(CropGrowthMath.isQualitySufficient(19, 20));
	}


	@Test
	void qualityDeficit_andResetThreshold()
	{
		assertEquals(40, CropGrowthMath.qualityDeficitAux(50, 40));
		assertEquals(120, CropGrowthMath.qualityDeficitAux(50, 20));

		assertFalse(CropGrowthMath.shouldResetFromDeficit(100, 0, 31));
		assertTrue(CropGrowthMath.shouldResetFromDeficit(120, 10, 11));
		assertFalse(CropGrowthMath.shouldResetFromDeficit(120, 10, 10));
	}

	@Test
	void totalGrowth_whenDeficient_clampsToZero()
	{
		assertEquals(6, CropGrowthMath.totalGrowthWhenDeficient(10, 40));
		assertEquals(0, CropGrowthMath.totalGrowthWhenDeficient(10, 100));
		assertEquals(0, CropGrowthMath.totalGrowthWhenDeficient(10, 150));
	}


	@Test
	void addGrowthPoints_andReadyToAgeUp()
	{
		assertEquals(15, CropGrowthMath.addGrowthPoints((short) 10, 5));
		assertEquals(0, CropGrowthMath.addGrowthPoints((short) 0, 0));
		assertFalse(CropGrowthMath.readyToAgeUp(99, 100));
		assertTrue(CropGrowthMath.readyToAgeUp(100, 100));
		assertTrue(CropGrowthMath.readyToAgeUp(101, 100));
	}


	@Test
	void crossEligibilityBase_thresholdsAndPass()
	{
		assertEquals(4, CropGrowthMath.crossEligibilityBase(0, 0));
		assertEquals(5, CropGrowthMath.crossEligibilityBase(16, 0));
		assertEquals(6, CropGrowthMath.crossEligibilityBase(30, 0));
		assertEquals(3, CropGrowthMath.crossEligibilityBase(0, 28));
		assertEquals(0, CropGrowthMath.crossEligibilityBase(0, 31));

		assertTrue(CropGrowthMath.passesCrossRoll(4, 4));
		assertTrue(CropGrowthMath.passesCrossRoll(4, 0));
		assertFalse(CropGrowthMath.passesCrossRoll(4, 5));
	}


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
		assertEquals(40, CropGrowthMath.acceptFixedDose(50, 40, 100));
		assertEquals(0, CropGrowthMath.acceptFixedDose(50, 50, 100));
		assertEquals(0, CropGrowthMath.acceptFixedDose(50, 60, 100));
	}
}
