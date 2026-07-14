package me.halfcooler.ic2r.api.energy.event;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;
import net.minecraftforge.event.level.LevelEvent;

public class EnergyTileEvent extends LevelEvent
{
	public final IEnergyTile tile;

	public EnergyTileEvent(IEnergyTile tile)
	{
		super(EnergyNet.instance.getWorld(tile));
		if (this.getLevel() == null)
		{
			throw new NullPointerException("world is null");
		}

		this.tile = tile;
	}
}
