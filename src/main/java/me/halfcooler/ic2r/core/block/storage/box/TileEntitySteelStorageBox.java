package me.halfcooler.ic2r.core.block.storage.box;

import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySteelStorageBox extends TileEntityStorageBox
{
	public TileEntitySteelStorageBox(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.STEEL_STORAGE_BOX, pos, state, 63);
	}
}
