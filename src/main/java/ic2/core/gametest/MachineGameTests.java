package ic2.core.gametest;

import ic2.core.block.machine.tileentity.TileEntityElectricFurnace;
import ic2.core.block.machine.tileentity.TileEntityInduction;
import ic2.core.block.machine.tileentity.TileEntityMacerator;
import ic2.core.item.ElectricItemManager;
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
public class MachineGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);

	// macerator: 2 EU/t over 300 ticks, tier 1
	@GameTest(template = EMPTY, timeoutTicks = 400)
	public static void maceratorGrindsCobblestoneToSand(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.MACERATOR);
		TileEntityMacerator te = getMachine(helper, TileEntityMacerator.class);
		te.dischargeSlot.put(0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
		te.inputSlot.put(0, new ItemStack(Items.COBBLESTONE));

		helper.succeedWhen(() ->
		{
			ItemStack output = te.outputSlot.get(0);
			helper.assertTrue(output.getItem() == Items.SAND && output.getCount() == 1, "macerator should produce 1 sand, has " + output);
			helper.assertTrue(te.inputSlot.get(0).isEmpty(), "macerator input should be consumed");
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void maceratorWithoutEnergyDoesNothing(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.MACERATOR);
		TileEntityMacerator te = getMachine(helper, TileEntityMacerator.class);
		te.inputSlot.put(0, new ItemStack(Items.COBBLESTONE));

		helper.runAtTickTime(60, () ->
		{
			helper.assertTrue(te.outputSlot.get(0).isEmpty(), "unpowered macerator must not produce output");
			helper.assertValueEqual(te.inputSlot.get(0).getCount(), 1, "unpowered macerator input count");
			helper.succeed();
		});
	}

	// electric furnace: 3 EU/t over 100 ticks, tier 1
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void electricFurnaceSmeltsCobblestone(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.ELECTRIC_FURNACE);
		TileEntityElectricFurnace te = getMachine(helper, TileEntityElectricFurnace.class);
		te.dischargeSlot.put(0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
		te.inputSlot.put(0, new ItemStack(Items.COBBLESTONE));

		helper.succeedWhen(() ->
		{
			ItemStack output = te.outputSlot.get(0);
			helper.assertTrue(output.getItem() == Items.STONE && output.getCount() == 1, "electric furnace should produce 1 stone, has " + output);
			helper.assertTrue(te.inputSlot.get(0).isEmpty(), "electric furnace input should be consumed");
		});
	}

	// induction furnace: tier 2, progress per tick scales with heat; preheated it finishes in ~13 ticks
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void inductionFurnaceSmeltsBothSlotsWhenHot(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.INDUCTION_FURNACE);
		TileEntityInduction te = getMachine(helper, TileEntityInduction.class);
		te.dischargeSlot.put(0, ElectricItemManager.getCharged(Ic2Items.ADVANCED_RE_BATTERY, Double.POSITIVE_INFINITY));
		te.heat = 10000;
		te.inputSlotA.put(0, new ItemStack(Items.COBBLESTONE));
		te.inputSlotB.put(0, new ItemStack(Items.SAND));

		helper.succeedWhen(() ->
		{
			ItemStack outputA = te.outputSlotA.get(0);
			ItemStack outputB = te.outputSlotB.get(0);
			helper.assertTrue(outputA.getItem() == Items.STONE && outputA.getCount() == 1, "induction slot A should produce 1 stone, has " + outputA);
			helper.assertTrue(outputB.getItem() == Items.GLASS && outputB.getCount() == 1, "induction slot B should produce 1 glass, has " + outputB);
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void inductionFurnaceBuildsAndKeepsHeat(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.INDUCTION_FURNACE);
		TileEntityInduction te = getMachine(helper, TileEntityInduction.class);
		te.dischargeSlot.put(0, ElectricItemManager.getCharged(Ic2Items.ADVANCED_RE_BATTERY, Double.POSITIVE_INFINITY));
		te.inputSlotA.put(0, new ItemStack(Items.COBBLESTONE, 64));

		helper.runAtTickTime(50, () ->
		{
			helper.assertTrue(te.heat > 0, "induction furnace should build up heat while working, heat=" + te.heat);
			helper.succeed();
		});
	}

	private static <T extends BlockEntity> T getMachine(GameTestHelper helper, Class<T> type)
	{
		BlockEntity be = helper.getBlockEntity(MACHINE_POS);
		if (!type.isInstance(be))
		{
			throw new IllegalStateException("expected " + type.getSimpleName() + " at " + MACHINE_POS + ", found " + be);
		}

		return type.cast(be);
	}
}
