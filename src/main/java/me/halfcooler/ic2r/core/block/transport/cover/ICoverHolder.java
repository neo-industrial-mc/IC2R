package me.halfcooler.ic2r.core.block.transport.cover;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Set;

public interface ICoverHolder
{
	Set<CoverProperty> getCoverProperties();

	boolean canPlaceCover(Level var1, BlockPos var2, Direction var3, ItemStack var4);

	void placeCover(Level var1, BlockPos var2, Direction var3, ItemStack var4);

	boolean canRemoveCover(Level var1, BlockPos var2, Direction var3);

	void removeCover(Level var1, BlockPos var2, Direction var3);
}
