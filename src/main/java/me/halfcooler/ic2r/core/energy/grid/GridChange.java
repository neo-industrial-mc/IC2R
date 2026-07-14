package me.halfcooler.ic2r.core.energy.grid;

import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;

import java.util.List;

import net.minecraft.core.BlockPos;

class GridChange
{
	final GridChange.Type type;
	final BlockPos pos;
	final IEnergyTile ioTile;
	List<IEnergyTile> subTiles;

	GridChange(GridChange.Type type, BlockPos pos, IEnergyTile ioTile)
	{
		this.type = type;
		this.pos = pos;
		this.ioTile = ioTile;
	}

	enum Type
	{
		ADDITION,
		REMOVAL
	}
}
