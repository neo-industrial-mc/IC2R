package me.halfcooler.ic2r.core.block.misc;

import me.halfcooler.ic2r.core.Ic2rDamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.jetbrains.annotations.NotNull;

public class HydrogenBlock extends LiquidBlock
{
	public HydrogenBlock(FlowingFluid fluid, Properties properties)
	{
		super(fluid, properties);
	}

	private static void checkFireAndExplode(Level world, BlockPos pos)
	{
		for (int dx = -1; dx <= 1; dx++)
		{
			for (int dy = -1; dy <= 1; dy++)
			{
				for (int dz = -1; dz <= 1; dz++)
				{
					if (dx == 0 && dy == 0 && dz == 0)
					{
						continue;
					}

					BlockPos neighborPos = pos.offset(dx, dy, dz);
					if (world.getBlockState(neighborPos).is(Blocks.FIRE))
					{
						// Remove fluid first so the source block is gone before the blast.
						world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
						if (!world.isClientSide && world instanceof ServerLevel serverLevel)
						{
							double x = pos.getX() + 0.5;
							double y = pos.getY() + 0.5;
							double z = pos.getZ() + 0.5;
							// Custom damage type so advancements can detect hydrogen blasts specifically.
							serverLevel.explode(
								null,
								Ic2rDamageSource.hydrogenExplosion(serverLevel),
								null,
								x,
								y,
								z,
								2.0F,
								false,
								Level.ExplosionInteraction.BLOCK
							);
						}
						return;
					}
				}
			}
		}
	}

	@Override
	public void entityInside(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Entity entity)
	{
	}

	@Override
	public void onPlace(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston)
	{
		if (state.getValue(LiquidBlock.LEVEL) == 0)
		{
			checkFireAndExplode(world, pos);
		}
	}

	@Override
	public void neighborChanged(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston)
	{
		if (state.getValue(LiquidBlock.LEVEL) == 0)
		{
			checkFireAndExplode(world, pos);
		}
	}
}
