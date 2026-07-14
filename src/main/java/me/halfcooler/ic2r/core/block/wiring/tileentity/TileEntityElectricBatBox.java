package me.halfcooler.ic2r.core.block.wiring.tileentity;

import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityElectricBatBox extends TileEntityElectricBlock
{
	public TileEntityElectricBatBox(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.BATBOX, pos, state, 1, 32, 40000);
	}
}
