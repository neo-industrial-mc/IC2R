package me.halfcooler.ic2r.core.gametest;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityCanner;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r")
@PrefixGameTestTemplate(false)
public final class CannerGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";
	private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);

	private CannerGameTests()
	{
	}

	@GameTest(template = EMPTY)
	public static void cannerEnrichAcceptsLapisWithPartiallyFilledTank(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2rBlocks.CANNER.get());
		TileEntityCanner te = helper.getBlockEntity(MACHINE_POS);
		te.setMode(TileEntityCanner.Mode.EnrichLiquid);
		int filled = te.inputTank.fillMb(Ic2rFluidStack.create(Ic2rFluids.DISTILLED_WATER.still(), 500), false);
		helper.assertValueEqual(filled, 500, "distilled water accepted by the canner input tank");

		helper.assertTrue(
			te.inputSlot.accepts(new ItemStack(Ic2rItems.LAPIS_DUST)),
			"canner should accept lapis dust while the tank holds less than the recipe amount"
		);
		helper.succeed();
	}
}
