package me.halfcooler.ic2r.api.energy.event;

import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;

public class EnergyTileLoadEvent extends EnergyTileEvent
{
	public EnergyTileLoadEvent(IEnergyTile energyTile1)
	{
		super(energyTile1);
	}
}
