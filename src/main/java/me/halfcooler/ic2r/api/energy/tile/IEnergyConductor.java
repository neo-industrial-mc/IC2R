package me.halfcooler.ic2r.api.energy.tile;

public interface IEnergyConductor extends IEnergyAcceptor, IEnergyEmitter
{
	double getConductionLoss();

	double getInsulationEnergyAbsorption();

	double getInsulationBreakdownEnergy();

	double getConductorBreakdownEnergy();

	void removeInsulation();

	void removeConductor();
}
