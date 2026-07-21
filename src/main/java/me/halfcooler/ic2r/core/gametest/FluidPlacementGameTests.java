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

	/** Steam rises (tick delay 5); source stays, flowing appears above. */
	@GameTest(template = TEMPLATE, timeoutTicks = 80)
	public static void steamRisesWithoutSideSpread(GameTestHelper helper)
	{
		assertGasRisesWithoutSideSpread(helper, Ic2rFluids.STEAM.still(), 30);
	}

	/** Superheated steam same rise rules as steam. */
	@GameTest(template = TEMPLATE, timeoutTicks = 80)
	public static void superheatedSteamRisesWithoutSideSpread(GameTestHelper helper)
	{
		assertGasRisesWithoutSideSpread(helper, Ic2rFluids.SUPERHEATED_STEAM.still(), 30);
	}

	/** Hydrogen rises every tick; source stays, flowing appears above. */
	@GameTest(template = TEMPLATE, timeoutTicks = 40)
	public static void hydrogenRisesWithoutSideSpread(GameTestHelper helper)
	{
		assertGasRisesWithoutSideSpread(helper, Ic2rFluids.HYDROGEN.still(), 8);
	}

	/** Biogas is lighter-than-air; source stays, flowing rises. */
	@GameTest(template = TEMPLATE, timeoutTicks = 80)
	public static void biogasRisesWithoutSideSpread(GameTestHelper helper)
	{
		assertGasRisesWithoutSideSpread(helper, Ic2rFluids.BIOGAS.still(), 35);
	}

	/** Compressed air is slow (tick delay 20); still rises, and never spreads sideways. */
	@GameTest(template = TEMPLATE, timeoutTicks = 100)
	public static void airRisesWithoutSideSpread(GameTestHelper helper)
	{
		assertGasRisesWithoutSideSpread(helper, Ic2rFluids.AIR.still(), 45);
	}

	/**
	 * Source remains at the placement cell; flowing gas extends only upward with a uniform
	 * non-source amount (legacy level 1 / amount 7); no horizontal liquid-style spread.
	 */
	private static void assertGasRisesWithoutSideSpread(GameTestHelper helper, Fluid gas, int delayTicks)
	{
		BlockPos start = new BlockPos(1, 1, 1);
		BlockPos side = new BlockPos(2, 1, 1);
		helper.setBlock(start, gas.defaultFluidState().createLegacyBlock());

		helper.runAfterDelay(delayTicks, () -> {
			FluidState atStart = helper.getLevel().getFluidState(helper.absolutePos(start));
			helper.assertTrue(
				atStart.getType().isSame(gas) && atStart.isSource(),
				"gas source must stay at the start block (got " + atStart + ")"
			);

			boolean foundFlowingAbove = false;
			for (int y = 2; y < 9; y++)
			{
				FluidState above = helper.getLevel().getFluidState(helper.absolutePos(new BlockPos(1, y, 1)));
				if (above.getType().isSame(gas) && !above.isSource())
				{
					helper.assertTrue(
						above.getAmount() == 7,
						"rising gas must use uniform amount 7 (flowed-one-block look), got " + above.getAmount() + " at y=" + y
					);
					foundFlowingAbove = true;
				}
				else if (above.getType().isSame(gas) && above.isSource())
				{
					helper.fail("rising gas must be flowing, not a second source at y=" + y);
				}
			}
			helper.assertTrue(foundFlowingAbove, "flowing gas must appear somewhere above the source");

			FluidState sideState = helper.getLevel().getFluidState(helper.absolutePos(side));
			helper.assertTrue(
				sideState.isEmpty() || !sideState.getType().isSame(gas),
				"gas must not spread horizontally like a liquid"
			);
			helper.succeed();
		});
	}
}
