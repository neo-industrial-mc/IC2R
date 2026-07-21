package me.halfcooler.ic2r.core.gametest;

import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMatter;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r")
@PrefixGameTestTemplate(false)
public final class MatterGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";
	private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);

	private MatterGameTests()
	{
	}

	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void matterFabricatorIsActiveWhenHoldingCharge(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2rBlocks.MATTER_GENERATOR.get());
		TileEntityMatter matter = (TileEntityMatter) helper.getBlockEntity(MACHINE_POS);
		matter.getComponent(Energy.class).addEnergy(1000.0);

		helper.runAtTickTime(20, () ->
		{
			helper.assertTrue(matter.getActive(), "fabricator holding charge should be active");
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void matterFabricatorIsInactiveWhenEmpty(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2rBlocks.MATTER_GENERATOR.get());
		TileEntityMatter matter = (TileEntityMatter) helper.getBlockEntity(MACHINE_POS);

		helper.runAtTickTime(20, () ->
		{
			helper.assertTrue(!matter.getActive(), "empty fabricator should be inactive");
			helper.succeed();
		});
	}
}
