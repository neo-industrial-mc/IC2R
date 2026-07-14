package me.halfcooler.ic2r.api.energy;

import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;

public interface IEnergyNetEventReceiver
{
	void onAdd(IEnergyTile var1);

	void onRemove(IEnergyTile var1);
}
