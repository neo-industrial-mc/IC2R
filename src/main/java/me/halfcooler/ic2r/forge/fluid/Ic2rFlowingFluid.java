/*
 * Copyright (c) IC2R contributors
 */

package me.halfcooler.ic2r.forge.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/**
 * IC2R flowing fluids that are not swallowed by neighboring vanilla water/lava.
 *
 * <p>{@link BaseFlowingFluid} defaults {@code canBeReplacedWith} to water-like behavior:
 * any different fluid flowing {@link Direction#DOWN} may replace this fluid. In an ocean
 * that means water above immediately overwrites a placed IC2R source, turning it into water.
 *
 * <p>IC2R fluids keep their own cells and should coexist next to other fluids unless a
 * dedicated {@link net.neoforged.neoforge.fluids.FluidInteractionRegistry} interaction
 * converts them.
 */
public abstract class Ic2rFlowingFluid extends BaseFlowingFluid
{
	protected Ic2rFlowingFluid(Properties properties)
	{
		super(properties);
	}

	/**
	 * Never allow a foreign fluid to overwrite this fluid in-world.
	 * Source and flowing levels both refuse displacement so underwater placements survive.
	 */
	@Override
	protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluidIn, Direction direction)
	{
		return false;
	}

	public static class Flowing extends Ic2rFlowingFluid
	{
		public Flowing(Properties properties)
		{
			super(properties);
			registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
		}

		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
		{
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(FluidState state)
		{
			return state.getValue(LEVEL);
		}

		@Override
		public boolean isSource(FluidState state)
		{
			return false;
		}
	}

	public static class Source extends Ic2rFlowingFluid
	{
		public Source(Properties properties)
		{
			super(properties);
		}

		@Override
		public int getAmount(FluidState state)
		{
			return 8;
		}

		@Override
		public boolean isSource(FluidState state)
		{
			return true;
		}
	}
}
