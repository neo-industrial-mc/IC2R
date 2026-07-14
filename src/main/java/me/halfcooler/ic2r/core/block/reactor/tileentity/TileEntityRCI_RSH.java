package me.halfcooler.ic2r.core.block.reactor.tileentity;

import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityRCI_RSH extends TileEntityAbstractRCI
{
	public TileEntityRCI_RSH(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.RCI_RSH, pos, state, new ItemStack(Ic2rItems.RSH_CONDENSATOR), new ItemStack(Blocks.REDSTONE_BLOCK));
	}
}
