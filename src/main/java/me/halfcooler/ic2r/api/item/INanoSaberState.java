package me.halfcooler.ic2r.api.item;

public interface INanoSaberState
{
	boolean isActive();

	void setActive(boolean active);

	int getEnergyTick();

	void setEnergyTick(int energyTick);
}