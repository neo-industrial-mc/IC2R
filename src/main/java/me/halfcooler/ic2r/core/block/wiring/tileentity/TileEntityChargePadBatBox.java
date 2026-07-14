package me.halfcooler.ic2r.core.block.wiring.tileentity;

import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityChargePadBatBox extends TileEntityChargePadBlock
{
	public TileEntityChargePadBatBox(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.BATBOX_CHARGEPAD, pos, state, 1, 32, 40000);
	}
}
