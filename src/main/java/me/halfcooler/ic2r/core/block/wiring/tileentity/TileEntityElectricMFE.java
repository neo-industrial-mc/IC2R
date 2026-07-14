package me.halfcooler.ic2r.core.block.wiring.tileentity;

import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityElectricMFE extends TileEntityElectricBlock
{
	public TileEntityElectricMFE(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.MFE, pos, state, 3, 512, 4000000);
	}
}
