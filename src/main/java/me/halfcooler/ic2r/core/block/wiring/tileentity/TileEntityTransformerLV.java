package me.halfcooler.ic2r.core.block.wiring.tileentity;

import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityTransformerLV extends TileEntityTransformer
{
	public TileEntityTransformerLV(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.LV_TRANSFORMER, pos, state, 1);
	}
}
