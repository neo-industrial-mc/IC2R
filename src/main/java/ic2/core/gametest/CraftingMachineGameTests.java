package ic2.core.gametest;

import ic2.core.block.comp.Energy;
import ic2.core.block.machine.tileentity.TileEntityBatchCrafter;
import ic2.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class CraftingMachineGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);

	// industrial workbench: the tool combo slots pair a fixed tool with one input and expose the crafting result
	@GameTest(template = EMPTY)
	public static void industrialWorkbenchToolCombosResolveRecipes(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.INDUSTRIAL_WORKBENCH);
		TileEntityIndustrialWorkbench te = getTe(helper, MACHINE_POS, TileEntityIndustrialWorkbench.class);

		te.leftCrafting.tool.put(new ItemStack(Ic2Items.FORGE_HAMMER));
		te.leftCrafting.input.put(new ItemStack(Items.IRON_INGOT));
		ItemStack hammered = te.leftCrafting.getOutputStack();
		helper.assertTrue(hammered.getItem() == Ic2Items.IRON_PLATE, "hammer + iron ingot should offer an iron plate, offers " + hammered);

		te.rightCrafting.tool.put(new ItemStack(Ic2Items.CUTTER));
		te.rightCrafting.input.put(new ItemStack(Ic2Items.COPPER_PLATE));
		ItemStack cut = te.rightCrafting.getOutputStack();
		helper.assertTrue(cut.getItem() == Ic2Items.COPPER_CABLE && cut.getCount() == 2,
			"cutter + copper plate should offer 2 copper cables, offers " + cut);

		// without the hammer the ingot alone matches vanilla's nugget recipe at most, never the plate
		te.leftCrafting.tool.clear();
		ItemStack withoutTool = te.leftCrafting.getOutputStack();
		helper.assertTrue(withoutTool.getItem() != Ic2Items.IRON_PLATE, "without the hammer the plate recipe must not match, offers " + withoutTool);

		helper.succeed();
	}

	// batch crafter: 40 ticks at 2 EU/t per crafting operation, pulling from the per-slot ingredient buffers
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void batchCrafterCraftsConfiguredRecipe(GameTestHelper helper)
	{
		TileEntityBatchCrafter te = setupPlankCrafter(helper);
		te.getComponent(Energy.class).addEnergy(20000.0);

		helper.succeedWhen(() ->
		{
			ItemStack output = te.craftingOutput.get(0);
			helper.assertTrue(output.getItem() == Items.CRAFTING_TABLE && output.getCount() == 1,
				"the batch crafter should produce exactly one crafting table, has " + output);
			helper.assertTrue(te.ingredientsRow[0].get().isEmpty(), "the plank ingredients should be consumed");
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void batchCrafterWithoutEnergyDoesNotCraft(GameTestHelper helper)
	{
		TileEntityBatchCrafter te = setupPlankCrafter(helper);

		helper.runAtTickTime(60, () ->
		{
			helper.assertTrue(te.craftingOutput.get(0).isEmpty(), "an unpowered batch crafter must not produce output");
			helper.assertValueEqual(te.ingredientsRow[0].get().getCount(), 1, "an unpowered batch crafter must not consume ingredients");
			helper.succeed();
		});
	}

	private static TileEntityBatchCrafter setupPlankCrafter(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.BATCH_CRAFTER);
		TileEntityBatchCrafter te = getTe(helper, MACHINE_POS, TileEntityBatchCrafter.class);

		// 2x2 planks -> crafting table, laid out in the top left of the 3x3 grid
		int[] plankSlots = {0, 1, 3, 4};
		for (int i = 0; i < te.craftingGrid.length; i++)
		{
			te.craftingGrid[i] = ItemStack.EMPTY;
		}

		for (int slot : plankSlots)
		{
			te.craftingGrid[slot] = new ItemStack(Items.OAK_PLANKS);
		}

		te.matrixChange(-1);
		helper.assertTrue(te.recipeOutput.getItem() == Items.CRAFTING_TABLE, "the configured grid should resolve to a crafting table, resolves to " + te.recipeOutput);

		for (int slot : plankSlots)
		{
			te.ingredientsRow[slot].put(new ItemStack(Items.OAK_PLANKS));
		}

		return te;
	}

	private static <T extends BlockEntity> T getTe(GameTestHelper helper, BlockPos pos, Class<T> type)
	{
		BlockEntity be = helper.getBlockEntity(pos);
		if (!type.isInstance(be))
		{
			throw new IllegalStateException("expected " + type.getSimpleName() + " at " + pos + ", found " + be);
		}

		return type.cast(be);
	}
}
