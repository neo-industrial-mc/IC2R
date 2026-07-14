package me.halfcooler.ic2r.core.block.wiring;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSplitterCableBlock extends AbstractCableBlock
{
	public static final BooleanProperty active = BooleanProperty.create("active");

	protected AbstractSplitterCableBlock(Properties settings)
	{
		super(settings, CableType.splitter, 0);
		this.registerDefaultState(this.defaultBlockState().setValue(active, false));
	}

	@Override
	protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(active);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		return super.getStateForPlacement(ctx).setValue(active, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
	}

	@Override
	public void neighborChanged(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean notify)
	{
		if (!world.isClientSide)
		{
			boolean newActive = world.hasNeighborSignal(pos);
			if (state.getValue(active) != newActive)
			{
				state = state.setValue(active, newActive);
				world.setBlockAndUpdate(pos, state);
				if (newActive)
				{
					this.addToEnet(state, world, pos, true);
				} else
				{
					this.removeFromEnet(state, world, pos);
				}
			}
		}

		super.neighborChanged(state, world, pos, block, fromPos, notify);
	}

	@Override
	protected void addToEnet(BlockState state, Level world, BlockPos pos, boolean checkConflicting)
	{
		if (state.getValue(active))
		{
			super.addToEnet(state, world, pos, checkConflicting);
		}
	}
}
