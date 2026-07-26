package ic2.core.energy;

/**
 * Pure conversion formulas for bridging EU to FE-shaped energy systems (Forge Energy, AE2).
 *
 * <p>All methods are defensive: non-finite or non-positive inputs yield the identity ("nothing
 * transferred") result instead of propagating NaN or negative energy into a grid.
 */
public final class EnergyBridgeMath {

  public static final double DEFAULT_FE_PER_EU = 2.0;

  private EnergyBridgeMath() {}

  public static boolean isValidRatio(double fePerEu) {
    return Double.isFinite(fePerEu) && fePerEu > 0.0;
  }

  public static long euToFeCeil(double eu, double fePerEu) {
    if (eu <= 0.0 || !isValidRatio(fePerEu)) {
      return 0L;
    }

    double fe = eu * fePerEu;
    if (!Double.isFinite(fe) || fe <= 0.0) {
      return 0L;
    }
    if (fe >= (double) Long.MAX_VALUE) {
      return Long.MAX_VALUE;
    }

    return (long) Math.ceil(fe);
  }

  public static long euToFeCeil(double eu) {
    return euToFeCeil(eu, DEFAULT_FE_PER_EU);
  }

  public static long euToFeFloor(double eu, double fePerEu) {
    if (eu <= 0.0 || !isValidRatio(fePerEu)) {
      return 0L;
    }

    double fe = eu * fePerEu;
    if (!Double.isFinite(fe) || fe <= 0.0) {
      return 0L;
    }
    if (fe >= (double) Long.MAX_VALUE) {
      return Long.MAX_VALUE;
    }

    return (long) Math.floor(fe);
  }

  public static long euToFeFloor(double eu) {
    return euToFeFloor(eu, DEFAULT_FE_PER_EU);
  }

  public static double feToEu(long fe, double fePerEu) {
    if (fe <= 0L || !isValidRatio(fePerEu)) {
      return 0.0;
    }

    return fe / fePerEu;
  }

  public static double feToEu(long fe) {
    return feToEu(fe, DEFAULT_FE_PER_EU);
  }

  public static long residualFe(long offeredFe, long transferredFe) {
    if (offeredFe <= 0L) {
      return 0L;
    }
    if (transferredFe <= 0L) {
      return offeredFe;
    }

    long residual = offeredFe - transferredFe;
    return Math.max(residual, 0L);
  }

  /** EU still owed to the emitter after the FE side accepted {@code feAccepted}. */
  public static double residualEuAfterFeTransfer(double euOffer, long feAccepted, double fePerEu) {
    if (euOffer <= 0.0) {
      return 0.0;
    }
    if (feAccepted <= 0L || !isValidRatio(fePerEu)) {
      return euOffer;
    }

    double euAccepted = Math.min(feToEu(feAccepted, fePerEu), euOffer);
    return Math.max(euOffer - euAccepted, 0.0);
  }

  public static double residualEuAfterFeTransfer(double euOffer, long feAccepted) {
    return residualEuAfterFeTransfer(euOffer, feAccepted, DEFAULT_FE_PER_EU);
  }

  public static int clampToIntEnergy(long amount) {
    if (amount <= 0L) {
      return 0;
    }
    if (amount >= Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }

    return (int) amount;
  }
}
