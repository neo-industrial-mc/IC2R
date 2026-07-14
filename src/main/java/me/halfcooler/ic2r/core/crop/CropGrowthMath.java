package me.halfcooler.ic2r.core.crop;

/**
 * Pure crop growth / storage / cross-eligibility arithmetic extracted from
 * {@link TileEntityCrop} (G3.9 / §8.3 巨型 BE 切片).
 * <p>
 * No Level / CropCard / BlockEntity — unit-testable. Wired back into production
 * so tests pin live behavior (same pattern as {@code StandardMachineCycleMath}).
 */
public final class CropGrowthMath
{
	/** Matches {@link TileEntityCrop#applyHydration} hard cap. */
	public static final int WATER_STORAGE_MAX = 200;

	/** Auto WeedEX fill cap ({@code applyWeedEx(..., manual=false)}). */
	public static final int WEED_EX_AUTO_MAX = 150;

	/** Manual WeedEX fill cap ({@code applyWeedEx(..., manual=true)}). */
	public static final int WEED_EX_MANUAL_MAX = 100;

	private CropGrowthMath()
	{
	}

	/**
	 * Base growth points candidate for one growth tick:
	 * {@code 3 + random0Inclusive6 + statGrowth} where {@code random0Inclusive6}
	 * is the result of {@code nextInt(7)}.
	 */
	public static int baseGrowth(int statGrowth, int random0Inclusive6)
	{
		return 3 + random0Inclusive6 + statGrowth;
	}

	/**
	 * Minimum terrain quality required for full growth:
	 * {@code max(0, (tier - 1) * 4 + growth + gain + resistance)}.
	 */
	public static int minimumQuality(int cropTier, int statGrowth, int statGain, int statResistance)
	{
		return Math.max((cropTier - 1) * 4 + statGrowth + statGain + statResistance, 0);
	}

	/**
	 * Scales crop weight-influences sum to quality units ({@code * 5}),
	 * mirroring {@code getWeightInfluences(...) * 5}.
	 */
	public static int scaleWeightInfluences(int weightInfluencesSum)
	{
		return weightInfluencesSum * 5;
	}

	/** {@code providedQuality >= minimumQuality}. */
	public static boolean isQualitySufficient(int providedQuality, int minimumQuality)
	{
		return providedQuality >= minimumQuality;
	}

	/**
	 * Growth when quality meets or exceeds minimum:
	 * {@code baseGrowth * (100 + (provided - minimum)) / 100}.
	 */
	public static int totalGrowthWhenSufficient(int baseGrowth, int providedQuality, int minimumQuality)
	{
		return baseGrowth * (100 + (providedQuality - minimumQuality)) / 100;
	}

	/**
	 * Quality deficit auxiliary used when {@code provided < minimum}:
	 * {@code (minimum - provided) * 4}.
	 */
	public static int qualityDeficitAux(int minimumQuality, int providedQuality)
	{
		return (minimumQuality - providedQuality) * 4;
	}

	/**
	 * Whether the crop dies from poor conditions:
	 * {@code aux > 100 && random0to31 > statResistance}
	 * ({@code random0to31} = {@code nextInt(32)}).
	 * Caller must only sample RNG when {@code aux > 100} to preserve stream parity.
	 */
	public static boolean shouldResetFromDeficit(int aux, int statResistance, int random0to31)
	{
		return aux > 100 && random0to31 > statResistance;
	}

	/**
	 * Growth under quality deficit (no reset):
	 * {@code max(0, baseGrowth * (100 - aux) / 100)}.
	 */
	public static int totalGrowthWhenDeficient(int baseGrowth, int aux)
	{
		return Math.max(baseGrowth * (100 - aux) / 100, 0);
	}

	/** Accumulates growth points: {@code (short) (current + totalGrowth)}. */
	public static short addGrowthPoints(short growthPoints, int totalGrowth)
	{
		return (short) (growthPoints + totalGrowth);
	}

	/** Age advances when accumulated points reach crop growth duration. */
	public static boolean readyToAgeUp(int growthPoints, int growthDuration)
	{
		return growthPoints >= growthDuration;
	}

	/**
	 * Neighbor cross / spread eligibility base before {@code nextInt(16)} roll.
	 * Mirrors {@code checkCrossingAvailability} / {@code attemptSpreading}.
	 */
	public static int crossEligibilityBase(int statGrowth, int statResistance)
	{
		int base = 4;
		if (statGrowth >= 16)
		{
			base++;
		}

		if (statGrowth >= 30)
		{
			base++;
		}

		if (statResistance >= 28)
		{
			base += 27 - statResistance;
		}

		return base;
	}

	/**
	 * Crossing availability: {@code base >= random0to15}
	 * ({@code random0to15} = {@code nextInt(16)}).
	 */
	public static boolean passesCrossRoll(int base, int random0to15)
	{
		return base >= random0to15;
	}

	/**
	 * Amount accepted into a capped storage (water / non-fixed WeedEX).
	 * Returns 0 when full; otherwise {@code min(amount, capacity - current)}.
	 */
	public static int acceptIntoStorage(int current, int amount, int capacity)
	{
		int space = capacity - current;
		if (space <= 0)
		{
			return 0;
		}

		return Math.min(amount, space);
	}

	/**
	 * Fixed-dose WeedEX: accepts full {@code amount} only when
	 * {@code capacity - current > amount}; else 0.
	 */
	public static int acceptFixedDose(int current, int amount, int capacity)
	{
		int space = capacity - current;
		if (space <= amount)
		{
			return 0;
		}

		return amount;
	}

	/** WeedEX capacity for manual vs auto application. */
	public static int weedExCapacity(boolean manual)
	{
		return manual ? WEED_EX_MANUAL_MAX : WEED_EX_AUTO_MAX;
	}
}
