package me.halfcooler.ic2r.core.block.wiring.tileentity;

import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityChargePadMFE extends TileEntityChargePadBlock
{
	public TileEntityChargePadMFE(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.MFE_CHARGEPAD, pos, state, 3, 512, 4000000);
	}
}
