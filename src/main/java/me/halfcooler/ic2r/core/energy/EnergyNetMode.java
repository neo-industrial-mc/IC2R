package me.halfcooler.ic2r.core.energy;

/**
 * Runtime flag for GregTech-style V/A display and machine behaviour hooks in IC2R.
 * <p>
 * The base mod always uses the classic IC energy solver. Loading the optional
 * {@code ic2r_gt_addon} enables this flag and installs the GT calculator via
 * {@code EnergyNetGlobal.setCalculator(...)}.
 */
public enum EnergyNetMode
{
	IC2R,
	GT;

	private static volatile boolean gtModeEnabled;

	public static boolean isGtModeEnabled()
	{
		return gtModeEnabled;
	}

	/**
	 * Enable or disable GT energy-net mode. Intended for the GT addon to call from its
	 * mod constructor (together with installing the GT calculator).
	 */
	public static void setGtModeEnabled(boolean enabled)
	{
		gtModeEnabled = enabled;
	}

	public static EnergyNetMode current()
	{
		return gtModeEnabled ? GT : IC2R;
	}

	public static boolean isGt()
	{
		return gtModeEnabled;
	}

	/**
	 * @deprecated Use {@link #current()} / {@link #isGt()}. Config toggle was removed in favour of the GT addon.
	 */
	@Deprecated
	public static EnergyNetMode fromConfig(boolean useGregTechEnergyNet)
	{
		return useGregTechEnergyNet || gtModeEnabled ? GT : IC2R;
	}
}
