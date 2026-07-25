package me.halfcooler.ic2r.core.gametest;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMacerator;
import me.halfcooler.ic2r.core.block.wiring.AbstractSplitterCableBlock;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityElectricBatBox;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r")
@PrefixGameTestTemplate(false)
public final class SplitterCableGameTests
{
	private static final String TALL = "gametest/empty3x9x3";
	private static final BlockPos SPLITTER_POS = new BlockPos(1, 2, 1);
	private static final BlockPos FAR_MACERATOR_POS = new BlockPos(1, 1, 1);
	private static final BlockPos CABLE_POS = new BlockPos(1, 3, 1);
	private static final BlockPos NEAR_MACERATOR_POS = new BlockPos(0, 3, 1);
	private static final BlockPos BATBOX_POS = new BlockPos(1, 4, 1);
	private static final BlockPos REDSTONE_POS = new BlockPos(2, 2, 1);

	private SplitterCableGameTests()
	{
	}

	// a splitter cable conducts while powered; taking the redstone signal away removes it from
	// the energy net. That removal must only cut the splitter's own branch - sinks that don't
	// route through it keep receiving energy
	@GameTest(template = TALL, timeoutTicks = 120)
	public static void splitterDisconnectOnlyCutsItsOwnBranch(GameTestHelper helper)
	{
		// build the splitter branch first so the splitter's first grid link points at it and the
		// source side ends up in the component that splits off into a new grid
		helper.setBlock(SPLITTER_POS, Ic2rBlocks.SPLITTER_CABLE.get());
		helper.setBlock(FAR_MACERATOR_POS, Ic2rBlocks.MACERATOR.get());

		helper.runAtTickTime(5, () -> helper.setBlock(REDSTONE_POS, Blocks.REDSTONE_BLOCK));

		helper.runAtTickTime(10, () ->
		{
			helper.assertBlockProperty(SPLITTER_POS, AbstractSplitterCableBlock.active, true);
			helper.setBlock(CABLE_POS, Ic2rBlocks.COPPER_CABLE.get());
			helper.setBlock(NEAR_MACERATOR_POS, Ic2rBlocks.MACERATOR.get());
			helper.setBlock(BATBOX_POS, Ic2rBlocks.BATBOX.get());
		});

		helper.runAtTickTime(20, () -> helper.setBlock(REDSTONE_POS, Blocks.AIR));

		helper.runAtTickTime(30, () ->
		{
			helper.assertBlockProperty(SPLITTER_POS, AbstractSplitterCableBlock.active, false);
			TileEntityElectricBatBox batbox = helper.getBlockEntity(BATBOX_POS);
			batbox.energy.addEnergy(320.0);
		});

		helper.runAtTickTime(90, () ->
		{
			TileEntityMacerator nearMacerator = helper.getBlockEntity(NEAR_MACERATOR_POS);
			TileEntityMacerator farMacerator = helper.getBlockEntity(FAR_MACERATOR_POS);
			TileEntityElectricBatBox batbox = helper.getBlockEntity(BATBOX_POS);
			helper.assertTrue(
				nearMacerator.getEnergy() >= 300.0,
				"macerator on the source side of a disconnected splitter must keep receiving energy, has "
					+ nearMacerator.getEnergy());
			helper.assertTrue(
				farMacerator.getEnergy() == 0.0,
				"macerator behind the disconnected splitter must not receive energy, has " + farMacerator.getEnergy());
			helper.assertTrue(
				batbox.energy.getEnergy() == 0.0,
				"batbox must be drained through the source-side branch, has " + batbox.energy.getEnergy());
			helper.succeed();
		});
	}
}
