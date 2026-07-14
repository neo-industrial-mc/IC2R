package me.halfcooler.ic2r.core.block.wiring.tileentity;

import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityElectricMFSU extends TileEntityElectricBlock
{
	public TileEntityElectricMFSU(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.MFSU, pos, state, 4, 2048, 40000000);
	}
}
