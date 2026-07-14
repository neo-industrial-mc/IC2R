package me.halfcooler.ic2r.api.energy.event;

import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;

public class EnergyTileUnloadEvent extends EnergyTileEvent
{
	public EnergyTileUnloadEvent(IEnergyTile energyTile1)
	{
		super(energyTile1);
	}
}
