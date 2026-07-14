package me.halfcooler.ic2r.core.energy;

public enum EnergyNetMode
{
	IC2R,
	GT;

	public static EnergyNetMode fromConfig(String value)
	{
		return "GT".equalsIgnoreCase(value) ? GT : IC2R;
	}
}