package me.halfcooler.ic2r.core.energy;

/**
 * Pure EU ↔ external FE conversion for the energy bridge (G2.8 / §7.5).
 * No Minecraft/Forge types — unit-testable without client or capability bootstrap.
 * <p>
 * <strong>EU is authoritative</strong> inside IC2R ({@code core.energy}). FE/RF is platform
 * interop only; this class never talks to the EnergyNet topology.
 * <p>
 * Default ratio is {@link #DEFAULT_FE_PER_EU} = {@code 2.0} FE per 1 EU, chosen to match the
 * existing AE2 path ({@code Ic2rAe2Plugin.EU_TO_AE_RATIO}). Callers that need another rate pass
 * an explicit {@code fePerEu} (config switch reserved; not hard-wired here).
 *
 * @see me.halfcooler.ic2r.platform.services.PlatformEnergyBridge
 */
public final class EnergyBridgeMath
{
	/**
	 * Default external FE (and AE2 FE-fallback units) per 1 EU.
	 * Aligned with {@code Ic2rAe2Plugin.EU_TO_AE_RATIO}; do not change silently.
	 */
	public static final double DEFAULT_FE_PER_EU = 2.0;

	private EnergyBridgeMath()
	{
	}

	/** Whether {@code fePerEu} is finite and strictly positive. */
	public static boolean isValidRatio(double fePerEu)
	{
		return Double.isFinite(fePerEu) && fePerEu > 0.0;
	}

	/**
	 * Convert EU → FE using <strong>ceil</strong> (send/push path: never under-request FE for a
	 * positive EU packet). Mirrors AE2 {@code Math.ceil(eu * ratio)} before {@code receiveEnergy}.
	 * Non-positive EU or invalid ratio → {@code 0}.
	 */
	public static long euToFeCeil(double eu, double fePerEu)
	{
		if (eu <= 0.0 || !isValidRatio(fePerEu))
		{
			return 0L;
		}

		double fe = eu * fePerEu;
		if (!Double.isFinite(fe) || fe <= 0.0)
		{
			return 0L;
		}
		if (fe >= (double) Long.MAX_VALUE)
		{
			return Long.MAX_VALUE;
		}

		return (long) Math.ceil(fe);
	}

	/** {@link #euToFeCeil(double, double)} with {@link #DEFAULT_FE_PER_EU}. */
	public static long euToFeCeil(double eu)
	{
		return euToFeCeil(eu, DEFAULT_FE_PER_EU);
	}

	/**
	 * Convert EU → FE using <strong>floor</strong> (conservative store/display path).
	 * Non-positive EU or invalid ratio → {@code 0}.
	 */
	public static long euToFeFloor(double eu, double fePerEu)
	{
		if (eu <= 0.0 || !isValidRatio(fePerEu))
		{
			return 0L;
		}

		double fe = eu * fePerEu;
		if (!Double.isFinite(fe) || fe <= 0.0)
		{
			return 0L;
		}
		if (fe >= (double) Long.MAX_VALUE)
		{
			return Long.MAX_VALUE;
		}

		return (long) Math.floor(fe);
	}

	/** {@link #euToFeFloor(double, double)} with {@link #DEFAULT_FE_PER_EU}. */
	public static long euToFeFloor(double eu)
	{
		return euToFeFloor(eu, DEFAULT_FE_PER_EU);
	}

	/**
	 * Convert FE → EU (exact double division). Non-positive FE or invalid ratio → {@code 0}.
	 */
	public static double feToEu(long fe, double fePerEu)
	{
		if (fe <= 0L || !isValidRatio(fePerEu))
		{
			return 0.0;
		}

		return fe / fePerEu;
	}

	/** {@link #feToEu(long, double)} with {@link #DEFAULT_FE_PER_EU}. */
	public static double feToEu(long fe)
	{
		return feToEu(fe, DEFAULT_FE_PER_EU);
	}

	/**
	 * FE residual after a transfer attempt: {@code offered − transferred}, floored at 0.
	 * Used for simulate/execute leftover accounting in external units.
	 */
	public static long residualFe(long offeredFe, long transferredFe)
	{
		if (offeredFe <= 0L)
		{
			return 0L;
		}
		if (transferredFe <= 0L)
		{
			return offeredFe;
		}

		long residual = offeredFe - transferredFe;
		return residual <= 0L ? 0L : residual;
	}

	/**
	 * EU residual after pushing EU as FE and receiving a FE accept amount.
	 * Mirrors AE2 leftover: {@code euOffer - min(feAccepted / ratio, euOffer)}.
	 */
	public static double residualEuAfterFeTransfer(double euOffer, long feAccepted, double fePerEu)
	{
		if (euOffer <= 0.0)
		{
			return 0.0;
		}
		if (feAccepted <= 0L || !isValidRatio(fePerEu))
		{
			return euOffer;
		}

		double euAccepted = Math.min(feToEu(feAccepted, fePerEu), euOffer);
		double residual = euOffer - euAccepted;
		return residual <= 0.0 ? 0.0 : residual;
	}

	/** {@link #residualEuAfterFeTransfer(double, long, double)} with {@link #DEFAULT_FE_PER_EU}. */
	public static double residualEuAfterFeTransfer(double euOffer, long feAccepted)
	{
		return residualEuAfterFeTransfer(euOffer, feAccepted, DEFAULT_FE_PER_EU);
	}

	/**
	 * Clamp a long FE amount to {@code int} for Forge {@code IEnergyStorage} APIs.
	 * Non-positive → {@code 0}; above {@link Integer#MAX_VALUE} → {@code Integer.MAX_VALUE}.
	 */
	public static int clampToIntEnergy(long amount)
	{
		if (amount <= 0L)
		{
			return 0;
		}
		if (amount >= Integer.MAX_VALUE)
		{
			return Integer.MAX_VALUE;
		}

		return (int) amount;
	}
}
