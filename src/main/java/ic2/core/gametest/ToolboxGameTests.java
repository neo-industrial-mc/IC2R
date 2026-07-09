package ic2.core.gametest;

import ic2.core.IHasGui;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.tool.ContainerToolbox;
import ic2.core.item.tool.HandHeldToolbox;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ToolboxGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static ServerPlayer makePlayer(GameTestHelper helper)
	{
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		return player;
	}

	// mock players can't receive the menu-open payload, so exercise the GUI wiring directly
	@GameTest(template = EMPTY)
	public static void toolboxProvidesItsGui(GameTestHelper helper)
	{
		ServerPlayer player = makePlayer(helper);
		ItemStack stack = new ItemStack(Ic2Items.TOOL_BOX);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		IHasGui inventory = ((IHandHeldInventory) Ic2Items.TOOL_BOX).getInventory(player, InteractionHand.MAIN_HAND, stack);
		helper.assertTrue(inventory instanceof HandHeldToolbox, "the toolbox should provide a hand held toolbox inventory");

		AbstractContainerMenu menu = inventory.createServerScreenHandler(1, player);
		helper.assertTrue(menu instanceof ContainerToolbox, "the toolbox inventory should provide the toolbox container");
		helper.succeed();
	}

	// only items marked as boxable may go into the toolbox
	@GameTest(template = EMPTY)
	public static void toolboxAcceptsOnlyBoxableItems(GameTestHelper helper)
	{
		ServerPlayer player = makePlayer(helper);
		ItemStack stack = new ItemStack(Ic2Items.TOOL_BOX);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		HandHeldToolbox toolbox = new HandHeldToolbox(player, InteractionHand.MAIN_HAND, stack, 9);

		helper.assertTrue(toolbox.canPlaceItem(0, new ItemStack(Ic2Items.TREETAP)), "treetap should fit into the toolbox");
		helper.assertTrue(toolbox.canPlaceItem(0, new ItemStack(Ic2Items.WRENCH)), "wrench should fit into the toolbox");
		helper.assertTrue(toolbox.canPlaceItem(0, new ItemStack(Ic2Items.ELECTRIC_WRENCH)), "electric wrench should fit into the toolbox");
		helper.assertFalse(toolbox.canPlaceItem(0, new ItemStack(Ic2Items.DRILL)), "drill must not fit into the toolbox");
		helper.assertFalse(toolbox.canPlaceItem(0, ItemStack.EMPTY), "empty stacks must be rejected");
		helper.succeed();
	}

	// contents are written to the toolbox item and read back when it is reopened
	@GameTest(template = EMPTY)
	public static void toolboxPersistsContents(GameTestHelper helper)
	{
		ServerPlayer player = makePlayer(helper);
		ItemStack stack = new ItemStack(Ic2Items.TOOL_BOX);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		HandHeldToolbox toolbox = new HandHeldToolbox(player, InteractionHand.MAIN_HAND, stack, 9);
		toolbox.setItem(3, new ItemStack(Ic2Items.TREETAP));

		// saving replaces the held stack with an updated copy
		ItemStack savedStack = player.getMainHandItem();
		helper.assertValueEqual(savedStack.getItem(), Ic2Items.TOOL_BOX, "held item after saving");
		helper.assertFalse(StackUtil.getOrCreateNbtData(savedStack).getList("Items", 10).isEmpty(), "toolbox NBT should contain the stored item");

		HandHeldToolbox reopened = new HandHeldToolbox(player, InteractionHand.MAIN_HAND, savedStack, 9);
		helper.assertValueEqual(reopened.getItem(3).getItem(), Ic2Items.TREETAP, "stored item after reopening");
		helper.succeed();
	}
}
