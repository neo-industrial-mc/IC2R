package me.halfcooler.ic2r.core.block.storage.tank;

import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySteelTank extends TileEntityTank
{
	public TileEntitySteelTank(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.STEEL_TANK, pos, state, 128);
	}
}
