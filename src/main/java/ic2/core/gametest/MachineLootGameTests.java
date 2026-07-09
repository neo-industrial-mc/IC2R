package ic2.core.gametest;

import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.item.tool.ItemToolWrench;
import ic2.core.ref.Ic2Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class MachineLootGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);

	// breaking a macerator returns the basic machine casing, not the macerator itself
	@GameTest(template = EMPTY)
	public static void maceratorDropsMachineCasing(GameTestHelper helper)
	{
		assertBreakingDrops(helper, Ic2Blocks.MACERATOR, Ic2Blocks.MACHINE.asItem());
	}

	@GameTest(template = EMPTY)
	public static void electricFurnaceDropsItself(GameTestHelper helper)
	{
		assertBreakingDrops(helper, Ic2Blocks.ELECTRIC_FURNACE, Ic2Blocks.ELECTRIC_FURNACE.asItem());
	}

	// advanced machines return the advanced machine casing
	@GameTest(template = EMPTY)
	public static void inductionFurnaceDropsAdvancedCasing(GameTestHelper helper)
	{
		assertBreakingDrops(helper, Ic2Blocks.INDUCTION_FURNACE, Ic2Blocks.ADVANCED_MACHINE.asItem());
	}

	private static void assertBreakingDrops(GameTestHelper helper, Block machine, Item expectedDrop)
	{
		helper.setBlock(MACHINE_POS, machine);
		// GameTestHelper.destroyBlock suppresses drops, so break through the level instead
		helper.getLevel().destroyBlock(helper.absolutePos(MACHINE_POS), true);

		helper.succeedWhen(() -> helper.assertItemEntityPresent(expectedDrop, MACHINE_POS, 2.0));
	}

	// wrench removal returns the machine itself instead of the casing
	@GameTest(template = EMPTY)
	public static void wrenchedMaceratorDropsItselfNotCasing(GameTestHelper helper)
	{
		assertWrenchingDrops(helper, Ic2Blocks.MACERATOR, Ic2Blocks.MACERATOR.asItem(), Ic2Blocks.MACHINE.asItem());
	}

	@GameTest(template = EMPTY)
	public static void wrenchedInductionFurnaceDropsItselfNotCasing(GameTestHelper helper)
	{
		assertWrenchingDrops(helper, Ic2Blocks.INDUCTION_FURNACE, Ic2Blocks.INDUCTION_FURNACE.asItem(), Ic2Blocks.ADVANCED_MACHINE.asItem());
	}

	private static void assertWrenchingDrops(GameTestHelper helper, Block machine, Item expectedDrop, Item forbiddenDrop)
	{
		helper.setBlock(MACHINE_POS, machine);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();

		BlockPos absolutePos = helper.absolutePos(MACHINE_POS);
		// clicking the side the machine already faces skips the rotation step and removes it
		Direction facing = ((Ic2TileEntityBlock) machine).getFacing(helper.getLevel(), absolutePos);
		ItemToolWrench.WrenchResult result = ItemToolWrench.wrenchBlock(helper.getLevel(), absolutePos, facing, player, true);

		helper.assertValueEqual(result, ItemToolWrench.WrenchResult.Removed, "wrench result");
		helper.succeedWhen(() ->
		{
			helper.assertBlockPresent(Blocks.AIR, MACHINE_POS);
			helper.assertItemEntityPresent(expectedDrop, MACHINE_POS, 2.0);
			helper.assertItemEntityNotPresent(forbiddenDrop, MACHINE_POS, 2.0);
		});
	}
}
