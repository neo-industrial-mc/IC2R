package me.halfcooler.ic2r.core.gametest;

import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Ensures IC2R fluid sources are not overwritten by neighboring water
 * (BaseFlowingFluid's default water-like displacement).
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
}
