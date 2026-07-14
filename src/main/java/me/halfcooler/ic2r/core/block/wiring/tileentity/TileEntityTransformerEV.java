package me.halfcooler.ic2r.core.block.wiring.tileentity;

import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityTransformerEV extends TileEntityTransformer
{
	public TileEntityTransformerEV(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.EV_TRANSFORMER, pos, state, 4);
	}
}
