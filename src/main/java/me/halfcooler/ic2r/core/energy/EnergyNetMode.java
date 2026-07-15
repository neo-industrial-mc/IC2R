package me.halfcooler.ic2r.core.energy;

public enum EnergyNetMode
{
	IC2R,
	GT;

	/**
	 * @param useGregTechEnergyNet {@code true} selects GT voltage/amp limits; {@code false} (default) classic IC2R packets.
	 */
	public static EnergyNetMode fromConfig(boolean useGregTechEnergyNet)
	{
		return useGregTechEnergyNet ? GT : IC2R;
	}
}