package me.halfcooler.ic2r.core.block.inherit;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Ic2rSheetBlock extends Block
{
    public static final com.mojang.serialization.MapCodec<Ic2rSheetBlock> CODEC = simpleCodec(Ic2rSheetBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.Block> codec() {
        return CODEC;
    }

	private static final VoxelShape aabb = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.125, 1.0);
	private static final Direction[] positiveHorizontalFacings = new Direction[] { Direction.EAST, Direction.SOUTH };

	public Ic2rSheetBlock(Properties settings)
	{
		super(settings);
	}

	private static boolean canSupportWeight(Level world, BlockPos pos)
	{
		int maxRange = 16;
		MutableBlockPos cPos = new MutableBlockPos();

		for (Direction axis : positiveHorizontalFacings)
		{
			for (int dir = -1; dir <= 1; dir += 2)
			{
				cPos.set(pos);
				boolean supported = false;

				for (int i = 0; i < 16; i++)
				{
					cPos.move(axis, dir);
					BlockState state = world.getBlockState(cPos);
					if (state.isCollisionShapeFullBlock(world, cPos))
					{
						supported = true;
						break;
					}

					if (state != Ic2rBlocks.RUBBER_SHEET.get().defaultBlockState())
					{
						break;
					}

					cPos.move(Direction.DOWN);
					BlockState baseState = world.getBlockState(cPos);
					if (baseState.isCollisionShapeFullBlock(world, cPos))
					{
						supported = true;
						break;
					}

					cPos.move(Direction.UP);
				}

				if (!supported)
				{
					break;
				}

				if (dir == 1)
				{
					return true;
				}
			}
		}

		return false;
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		Level world = ctx.getLevel();
		if (world == null)
		{
			return null;
		}

		BlockPos pos = ctx.getClickedPos();
		BlockState state = this.defaultBlockState();
		return this.isValidPosition(world, pos, state) ? state : null;
	}

	public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context)
	{
		return aabb;
	}

	public @NotNull VoxelShape getCollisionShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context)
	{
		if (state.getBlock() == Ic2rBlocks.RESIN_SHEET.get())
		{
			return Shapes.empty();
		} else
		{
			return !(state.getBlock() == Ic2rBlocks.WOOL_SHEET.get() && context instanceof EntityCollisionContext entityContext)
				|| !(entityContext.getEntity() instanceof Player)
				|| !entityContext.getEntity().isShiftKeyDown()
				&& !(entityContext.getEntity().getY() < pos.getY() + 0.125 - entityContext.getEntity().maxUpStep())
				? aabb
				: Shapes.empty();
		}
	}

	private boolean isValidPosition(Level world, BlockPos pos, BlockState state)
	{
		if (state.getBlock() == Ic2rBlocks.RESIN_SHEET.get())
		{
			return this.isNormalCubeBelow(world, pos);
		}

		if (state.getBlock() != Ic2rBlocks.RUBBER_SHEET.get())
		{
			return state.getBlock() == Ic2rBlocks.WOOL_SHEET.get();
		}

		for (Direction facing : Util.HORIZONTAL_DIRS)
		{
			state = world.getBlockState(pos.relative(facing));
			if (state == Ic2rBlocks.RUBBER_SHEET.get().defaultBlockState() || state.isCollisionShapeFullBlock(world, pos))
			{
				return true;
			}
		}

		return this.isNormalCubeBelow(world, pos);
	}

	private boolean isNormalCubeBelow(Level world, BlockPos pos)
	{
		pos = pos.below();
		BlockState state = world.getBlockState(pos);
		return state.isCollisionShapeFullBlock(world, pos);
	}

	public void neighborChanged(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean notify)
	{
		if (!this.isValidPosition(world, pos, state))
		{
			Block.dropResources(state, world, pos, null);
			world.removeBlock(pos, false);
		}
	}

	public void entityInside(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Entity entity)
	{
		if (state.getBlock() == Ic2rBlocks.RESIN_SHEET.get())
		{
			entity.fallDistance = (float) (entity.fallDistance * 0.75);
			entity.setDeltaMovement(entity.getDeltaMovement().x() * 0.6, entity.getDeltaMovement().y() * 0.85, entity.getDeltaMovement().z() * 0.6);
		} else if (state.getBlock() == Ic2rBlocks.RUBBER_SHEET.get())
		{
			if (!world.isEmptyBlock(pos.below()))
			{
				return;
			}

			if (entity instanceof LivingEntity && !canSupportWeight(world, pos))
			{
				world.levelEvent(2001, pos, Block.getId(state));
				world.removeBlock(pos, false);
				return;
			}

			if (entity.getDeltaMovement().y() <= -0.4)
			{
				entity.fallDistance = 0.0F;
				entity.setDeltaMovement(entity.getDeltaMovement().x() * 1.1, entity.getDeltaMovement().y(), entity.getDeltaMovement().z() * 1.1);
				if (entity instanceof LivingEntity)
				{
					if (entity instanceof Player && IC2R.keyboard.isJumpKeyDown((Player) entity))
					{
						entity.setDeltaMovement(entity.getDeltaMovement().x(), entity.getDeltaMovement().y() * -1.3, entity.getDeltaMovement().z());
					} else if (entity instanceof Player && entity.isShiftKeyDown())
					{
						entity.setDeltaMovement(entity.getDeltaMovement().x(), entity.getDeltaMovement().y() * -0.1, entity.getDeltaMovement().z());
					} else
					{
						entity.setDeltaMovement(entity.getDeltaMovement().x(), entity.getDeltaMovement().y() * -0.8, entity.getDeltaMovement().z());
					}
				} else
				{
					entity.setDeltaMovement(entity.getDeltaMovement().x(), entity.getDeltaMovement().y() * -0.8, entity.getDeltaMovement().z());
				}
			} else if (state.getBlock() == Ic2rBlocks.WOOL_SHEET.get())
			{
				entity.fallDistance = (float) (entity.fallDistance * 0.95);
			}
		}
	}
}
