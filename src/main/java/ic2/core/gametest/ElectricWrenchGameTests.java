package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.tool.ItemToolWrenchElectric;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ElectricWrenchGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final double MAX_CHARGE = 12000.0;

  private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);

  private static ItemToolWrenchElectric wrench() {
    return (ItemToolWrenchElectric) Ic2Items.ELECTRIC_WRENCH;
  }

  private static ServerPlayer makePlayer(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    player.setGameMode(GameType.SURVIVAL);
    return player;
  }

  private static Direction getMachineFacing(GameTestHelper helper) {
    return ((Ic2TileEntityBlock) Ic2Blocks.GENERATOR)
        .getFacing(helper.getLevel(), helper.absolutePos(MACHINE_POS));
  }

  // clicking a side the machine doesn't face rotates it for 100 EU
  @GameTest(template = EMPTY)
  public static void electricWrenchRotatesMachine(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.GENERATOR);
    ServerPlayer player = makePlayer(helper);
    ItemStack stack =
        ElectricItemManager.getCharged(Ic2Items.ELECTRIC_WRENCH, Double.POSITIVE_INFINITY);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    Direction newFacing = getMachineFacing(helper).getClockWise();
    InteractionResult result =
        wrench()
            .onItemUseFirst(stack, Ic2GameTestUtil.useOn(helper, player, MACHINE_POS, newFacing));

    helper.assertValueEqual(result, InteractionResult.SUCCESS, "wrench use result");
    helper.assertValueEqual(getMachineFacing(helper), newFacing, "machine facing after rotation");
    helper.assertValueEqual(
        ElectricItem.manager.getCharge(stack), MAX_CHARGE - 100.0, "charge after rotating");
    helper.succeed();
  }

  // clicking the side the machine faces removes it, dropping the machine itself, for 1000 EU
  @GameTest(template = EMPTY)
  public static void electricWrenchRemovesMachine(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.GENERATOR);
    ServerPlayer player = makePlayer(helper);
    ItemStack stack =
        ElectricItemManager.getCharged(Ic2Items.ELECTRIC_WRENCH, Double.POSITIVE_INFINITY);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    InteractionResult result =
        wrench()
            .onItemUseFirst(
                stack,
                Ic2GameTestUtil.useOn(helper, player, MACHINE_POS, getMachineFacing(helper)));

    helper.assertValueEqual(result, InteractionResult.SUCCESS, "wrench use result");
    helper.assertValueEqual(
        ElectricItem.manager.getCharge(stack), MAX_CHARGE - 1000.0, "charge after removing");
    helper.succeedWhen(
        () -> {
          helper.assertBlockPresent(Blocks.AIR, MACHINE_POS);
          helper.assertItemEntityPresent(Ic2Blocks.GENERATOR.asItem(), MACHINE_POS, 2.0);
        });
  }

  // with enough charge to rotate but not to remove, clicking the facing side does nothing
  @GameTest(template = EMPTY)
  public static void electricWrenchLowChargeCannotRemove(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.GENERATOR);
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = ElectricItemManager.getCharged(Ic2Items.ELECTRIC_WRENCH, 500.0);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    InteractionResult result =
        wrench()
            .onItemUseFirst(
                stack,
                Ic2GameTestUtil.useOn(helper, player, MACHINE_POS, getMachineFacing(helper)));

    helper.assertValueEqual(result, InteractionResult.FAIL, "wrench use result");
    helper.assertBlockPresent(Ic2Blocks.GENERATOR, MACHINE_POS);
    helper.assertValueEqual(
        ElectricItem.manager.getCharge(stack), 500.0, "charge must be untouched");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void electricWrenchWithoutChargeFails(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.GENERATOR);
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = new ItemStack(Ic2Items.ELECTRIC_WRENCH);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    Direction facingBefore = getMachineFacing(helper);
    InteractionResult result =
        wrench()
            .onItemUseFirst(
                stack,
                Ic2GameTestUtil.useOn(helper, player, MACHINE_POS, facingBefore.getClockWise()));

    helper.assertValueEqual(result, InteractionResult.FAIL, "wrench use result");
    helper.assertValueEqual(getMachineFacing(helper), facingBefore, "machine must not rotate");
    helper.succeed();
  }
}
