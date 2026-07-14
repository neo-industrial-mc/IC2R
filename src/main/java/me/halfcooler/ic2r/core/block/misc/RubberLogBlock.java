package me.halfcooler.ic2r.core.block.misc;

import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class RubberLogBlock extends RotatedPillarBlock
{
	public static final EnumProperty<RubberLogBlock.RubberWoodState> stateProperty = EnumProperty.create("state", RubberLogBlock.RubberWoodState.class);

	public RubberLogBlock(Properties settings)
	{
		super(settings);
		this.registerDefaultState(this.defaultBlockState().setValue(stateProperty, RubberWoodState.plain));
	}

	protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(stateProperty);
	}

	public @NotNull BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		return this.defaultBlockState().setValue(AXIS, ctx.getClickedFace().getAxis()).setValue(stateProperty, RubberWoodState.plain);
	}

	public void randomTick(@NotNull BlockState state, @NotNull ServerLevel world, @NotNull BlockPos pos, RandomSource random)
	{
		if (random.nextInt(7) == 0)
		{
			RubberLogBlock.RubberWoodState rwState = state.getValue(stateProperty);
			if (!rwState.canRegenerate(world, pos))
			{
				return;
			}

			world.setBlockAndUpdate(pos, state.setValue(stateProperty, rwState.getWet()));
		}
	}

	@Override
	public void onRemove(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston)
	{
		if (!state.is(newState.getBlock()) && !world.isClientSide)
		{
			BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();

			for (int y = -4; y <= 4; y++)
			{
				for (int z = -4; z <= 4; z++)
				{
					for (int x = -4; x <= 4; x++)
					{
						cPos.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
						BlockState cState = world.getBlockState(cPos);
						if (cState.is(BlockTags.LEAVES))
						{
							world.scheduleTick(cPos, cState.getBlock(), 1);
						}
					}
				}
			}
		}

		super.onRemove(state, world, pos, newState, movedByPiston);
	}

	public PushReaction getPistonPushReaction(BlockState state)
	{
		RubberWoodState rwState = state.getValue(stateProperty);
		return rwState.isPlain() ? PushReaction.NORMAL : PushReaction.BLOCK;
	}

	private static boolean hasContactingLeaves(Level world, BlockPos pos)
	{
		BlockPos.MutableBlockPos top = new BlockPos.MutableBlockPos();
		top.set(pos);

		while (world.getBlockState(top).is(Ic2rBlocks.RUBBER_LOG))
		{
			BlockPos above = top.above();
			if (world.getBlockState(above).is(Ic2rBlocks.RUBBER_LOG))
			{
				top.set(above);
			} else
			{
				return world.getBlockState(above).is(BlockTags.LEAVES);
			}
		}

		return false;
	}

	public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit)
	{
		ItemStack mainHandItem = player.getMainHandItem();
		if (!(mainHandItem.getItem() instanceof AxeItem))
		{
			return super.use(state, world, pos, player, hand, hit);
		}

		WorldUtil.strip(state, world, pos, player, mainHandItem, Ic2rBlocks.STRIPPED_RUBBER_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
		RubberLogBlock.RubberWoodState resinState = state.getValue(stateProperty);
		if (resinState == RubberLogBlock.RubberWoodState.wet_north || resinState == RubberLogBlock.RubberWoodState.wet_south || resinState == RubberLogBlock.RubberWoodState.wet_west || resinState == RubberLogBlock.RubberWoodState.wet_east)
		{
			this.dropResin(world, pos, world.random.nextInt(2) + 1);
		}

		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	private void dropResin(Level world, BlockPos pos, int amount)
	{
		for (int i = 0; i < amount; i++)
		{
			ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Ic2rItems.RESIN));
			entity.setDefaultPickUpDelay();
			world.addFreshEntity(entity);
		}
	}

	public enum RubberWoodState implements StringRepresentable
	{
		plain(null, false),
		dry_north(Direction.NORTH, false),
		dry_south(Direction.SOUTH, false),
		dry_west(Direction.WEST, false),
		dry_east(Direction.EAST, false),
		wet_north(Direction.NORTH, true),
		wet_south(Direction.SOUTH, true),
		wet_west(Direction.WEST, true),
		wet_east(Direction.EAST, true);

		private static final RubberLogBlock.RubberWoodState[] values = values();
		public final Direction facing;
		public final boolean wet;

		RubberWoodState(Direction facing, boolean wet)
		{
			this.facing = facing;
			this.wet = wet;
		}

		public @NotNull String getSerializedName()
		{
			return this.name();
		}

		public boolean isPlain()
		{
			return this.facing == null;
		}

		public boolean canRegenerate(Level world, BlockPos pos)
		{
			return !this.isPlain() && !this.wet && hasContactingLeaves(world, pos);
		}

		public RubberLogBlock.RubberWoodState getWet()
		{
			if (this.isPlain())
			{
				return null;
			} else
			{
				return this.wet ? this : values[this.ordinal() + 4];
			}
		}

		public RubberLogBlock.RubberWoodState getDry()
		{
			return !this.isPlain() && this.wet ? values[this.ordinal() - 4] : this;
		}

		public static RubberWoodState getWet(Direction facing)
		{
			return switch (facing)
			{
				case NORTH -> wet_north;
				case SOUTH -> wet_south;
				case WEST -> wet_west;
				case EAST -> wet_east;
				default -> throw new IllegalArgumentException("incompatible facing: " + facing);
			};
		}
	}
}
