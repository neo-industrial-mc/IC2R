package me.halfcooler.ic2r.api.energy.tile;

import java.util.List;

public interface IMetaDelegate extends IEnergyTile
{
	List<IEnergyTile> getSubTiles();
}
