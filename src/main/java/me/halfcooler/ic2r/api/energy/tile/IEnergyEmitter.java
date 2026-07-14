package me.halfcooler.ic2r.api.energy.tile;

import net.minecraft.core.Direction;

public interface IEnergyEmitter extends IEnergyTile
{
	boolean emitsEnergyTo(IEnergyAcceptor var1, Direction var2);
}
