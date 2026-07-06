package ic2.core.energy;

public enum EnergyNetMode
{
	IC2,
	GT;

	public static EnergyNetMode fromConfig(String value)
	{
		return "GT".equalsIgnoreCase(value) ? GT : IC2;
	}
}