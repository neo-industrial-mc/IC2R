package ic2.api.energy.profile;

public interface ICableSpec
{
	VoltageTier getMaxVoltage();

	int getMaxAmperage();

	int getLossPerMeterPerAmp();
}