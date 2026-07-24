package me.halfcooler.ic2r.core.gametest;

import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMatter;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityScanner;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rItems;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
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
	public static void matterFabricatorIsInactiveWhenHoldingCharge(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2rBlocks.MATTER_GENERATOR.get());
		TileEntityMatter matter = helper.getBlockEntity(MACHINE_POS);
		matter.getComponent(Energy.class).addEnergy(1000.0);

		// After the charge is applied once, with no further EU intake the fabricator is idle.
		helper.runAtTickTime(20, () ->
		{
			helper.assertTrue(!matter.getActive(), "fabricator holding charge without intake should be inactive");
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void matterFabricatorIsInactiveWhenEmpty(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2rBlocks.MATTER_GENERATOR.get());
		TileEntityMatter matter = helper.getBlockEntity(MACHINE_POS);

		helper.runAtTickTime(20, () ->
		{
			helper.assertTrue(!matter.getActive(), "empty fabricator should be inactive");
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY)
	public static void scannerAcceptsHeatVents(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2rBlocks.UU_SCANNER.get());
		TileEntityScanner scanner = helper.getBlockEntity(MACHINE_POS);

		for (ItemStack vent : List.of(
			new ItemStack(Ic2rItems.HEAT_VENT),
			new ItemStack(Ic2rItems.REACTOR_HEAT_VENT),
			new ItemStack(Ic2rItems.OVERCLOCKED_HEAT_VENT),
			new ItemStack(Ic2rItems.COMPONENT_HEAT_VENT),
			new ItemStack(Ic2rItems.ADVANCED_HEAT_VENT)))
		{
			helper.assertTrue(scanner.inputSlot.accepts(vent), "scanner should accept heat vent " + vent.getItem());
		}

		helper.succeed();
	}
}
