package ic2.core.gametest;

import ic2.core.block.comp.Energy;
import ic2.core.block.machine.tileentity.TileEntityItemBuffer;
import ic2.core.block.machine.tileentity.TileEntitySortingMachine;
import ic2.core.block.machine.tileentity.TileEntityWeightedItemDistributor;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ItemRoutingGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);
	private static final BlockPos EAST_POS = new BlockPos(2, 1, 1);
	private static final BlockPos WEST_POS = new BlockPos(0, 1, 1);
	private static final BlockPos DOWN_POS = new BlockPos(1, 0, 1);

	// sorting machine: items matching a side filter are sent to that side in filter-sized batches for 20 EU per item
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void sortingMachineRoutesFilteredItemsToConfiguredSide(GameTestHelper helper)
	{
		helper.setBlock(EAST_POS, Blocks.CHEST);
		helper.setBlock(DOWN_POS, Blocks.CHEST);
		helper.setBlock(MACHINE_POS, Ic2Blocks.SORTING_MACHINE);
		TileEntitySortingMachine te = getTe(helper, MACHINE_POS, TileEntitySortingMachine.class);
		te.getComponent(Energy.class).addEnergy(15000.0);
		te.getFilterSlots(Direction.EAST)[0] = new ItemStack(Items.COBBLESTONE, 8);
		te.buffer.put(0, new ItemStack(Items.COBBLESTONE, 16));

		helper.succeedWhen(() ->
		{
			helper.assertValueEqual(countItems(getTe(helper, EAST_POS, ChestBlockEntity.class), Items.COBBLESTONE), 16, "cobblestone routed to the east chest");
			helper.assertValueEqual(countItems(getTe(helper, DOWN_POS, ChestBlockEntity.class), Items.COBBLESTONE), 0, "no cobblestone may reach the default route");
		});
	}

	// items not matching any filter leave through the default route (down) one at a time
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void sortingMachineSendsUnfilteredItemsToDefaultRoute(GameTestHelper helper)
	{
		helper.setBlock(EAST_POS, Blocks.CHEST);
		helper.setBlock(DOWN_POS, Blocks.CHEST);
		helper.setBlock(MACHINE_POS, Ic2Blocks.SORTING_MACHINE);
		TileEntitySortingMachine te = getTe(helper, MACHINE_POS, TileEntitySortingMachine.class);
		te.getComponent(Energy.class).addEnergy(15000.0);
		te.getFilterSlots(Direction.EAST)[0] = new ItemStack(Items.COBBLESTONE, 8);
		te.buffer.put(0, new ItemStack(Items.DIRT, 4));

		helper.succeedWhen(() ->
		{
			helper.assertValueEqual(countItems(getTe(helper, DOWN_POS, ChestBlockEntity.class), Items.DIRT), 4, "dirt sent through the default route");
			helper.assertValueEqual(countItems(getTe(helper, EAST_POS, ChestBlockEntity.class), Items.DIRT), 0, "no dirt may reach the filtered side");
		});
	}

	// item buffer: an ejector upgrade pushes the buffered items into an adjacent inventory one item per tick
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void itemBufferEjectsContentsIntoChest(GameTestHelper helper)
	{
		helper.setBlock(EAST_POS, Blocks.CHEST);
		helper.setBlock(MACHINE_POS, Ic2Blocks.ITEM_BUFFER);
		TileEntityItemBuffer te = getTe(helper, MACHINE_POS, TileEntityItemBuffer.class);
		te.upgradeSlot.put(0, new ItemStack(Ic2Items.EJECTOR_UPGRADE));
		te.rightcontentSlot.put(0, new ItemStack(Items.COBBLESTONE, 8));

		helper.succeedWhen(() ->
		{
			helper.assertValueEqual(countItems(getTe(helper, EAST_POS, ChestBlockEntity.class), Items.COBBLESTONE), 8, "cobblestone ejected into the chest");
			helper.assertTrue(te.rightcontentSlot.get(0).isEmpty(), "the buffer slot should be emptied");
		});
	}

	// weighted item distributor: the full buffer goes to the highest priority side that accepts it
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void weightedItemDistributorPrefersFirstPriority(GameTestHelper helper)
	{
		TileEntityWeightedItemDistributor te = setupDistributor(helper);
		te.buffer.put(0, new ItemStack(Items.COBBLESTONE, 16));

		helper.succeedWhen(() ->
		{
			helper.assertValueEqual(countItems(getTe(helper, EAST_POS, ChestBlockEntity.class), Items.COBBLESTONE), 16, "cobblestone in the first priority chest");
			helper.assertValueEqual(countItems(getTe(helper, WEST_POS, ChestBlockEntity.class), Items.COBBLESTONE), 0, "the second priority chest must stay empty");
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void weightedItemDistributorFallsBackWhenPriorityIsFull(GameTestHelper helper)
	{
		TileEntityWeightedItemDistributor te = setupDistributor(helper);
		ChestBlockEntity eastChest = getTe(helper, EAST_POS, ChestBlockEntity.class);

		for (int i = 0; i < eastChest.getContainerSize(); i++)
		{
			eastChest.setItem(i, new ItemStack(Items.DIRT, 64));
		}

		te.buffer.put(0, new ItemStack(Items.COBBLESTONE, 16));

		helper.succeedWhen(() ->
		{
			helper.assertValueEqual(countItems(getTe(helper, WEST_POS, ChestBlockEntity.class), Items.COBBLESTONE), 16,
				"cobblestone must fall through to the second priority when the first is full");
		});
	}

	private static TileEntityWeightedItemDistributor setupDistributor(GameTestHelper helper)
	{
		helper.setBlock(EAST_POS, Blocks.CHEST);
		helper.setBlock(WEST_POS, Blocks.CHEST);
		// face the intake up so it does not collide with the east/west priorities
		helper.setBlock(MACHINE_POS, Ic2Blocks.WEIGHTED_ITEM_DISTRIBUTOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.UP));
		TileEntityWeightedItemDistributor te = getTe(helper, MACHINE_POS, TileEntityWeightedItemDistributor.class);
		te.getPriority().add(Direction.EAST);
		te.getPriority().add(Direction.WEST);
		return te;
	}

	private static int countItems(Container container, Item item)
	{
		int total = 0;

		for (int i = 0; i < container.getContainerSize(); i++)
		{
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty() && stack.getItem() == item)
			{
				total += stack.getCount();
			}
		}

		return total;
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
