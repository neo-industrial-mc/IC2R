package me.halfcooler.ic2r.core.block.machine.tileentity;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;

import java.util.List;

public interface IWeightedDistributor extends Container
{
	Direction getFacing();

	List<Direction> getPriority();

	void updatePriority(boolean var1);
}
