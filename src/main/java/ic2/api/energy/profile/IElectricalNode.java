package ic2.api.energy.profile;

public interface IElectricalNode
{
	VoltageTier getWorkingVoltage();

	int getWorkingCurrent();

	double getAverageCurrent();

	int getMaxSourceAmperage();

	int getMaxSinkAmperage();

	double getEnergyBufferCapacity();

	double getEnergyBufferFree();
}