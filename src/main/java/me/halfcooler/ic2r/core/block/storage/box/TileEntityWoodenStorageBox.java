package me.halfcooler.ic2r.core.block.storage.box;

import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityWoodenStorageBox extends TileEntityStorageBox
{
	public TileEntityWoodenStorageBox(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.WOODEN_STORAGE_BOX, pos, state, 27);
	}
}
