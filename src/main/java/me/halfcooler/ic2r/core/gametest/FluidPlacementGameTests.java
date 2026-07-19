package me.halfcooler.ic2r.core.gametest;

import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Ensures IC2R fluid sources are not overwritten by neighboring water
 * (BaseFlowingFluid's default water-like displacement), and that gases rise
 * upward without liquid-style side spread.
 */
@GameTestHolder("ic2r")
@PrefixGameTestTemplate(false)
public final class FluidPlacementGameTests
{
	private static final String TEMPLATE = "gametest/empty3x9x3";
	/** Center column of empty3x9x3 (width 3, height 9, depth 3). */
	private static final BlockPos FLUID_POS = new BlockPos(1, 2, 1);
	private static final BlockPos WATER_ABOVE = new BlockPos(1, 3, 1);
	private static final BlockPos WATER_SIDE = new BlockPos(2, 2, 1);

	private FluidPlacementGameTests()
	{
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 40)
	public static void fluidSourceSurvivesWaterAboveAndBeside(GameTestHelper helper)
	{
		BlockState fluidBlock = Ic2rFluids.COOLANT.still().defaultFluidState().createLegacyBlock();
		helper.setBlock(FLUID_POS, fluidBlock);
		helper.setBlock(WATER_ABOVE, Blocks.WATER.defaultBlockState());
		helper.setBlock(WATER_SIDE, Blocks.WATER.defaultBlockState());

		// Allow water fluid ticks (tick delay ~5) to attempt displacement.
		helper.runAfterDelay(20, () -> {
			FluidState remaining = helper.getLevel().getFluidState(helper.absolutePos(FLUID_POS));
			helper.assertTrue(
				remaining.getType().isSame(Ic2rFluids.COOLANT.still()),
				"coolant source must not be replaced by adjacent water (got " + remaining + ")"
			);
			helper.assertTrue(remaining.isSource(), "coolant must remain a source block");
			helper.succeed();
		});
	}

	/** Steam rises (tick delay 5); after ~25 ticks it should have left y=1 for higher y. */
	@GameTest(template = TEMPLATE, timeoutTicks = 80)
	public static void steamRisesWithoutSideSpread(GameTestHelper helper)
	{
		assertGasRisesWithoutSideSpread(helper, Ic2rFluids.STEAM.still(), 30);
	}

	/** Hydrogen rises every tick; after a few ticks it should be well above the start. */
	@GameTest(template = TEMPLATE, timeoutTicks = 40)
	public static void hydrogenRisesWithoutSideSpread(GameTestHelper helper)
	{
		assertGasRisesWithoutSideSpread(helper, Ic2rFluids.HYDROGEN.still(), 8);
	}

	/** Compressed air is slow (tick delay 20); still rises, and never spreads sideways. */
	@GameTest(template = TEMPLATE, timeoutTicks = 100)
	public static void airRisesWithoutSideSpread(GameTestHelper helper)
	{
		assertGasRisesWithoutSideSpread(helper, Ic2rFluids.AIR.still(), 45);
	}

	private static void assertGasRisesWithoutSideSpread(GameTestHelper helper, Fluid gas, int delayTicks)
	{
		BlockPos start = new BlockPos(1, 1, 1);
		BlockPos side = new BlockPos(2, 1, 1);
		helper.setBlock(start, gas.defaultFluidState().createLegacyBlock());

		helper.runAfterDelay(delayTicks, () -> {
			FluidState atStart = helper.getLevel().getFluidState(helper.absolutePos(start));
			helper.assertTrue(
				atStart.isEmpty() || !atStart.getType().isSame(gas),
				"gas must leave the start block by rising (still " + atStart + ")"
			);

			boolean foundAbove = false;
			for (int y = 2; y < 9; y++)
			{
				FluidState above = helper.getLevel().getFluidState(helper.absolutePos(new BlockPos(1, y, 1)));
				if (above.getType().isSame(gas) && above.isSource())
				{
					foundAbove = true;
					break;
				}
			}
			helper.assertTrue(foundAbove, "gas source must be somewhere above the start column");

			FluidState sideState = helper.getLevel().getFluidState(helper.absolutePos(side));
			helper.assertTrue(
				sideState.isEmpty() || !sideState.getType().isSame(gas),
				"gas must not spread horizontally like a liquid"
			);
			helper.succeed();
		});
	}
}
