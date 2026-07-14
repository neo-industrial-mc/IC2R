package me.halfcooler.ic2r.core.block.inherit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class Ic2rGlassBlock extends AbstractGlassBlock
{
	public Ic2rGlassBlock(Properties settings)
	{
		super(settings);
	}

	public @NotNull VoxelShape getBlockSupportShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos)
	{
		return Shapes.empty();
	}
}
