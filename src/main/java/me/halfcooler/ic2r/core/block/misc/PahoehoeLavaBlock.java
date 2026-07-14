package me.halfcooler.ic2r.core.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.jetbrains.annotations.NotNull;

public class PahoehoeLavaBlock extends LiquidBlock
{
	public PahoehoeLavaBlock(FlowingFluid fluid, Block.Properties properties)
	{
		super(fluid, properties);
	}

	private static boolean isTouchingWater(LevelAccessor world, BlockPos pos)
	{
		for (Direction dir : Direction.values())
		{
			if (world.getFluidState(pos.relative(dir)).is(FluidTags.WATER))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void onPlace(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston)
	{
		if (state.getValue(LiquidBlock.LEVEL) == 0)
		{
			world.scheduleTick(pos, this, 360);
		}
	}

	@Override
	public void tick(BlockState state, @NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource random)
	{
		if (state.getValue(LiquidBlock.LEVEL) == 0)
		{
			world.setBlockAndUpdate(pos, Blocks.BASALT.defaultBlockState());
		}
	}

	@Override
	public void neighborChanged(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston)
	{
		super.neighborChanged(state, world, pos, neighborBlock, neighborPos, movedByPiston);
		if (state.getValue(LiquidBlock.LEVEL) == 0 && isTouchingWater(world, pos))
		{
			world.setBlockAndUpdate(pos, Blocks.BASALT.defaultBlockState());
		}
	}

	@Override
	public void entityInside(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Entity entity)
	{
		if (!world.isClientSide)
		{
			entity.hurt(world.damageSources().lava(), 4.0F);
			entity.setSecondsOnFire(30);
		}
	}
}
