package ic2.core.gametest;

import ic2.core.IHasGui;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.tool.ContainerToolbox;
import ic2.core.item.tool.HandHeldToolbox;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ToolboxGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static ServerPlayer makePlayer(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    player.setGameMode(GameType.SURVIVAL);
    return player;
  }

  // mock players can't receive the menu-open payload, so exercise the GUI wiring directly
  @GameTest(template = EMPTY)
  public static void toolboxProvidesItsGui(GameTestHelper helper) {
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = new ItemStack(Ic2Items.TOOL_BOX);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    IHasGui inventory =
        ((IHandHeldInventory) Ic2Items.TOOL_BOX)
            .getInventory(player, InteractionHand.MAIN_HAND, stack);
    helper.assertTrue(
        inventory instanceof HandHeldToolbox,
        "the toolbox should provide a hand held toolbox inventory");

    AbstractContainerMenu menu = inventory.createServerScreenHandler(1, player);
    helper.assertTrue(
        menu instanceof ContainerToolbox,
        "the toolbox inventory should provide the toolbox container");
    helper.succeed();
  }

  // only items marked as boxable may go into the toolbox
  @GameTest(template = EMPTY)
  public static void toolboxAcceptsOnlyBoxableItems(GameTestHelper helper) {
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = new ItemStack(Ic2Items.TOOL_BOX);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    HandHeldToolbox toolbox = new HandHeldToolbox(player, InteractionHand.MAIN_HAND, stack, 9);

    helper.assertTrue(
        toolbox.canPlaceItem(0, new ItemStack(Ic2Items.TREETAP)),
        "treetap should fit into the toolbox");
    helper.assertTrue(
        toolbox.canPlaceItem(0, new ItemStack(Ic2Items.WRENCH)),
        "wrench should fit into the toolbox");
    helper.assertTrue(
        toolbox.canPlaceItem(0, new ItemStack(Ic2Items.ELECTRIC_WRENCH)),
        "electric wrench should fit into the toolbox");
    helper.assertFalse(
        toolbox.canPlaceItem(0, new ItemStack(Ic2Items.DRILL)),
        "drill must not fit into the toolbox");
    helper.assertFalse(toolbox.canPlaceItem(0, ItemStack.EMPTY), "empty stacks must be rejected");
    helper.succeed();
  }

  // contents are written to the toolbox item and read back when it is reopened
  @GameTest(template = EMPTY)
  public static void toolboxPersistsContents(GameTestHelper helper) {
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = new ItemStack(Ic2Items.TOOL_BOX);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    HandHeldToolbox toolbox = new HandHeldToolbox(player, InteractionHand.MAIN_HAND, stack, 9);
    toolbox.setItem(3, new ItemStack(Ic2Items.TREETAP));

    // saving replaces the held stack with an updated copy
    ItemStack savedStack = player.getMainHandItem();
    helper.assertValueEqual(savedStack.getItem(), Ic2Items.TOOL_BOX, "held item after saving");
    helper.assertFalse(
        StackUtil.getOrCreateNbtData(savedStack).getList("Items", 10).isEmpty(),
        "toolbox NBT should contain the stored item");

    HandHeldToolbox reopened =
        new HandHeldToolbox(player, InteractionHand.MAIN_HAND, savedStack, 9);
    helper.assertValueEqual(
        reopened.getItem(3).getItem(), Ic2Items.TREETAP, "stored item after reopening");
    helper.succeed();
  }

  // Throwing the open toolbox used to mutate its captured stack to empty before IC2 saved it,
  // causing CUSTOM_DATA creation to return null and crash both the click and close paths.
  @GameTest(template = EMPTY)
  public static void throwingOpenToolboxDoesNotCrash(GameTestHelper helper) {
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = new ItemStack(Ic2Items.TOOL_BOX);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    HandHeldToolbox toolbox = new HandHeldToolbox(player, InteractionHand.MAIN_HAND, stack, 9);
    toolbox.setItem(3, new ItemStack(Ic2Items.TREETAP));
    ContainerToolbox menu = new ContainerToolbox(1, toolbox);
    player.containerMenu = menu;

    // Toolbox slots occupy 0-8, the main inventory 9-35, and hotbar slot 0 is menu slot 36.
    menu.clicked(36, 0, ClickType.THROW, player);

    ItemEntity droppedToolbox =
        helper
            .getLevel()
            .getEntitiesOfClass(
                ItemEntity.class,
                new AABB(player.blockPosition()).inflate(3.0),
                entity -> entity.getItem().is(Ic2Items.TOOL_BOX))
            .stream()
            .findFirst()
            .orElse(null);
    helper.assertTrue(droppedToolbox != null, "throwing the open toolbox should drop it");

    CompoundTag droppedData = StackUtil.getTag(droppedToolbox.getItem());
    helper.assertTrue(droppedData != null, "the dropped toolbox should retain custom data");
    helper.assertFalse(
        droppedData.contains("uid"), "the dropped toolbox must not retain its GUI uid");
    helper.assertFalse(
        droppedData.getList("Items", 10).isEmpty(),
        "the dropped toolbox should retain its contents");
    helper.succeed();
  }
}
