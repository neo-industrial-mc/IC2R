/*
 * Copyright (c) IC2R contributors
 */

package me.halfcooler.ic2r.forge.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
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
 *
 * <p>Gases ({@code FluidType#isLighterThanAir()}) do not spread like liquids. The source
 * block stays put; each tick they place a fixed-height flowing cell upward (legacy level 1 /
 * amount {@link #GAS_FLOW_AMOUNT}) until a ceiling or the build-height limit. Flowing gas
 * without the same fluid below dissipates.
 */
public abstract class Ic2rFlowingFluid extends BaseFlowingFluid
{
	/**
	 * Flowing amount for rising gas (one step from a source amount of 8).
	 * Maps to {@link net.minecraft.world.level.block.LiquidBlock#LEVEL} = 1 — the same
	 * “flowed one block” appearance for every cell in the upward column.
	 */
	private static final int GAS_FLOW_AMOUNT = 7;

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

	/**
	 * Gases skip vanilla {@code getNewLiquid} collapse/horizontal logic. Source stays;
	 * unsupported flowing gas is cleared; then attempt to rise.
	 */
	@Override
	public void tick(Level level, BlockPos pos, FluidState state)
	{
		if (isGaseous())
		{
			if (!state.isSource())
			{
				FluidState below = level.getFluidState(pos.below());
				if (!below.getType().isSame(this))
				{
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
					return;
				}
				// Keep a uniform “flowed one block” look in the whole rising column.
				if (state.getAmount() != GAS_FLOW_AMOUNT || state.getValue(FALLING))
				{
					state = getFlowing(GAS_FLOW_AMOUNT, false);
					level.setBlock(pos, state.createLegacyBlock(), 2);
				}
			}
			spread(level, pos, state);
			return;
		}
		super.tick(level, pos, state);
	}

	/**
	 * Gases only extend upward as flowing fluid. The source is never moved or cleared.
	 */
	@Override
	protected void spread(Level level, BlockPos pos, FluidState state)
	{
		if (!isGaseous())
		{
			super.spread(level, pos, state);
			return;
		}
		if (state.isEmpty())
		{
			return;
		}

		BlockPos above = pos.above();
		// Build height is exclusive; gas stops at the highest placeable Y.
		if (above.getY() >= level.getMaxBuildHeight())
		{
			return;
		}

		BlockState selfState = level.getBlockState(pos);
		BlockState aboveState = level.getBlockState(above);
		FluidState aboveFluid = level.getFluidState(above);
		FluidState rising = getFlowing(GAS_FLOW_AMOUNT, false);

		if (canSpreadTo(level, pos, selfState, Direction.UP, above, aboveState, aboveFluid, rising.getType()))
		{
			// Source stays; only place a uniform flowing cell above.
			spreadTo(level, above, aboveState, Direction.UP, rising);
		}
	}

	private boolean isGaseous()
	{
		return getFluidType().isLighterThanAir();
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
