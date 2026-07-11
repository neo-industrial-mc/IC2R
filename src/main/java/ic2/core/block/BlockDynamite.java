package ic2.core.block;

import ic2.core.entity.StickyDynamiteEntity;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockDynamite extends Block
{
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final BooleanProperty LINKED = BooleanProperty.create("linked");

	private static final VoxelShape FLOOR = Block.box(6.0, 0.0, 6.0, 10.0, 10.0, 10.0);
	private static final VoxelShape NORTH = Block.box(5.0, 3.0, 11.0, 11.0, 13.0, 16.0);
	private static final VoxelShape SOUTH = Block.box(5.0, 3.0, 0.0, 11.0, 13.0, 5.0);
	private static final VoxelShape WEST = Block.box(11.0, 3.0, 5.0, 16.0, 13.0, 11.0);
	private static final VoxelShape EAST = Block.box(0.0, 3.0, 5.0, 5.0, 13.0, 11.0);

	public BlockDynamite()
	{
		super(Properties.of()
			.mapColor(MapColor.FIRE)
			.strength(0.0F)
			.sound(SoundType.GRASS)
			.noCollission()
			.instabreak()
			.noLootTable()
			.pushReaction(PushReaction.DESTROY));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(LINKED, false));
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, LINKED);
	}

	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return switch (state.getValue(FACING))
		{
			case NORTH -> NORTH;
			case SOUTH -> SOUTH;
			case WEST -> WEST;
			case EAST -> EAST;
			default -> FLOOR;
		};
	}

	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
	{
		Direction facing = state.getValue(FACING);
		if (facing == Direction.DOWN)
		{
			return false;
		}

		BlockPos supportPos = pos.relative(facing.getOpposite());
		return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
	}

	public boolean canPlaceDynamite(LevelReader level, BlockPos pos)
	{
		for (Direction dir : FACING.getPossibleValues())
		{
			if (dir != Direction.DOWN && this.canSurvive(this.defaultBlockState().setValue(FACING, dir), level, pos))
			{
				return true;
			}
		}

		return false;
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction clicked = context.getClickedFace();

		if (clicked != Direction.DOWN && this.canSurvive(this.defaultBlockState().setValue(FACING, clicked), level, pos))
		{
			return this.defaultBlockState().setValue(FACING, clicked);
		}

		for (Direction dir : FACING.getPossibleValues())
		{
			if (dir != Direction.DOWN && this.canSurvive(this.defaultBlockState().setValue(FACING, dir), level, pos))
			{
				return this.defaultBlockState().setValue(FACING, dir);
			}
		}

		return null;
	}

	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
	{
		this.checkPlacement(level, pos, state);
	}

	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
	{
		this.checkPlacement(level, pos, state);
	}

	public BlockState updateShape(
		BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos
	)
	{
		if (state.getValue(FACING).getOpposite() == direction && !state.canSurvive(level, pos))
		{
			// noLootTable: destroyBlock would not drop — refund the stick like classic IC2.
			if (level instanceof Level realLevel && !realLevel.isClientSide)
			{
				Block.popResource(realLevel, pos, new ItemStack(Ic2Items.DYNAMITE));
			}

			return Blocks.AIR.defaultBlockState();
		}

		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		if (!oldState.is(state.getBlock()))
		{
			this.checkPlacement(level, pos, state);
		}
	}

	@Override
	public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion)
	{
		// Chain reaction: arm with a short fuse instead of vanishing.
		if (!level.isClientSide && !level.getBlockState(pos).isAir())
		{
			LivingEntity igniter = explosion.getIndirectSourceEntity();
			this.explode(level, pos, igniter, true);
		}
	}

	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, net.minecraft.world.level.material.FluidState fluid)
	{
		// Classic IC2: breaking a placed stick always arms it (creative included).
		this.explode(level, pos, player, false);
		return true;
	}

	private void checkPlacement(Level level, BlockPos pos, BlockState state)
	{
		if (level.isClientSide)
		{
			return;
		}

		if (level.hasNeighborSignal(pos))
		{
			this.explode(level, pos, null, false);
		}
		else if (!this.canSurvive(state, level, pos) && level.getBlockState(pos).is(this))
		{
			// Path when shape updates did not already clear the stick (classic: drop the item).
			level.removeBlock(pos, false);
			Block.popResource(level, pos, new ItemStack(Ic2Items.DYNAMITE));
		}
	}

	public void explode(Level level, BlockPos pos, @Nullable LivingEntity igniter, boolean byExplosion)
	{
		level.removeBlock(pos, false);
		if (!level.isClientSide)
		{
			StickyDynamiteEntity entity = new StickyDynamiteEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
			entity.owner = igniter;
			entity.fuse = byExplosion ? 5 : 40;
			level.addFreshEntity(entity);
		}

		level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	/**
	 * Detonate a linked dynamite stick (used by remote detonator).
	 */
	public void detonate(Level level, BlockPos pos, @Nullable LivingEntity igniter)
	{
		if (!level.isClientSide)
		{
			this.explode(level, pos, igniter, false);
		}
	}

	public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state)
	{
		return new ItemStack(Ic2Items.DYNAMITE);
	}

	@Override
	public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion)
	{
		// Primed by explosion chain reaction; never drop the stick as an item.
		return false;
	}

	@Override
	public boolean dropFromExplosion(Explosion explosion)
	{
		return false;
	}
}
