package me.halfcooler.ic2r.core.fluid;

public final class FluidTransferMath
{
	private FluidTransferMath()
	{
	}

	/**
	 * Whether tank content is compatible with an offered fluid (empty tank always accepts).
	 * Mirrors {@link Ic2rFluidTank} fillMb {@code fluidsCompatible}.
	 */
	public static boolean tankFluidsCompatible(boolean tankEmptyOrNull, boolean sameFluidAsOffer)
	{
		return tankEmptyOrNull || sameFluidAsOffer;
	}

	/**
	 * Whether fill access is allowed for an offer.
	 * Mirrors: {@code !toFill.isEmpty() && (!external || canFill(fluid))}.
	 */
	public static boolean fillAccessAllowed(boolean offerEmpty, boolean external, boolean canFill)
	{
		return !offerEmpty && (!external || canFill);
	}

	/**
	 * Whether amount-based drain access is allowed.
	 * Mirrors: {@code !external || canDrain()}.
	 */
	public static boolean drainAccessAllowed(boolean external, boolean canDrain)
	{
		return !external || canDrain;
	}

	/**
	 * Whether stack-based drain may proceed (fluids must match + drain access).
	 * Mirrors {@link Ic2rFluidTank} drainMb(stack) composition of {@code fluidsMatch} and access.
	 */
	public static boolean drainByStackAccessAllowed(
		boolean tankEmptyOrNull,
		boolean requestEmpty,
		boolean sameFluid,
		boolean external,
		boolean canDrain
	)
	{
		boolean fluidsMatch = !tankEmptyOrNull && !requestEmpty && sameFluid;
		return fluidsMatch && drainAccessAllowed(external, canDrain);
	}

	/**
	 * How many mB can be filled into a tank.
	 *
	 * @param capacity         tank capacity in mB
	 * @param storedMb         current stored amount (0 if empty)
	 * @param offerMb          offered fill amount
	 * @param fluidsCompatible true if tank empty or same fluid as offer
	 * @param canFill          external/access gate (e.g. {@code canFill(fluid)})
	 * @return amount that would be accepted (0..offerMb)
	 */
	public static int fillableMb(int capacity, int storedMb, int offerMb, boolean fluidsCompatible, boolean canFill)
	{
		if (offerMb <= 0 || capacity <= 0 || storedMb < 0 || !fluidsCompatible || !canFill)
		{
			return 0;
		}

		int space = capacity - storedMb;
		if (space <= 0)
		{
			return 0;
		}

		return Math.min(space, offerMb);
	}

	/**
	 * How many mB can be drained from a tank.
	 *
	 * @param storedMb  current stored amount
	 * @param requestMb requested drain amount
	 * @param canDrain  external/access gate (e.g. {@code canDrain()})
	 * @return amount that would be drained
	 */
	public static int drainableMb(int storedMb, int requestMb, boolean canDrain)
	{
		if (storedMb <= 0 || requestMb <= 0 || !canDrain)
		{
			return 0;
		}

		return Math.min(storedMb, requestMb);
	}

	/**
	 * Remaining offer after accepting {@code filledMb} from {@code offerMb}.
	 */
	public static int remainingOfferAfterFill(int offerMb, int filledMb)
	{
		if (offerMb <= 0 || filledMb <= 0)
		{
			return Math.max(0, offerMb);
		}

		return Math.max(0, offerMb - filledMb);
	}

	/**
	 * Tank stored amount after draining {@code drainedMb} from {@code storedMb}.
	 */
	public static int remainingStoredAfterDrain(int storedMb, int drainedMb)
	{
		return remainingOfferAfterFill(storedMb, drainedMb);
	}

	/**
	 * Stored amount after a fill commit (simulate leaves stored unchanged).
	 */
	public static int storedAfterFill(int storedMb, int filledMb, boolean simulate)
	{
		if (simulate || filledMb <= 0)
		{
			return Math.max(0, storedMb);
		}

		return Math.max(0, storedMb) + filledMb;
	}

	/**
	 * Stored amount after a drain commit (simulate leaves stored unchanged; full drain → 0).
	 */
	public static int storedAfterDrain(int storedMb, int drainedMb, boolean simulate)
	{
		if (simulate || drainedMb <= 0)
		{
			return Math.max(0, storedMb);
		}

		return remainingStoredAfterDrain(storedMb, drainedMb);
	}

	/**
	 * End-to-end fill amount for tank delegate (compat + access + capacity math).
	 */
	public static int fillMbDelegated(
		int capacity,
		int storedMb,
		int offerMb,
		boolean tankEmptyOrNull,
		boolean sameFluidAsOffer,
		boolean offerEmpty,
		boolean external,
		boolean canFill
	)
	{
		boolean compatible = tankFluidsCompatible(tankEmptyOrNull, sameFluidAsOffer);
		boolean access = fillAccessAllowed(offerEmpty, external, canFill);
		return fillableMb(capacity, storedMb, offerMb, compatible, access);
	}

	/**
	 * End-to-end amount drain for tank delegate (access + stored math).
	 */
	public static int drainMbDelegated(
		int storedMb,
		int requestMb,
		boolean external,
		boolean canDrain
	)
	{
		return drainableMb(storedMb, requestMb, drainAccessAllowed(external, canDrain));
	}

	/**
	 * End-to-end stack drain amount for tank delegate (match + access + stored math).
	 */
	public static int drainMbByStackDelegated(
		int storedMb,
		int requestMb,
		boolean tankEmptyOrNull,
		boolean requestEmpty,
		boolean sameFluid,
		boolean external,
		boolean canDrain
	)
	{
		boolean access = drainByStackAccessAllowed(
			tankEmptyOrNull,
			requestEmpty,
			sameFluid,
			external,
			canDrain
		);
		return drainableMb(storedMb, requestMb, access);
	}
}
