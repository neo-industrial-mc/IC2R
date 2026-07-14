package me.halfcooler.ic2r.core.fluid;

/**
 * Pure fill/empty arithmetic for {@link Ic2rFluidTank} (and later unified fluid services).
 * No Minecraft/Forge types — unit-testable without client or registry bootstrap (W2.2 / FL-001).
 */
public final class FluidTransferMath
{
	private FluidTransferMath()
	{
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
		if (storedMb <= 0 || drainedMb <= 0)
		{
			return Math.max(0, storedMb);
		}

		return Math.max(0, storedMb - drainedMb);
	}
}
