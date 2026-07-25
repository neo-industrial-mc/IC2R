package me.halfcooler.ic2r.core.block.comp;

import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import net.minecraft.core.Direction;

import java.util.Set;

public class Kinetic extends TileEntityComponent
{
	private Set<Direction> sinkDirections;
	private Set<Direction> sourceDirections;

	public Kinetic(
		Ic2rTileEntity parent, double capacity, Set<Direction> sinkDirections, Set<Direction> sourceDirections, int sinkTier, int sourceTier, boolean fullEnergy
	)
	{
		super(parent);
	}
}
