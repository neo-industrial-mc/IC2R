package me.halfcooler.ic2r.api.energy.profile;

public interface IElectricalNode
{
	VoltageTier getWorkingVoltage();

	default VoltageTier getSinkWorkingVoltage()
	{
		return this.getWorkingVoltage();
	}

	int getWorkingCurrent();

	double getAverageCurrent();

	int getMaxSourceAmperage();

	int getMaxSinkAmperage();

	double getEnergyBufferCapacity();

	double getEnergyBufferFree();
}