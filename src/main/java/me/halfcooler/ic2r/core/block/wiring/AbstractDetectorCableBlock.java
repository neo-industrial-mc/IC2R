package me.halfcooler.ic2r.core.block.wiring;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.NodeStats;
import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDetectorCableBlock extends AbstractCableBlock
{
	public static final BooleanProperty active = BooleanProperty.create("active");
	private static final int tickRate = 32;

	protected AbstractDetectorCableBlock(Properties settings)
	{
		super(settings, CableType.detector, 0);
		this.registerDefaultState(this.defaultBlockState().setValue(active, false));
	}

	@Override
	protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(active);
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify)
	{
		super.onPlace(state, world, pos, oldState, notify);
		world.scheduleTick(pos, this, world.getRandom().nextInt(32));
	}

	public void tick(@NotNull BlockState state, @NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource random)
	{
		this.tickFoamHardening(state, world, pos, random);
		state = world.getBlockState(pos);
		world.scheduleTick(pos, this, 32);
		IEnergyTile tile = EnergyNet.instance.getTile(world, pos);
		if (tile != null)
		{
			NodeStats stats = EnergyNet.instance.getNodeStats(tile);
			if (stats != null)
			{
				boolean newActive = stats.getEnergyIn() > 0.0;
				if (newActive != state.getValue(active))
				{
					world.setBlockAndUpdate(pos, state.setValue(active, newActive));
				} else if (newActive)
				{
					world.updateNeighbourForOutputSignal(pos, this);
				}
			}
		}
	}

	public boolean isSignalSource(@NotNull BlockState state)
	{
		return true;
	}

	public int getSignal(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull Direction direction)
	{
		return state.getValue(active) ? 15 : 0;
	}

	public boolean hasAnalogOutputSignal(@NotNull BlockState state)
	{
		return true;
	}

	public int getAnalogOutputSignal(BlockState state, @NotNull Level world, @NotNull BlockPos pos)
	{
		if (!(Boolean) state.getValue(active))
		{
			return 0;
		}

		IEnergyTile tile = EnergyNet.instance.getTile(world, pos);
		return tile == null ? 0 : (int) Util.map(EnergyNet.instance.getNodeStats(tile).getEnergyIn() / this.type.capacity, 1.0, 15.0);
	}
}
