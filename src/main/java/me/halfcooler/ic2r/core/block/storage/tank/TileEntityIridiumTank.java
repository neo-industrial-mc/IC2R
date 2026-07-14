package me.halfcooler.ic2r.core.block.storage.tank;

import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIridiumTank extends TileEntityTank
{
	public TileEntityIridiumTank(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.IRIDIUM_TANK, pos, state, 1024);
	}
}
