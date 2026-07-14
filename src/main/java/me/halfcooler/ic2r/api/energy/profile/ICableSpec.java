package me.halfcooler.ic2r.api.energy.profile;

public interface ICableSpec
{
	VoltageTier getMaxVoltage();

	int getMaxAmperage();

	int getLossPerMeterPerAmp();
}