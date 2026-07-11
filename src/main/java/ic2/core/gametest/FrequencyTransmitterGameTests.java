package ic2.core.gametest;

import ic2.core.block.machine.tileentity.TileEntityTeleporter;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class FrequencyTransmitterGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final BlockPos TP1_POS = new BlockPos(0, 1, 0);
  private static final BlockPos TP2_POS = new BlockPos(2, 1, 0);

  private static ServerPlayer makePlayer(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    player.setGameMode(GameType.SURVIVAL);
    return player;
  }

  private static TileEntityTeleporter getTeleporter(GameTestHelper helper, BlockPos pos) {
    return (TileEntityTeleporter) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
  }

  // using the transmitter on two teleporters links them both ways
  @GameTest(template = EMPTY)
  public static void transmitterLinksTwoTeleporters(GameTestHelper helper) {
    helper.setBlock(TP1_POS, Ic2Blocks.TELEPORTER);
    helper.setBlock(TP2_POS, Ic2Blocks.TELEPORTER);
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = new ItemStack(Ic2Items.FREQUENCY_TRANSMITTER);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    stack.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, TP1_POS, Direction.UP));

    CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
    helper.assertTrue(
        nbt.getBoolean("targetSet"), "first use should store the teleporter position");
    helper.assertValueEqual(
        new BlockPos(nbt.getInt("targetX"), nbt.getInt("targetY"), nbt.getInt("targetZ")),
        helper.absolutePos(TP1_POS),
        "stored target position");

    stack.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, TP2_POS, Direction.UP));

    TileEntityTeleporter tp1 = getTeleporter(helper, TP1_POS);
    TileEntityTeleporter tp2 = getTeleporter(helper, TP2_POS);
    helper.assertTrue(tp1.hasTarget(), "first teleporter should be linked");
    helper.assertTrue(tp2.hasTarget(), "second teleporter should be linked");
    helper.assertValueEqual(
        tp1.getTarget(), helper.absolutePos(TP2_POS), "first teleporter's target");
    helper.assertValueEqual(
        tp2.getTarget(), helper.absolutePos(TP1_POS), "second teleporter's target");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void transmitterCannotLinkTeleporterToItself(GameTestHelper helper) {
    helper.setBlock(TP1_POS, Ic2Blocks.TELEPORTER);
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = new ItemStack(Ic2Items.FREQUENCY_TRANSMITTER);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    stack.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, TP1_POS, Direction.UP));
    stack.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, TP1_POS, Direction.UP));

    helper.assertFalse(
        getTeleporter(helper, TP1_POS).hasTarget(), "a teleporter must not link to itself");
    helper.succeed();
  }

  // using the transmitter in the air (after the grace use) clears the stored position
  @GameTest(template = EMPTY)
  public static void transmitterUseInAirUnlinks(GameTestHelper helper) {
    helper.setBlock(TP1_POS, Ic2Blocks.TELEPORTER);
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = new ItemStack(Ic2Items.FREQUENCY_TRANSMITTER);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    stack.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, TP1_POS, Direction.UP));
    CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
    helper.assertTrue(nbt.getBoolean("targetSet"), "target should be stored");

    // the first right-click after storing a position is a grace use and keeps the target
    stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    helper.assertTrue(
        nbt.getBoolean("targetSet"), "target should survive the use right after linking");

    stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    helper.assertFalse(
        nbt.getBoolean("targetSet"), "target should be cleared by a later use in the air");
    helper.succeed();
  }
}
