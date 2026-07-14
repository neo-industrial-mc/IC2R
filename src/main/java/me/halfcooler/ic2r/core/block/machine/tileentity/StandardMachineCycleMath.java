package me.halfcooler.ic2r.core.block.machine.tileentity;

/**
 * Pure / semi-pure standard-machine cycle arithmetic extracted from
 * {@link TileEntityStandardMachine} and {@link me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade}.
 * <p>
 * No Level / inventory / Energy component — unit-testable (G1.4 / SM-*).
 * Wired back into production so tests pin live behavior.
 */
public final class StandardMachineCycleMath
{
	private StandardMachineCycleMath()
	{
	}

	/**
	 * GUI fraction written each server tick: {@code progress / operationLength}.
	 * Spec SM-001 display side; mirrors {@code TileEntityStandardMachine.updateEntityServer}.
	 */
	public static float guiProgress(int progress, int operationLength)
	{
		if (operationLength <= 0)
		{
			return 0.0F;
		}

		return (float) progress / (float) operationLength;
	}

	/**
	 * Upgrade modifier used for energy demand / storage / tier.
	 * Mirrors {@code Process.applyModifier} / historical IC2 rounding.
	 */
	public static int applyModifier(int base, int extra, double multiplier)
	{
		double ret = Math.round(((double) base + extra) * multiplier);
		return ret > 2.147483647E9 ? Integer.MAX_VALUE : (int) ret;
	}

	/**
	 * Stack-scaled operation length intermediate used by overclock math.
	 * {@code (defaultLength + extraProcessTime) * 64 * processTimeMultiplier}.
	 */
	public static double stackOpLen(int defaultOperationLength, int extraProcessTime, double processTimeMultiplier)
	{
		return ((double) defaultOperationLength + extraProcessTime) * 64.0 * processTimeMultiplier;
	}

	/**
	 * Operations completed when one progress bar fills (after overclock).
	 * {@code min(ceil(64 / stackOpLen), Integer.MAX_VALUE)}; defaultLength 0 → 64.
	 */
	public static int operationsPerTick(int defaultOperationLength, int extraProcessTime, double processTimeMultiplier)
	{
		if (defaultOperationLength == 0)
		{
			return 64;
		}

		return opsPerTick(stackOpLen(defaultOperationLength, extraProcessTime, processTimeMultiplier));
	}

	/**
	 * Ticks of progress required for one operate cycle after overclock.
	 * defaultLength 0 → 1; otherwise {@code max(1, round(stackOpLen * opsPerTick / 64))}.
	 */
	public static int operationLength(int defaultOperationLength, int extraProcessTime, double processTimeMultiplier)
	{
		if (defaultOperationLength == 0)
		{
			return 1;
		}

		double stack = stackOpLen(defaultOperationLength, extraProcessTime, processTimeMultiplier);
		int ops = opsPerTick(stack);
		return Math.max(1, (int) Math.round(stack * ops / 64.0));
	}

	/**
	 * EU charged per progress tick after processing upgrades.
	 * Spec SM-002 / SM-006 energy side.
	 */
	public static int energyDemand(int defaultEnergyConsume, int extraEnergyDemand, double energyDemandMultiplier)
	{
		return applyModifier(defaultEnergyConsume, extraEnergyDemand, energyDemandMultiplier);
	}

	/**
	 * When overclock rates change, keep fractional progress continuous.
	 * {@code floor(previousProgressRatio * newLength + 0.1)} as short.
	 */
	public static short rescaleProgress(short progress, int previousOperationLength, int newOperationLength)
	{
		if (previousOperationLength <= 0 || newOperationLength <= 0)
		{
			return 0;
		}

		double previousRatio = (double) progress / previousOperationLength;
		return (short) Math.floor(previousRatio * newOperationLength + 0.1);
	}

	/**
	 * Whether the machine may run this tick (recipe path open and enough EU).
	 * {@code recipeReady} mirrors non-null {@code recipeResult}
	 * (input match + output can accept — see {@code getRecipeResult}).
	 */
	public static boolean canOperate(boolean recipeReady, double energyStored, int energyConsume)
	{
		return recipeReady && energyStored >= energyConsume;
	}

	/**
	 * One server-tick of standard-machine progress/energy (no inventory mutations).
	 * <p>
	 * Semantics frozen from {@link TileEntityStandardMachine#updateEntityServer}:
	 * <ul>
	 *   <li>SM-001: if can operate, progress += 1; at {@code operationLength} complete and reset to 0</li>
	 *   <li>SM-002: on operate, consume exactly {@code energyConsume} EU</li>
	 *   <li>SM-003: energy short → no progress advance; progress retained while recipe still ready</li>
	 *   <li>SM-004/005: {@code recipeReady == false} (output full / no input / no match) → no energy use;
	 *       progress forced to 0</li>
	 * </ul>
	 */
	public static CycleTickResult tick(
		short progress,
		int operationLength,
		int energyConsume,
		double energyStored,
		boolean recipeReady
	)
	{
		int length = Math.max(1, operationLength);
		int consume = Math.max(0, energyConsume);

		if (canOperate(recipeReady, energyStored, consume))
		{
			double remaining = energyStored - consume;
			int advanced = progress + 1;
			boolean completed = advanced >= length;
			short nextProgress = completed ? 0 : (short) advanced;
			return new CycleTickResult(nextProgress, remaining, true, completed, true);
		}

		// Cannot operate: energy interrupt keeps progress if recipe still ready; else clear.
		short nextProgress = recipeReady ? progress : 0;
		return new CycleTickResult(nextProgress, energyStored, false, false, false);
	}

	private static int opsPerTick(double stackOpLen)
	{
		if (stackOpLen <= 0.0)
		{
			return Integer.MAX_VALUE;
		}

		return (int) Math.min(Math.ceil(64.0 / stackOpLen), 2.147483647E9);
	}

	/**
	 * Immutable result of {@link #tick}.
	 *
	 * @param progress          progress after the tick (0..operationLength-1, or 0 after complete)
	 * @param energyStored      buffer after optional consume
	 * @param energyConsumed    true if {@code energyConsume} was deducted
	 * @param operationCompleted true if progress bar filled this tick (operate would run)
	 * @param shouldBeActive    mirrors TE wanting active while canOperate
	 */
	public record CycleTickResult(
		short progress,
		double energyStored,
		boolean energyConsumed,
		boolean operationCompleted,
		boolean shouldBeActive
	)
	{
	}
}
