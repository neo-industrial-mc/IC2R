package me.halfcooler.ic2r.core.block.storage.box;

import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityBronzeStorageBox extends TileEntityStorageBox
{
	public TileEntityBronzeStorageBox(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.BRONZE_STORAGE_BOX, pos, state, 45);
	}
}
