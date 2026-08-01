package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.core.item.ElectricItemManager;
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
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ElectricHoeGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final BlockPos DIRT_POS = new BlockPos(1, 1, 1);
  private static final double MAX_CHARGE = 10000.0;
  private static final double USE_COST = 50.0;

  private static ServerPlayer makePlayer(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    player.setGameMode(GameType.SURVIVAL);
    return player;
  }

  @GameTest(template = EMPTY)
  public static void chargedElectricHoeTillsDirtAndConsumesEnergy(GameTestHelper helper) {
    helper.setBlock(DIRT_POS, Blocks.DIRT);
    ServerPlayer player = makePlayer(helper);
    ItemStack hoe = ElectricItemManager.getCharged(Ic2Items.ELECTRIC_HOE, Double.POSITIVE_INFINITY);
    player.setItemInHand(InteractionHand.MAIN_HAND, hoe);

    InteractionResult result =
        hoe.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, DIRT_POS, Direction.UP));

    helper.assertValueEqual(result, InteractionResult.SUCCESS, "electric hoe use result");
    helper.assertBlockPresent(Blocks.FARMLAND, DIRT_POS);
    helper.assertValueEqual(
        ElectricItem.manager.getCharge(hoe), MAX_CHARGE - USE_COST, "charge after tilling");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void emptyElectricHoeDoesNotTillDirt(GameTestHelper helper) {
    helper.setBlock(DIRT_POS, Blocks.DIRT);
    ServerPlayer player = makePlayer(helper);
    ItemStack hoe = new ItemStack(Ic2Items.ELECTRIC_HOE);
    player.setItemInHand(InteractionHand.MAIN_HAND, hoe);

    InteractionResult result =
        hoe.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, DIRT_POS, Direction.UP));

    helper.assertValueEqual(result, InteractionResult.PASS, "empty electric hoe use result");
    helper.assertBlockPresent(Blocks.DIRT, DIRT_POS);
    helper.assertValueEqual(ElectricItem.manager.getCharge(hoe), 0.0, "charge after rejected use");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void blockedElectricHoeUseDoesNotConsumeEnergy(GameTestHelper helper) {
    helper.setBlock(DIRT_POS, Blocks.DIRT);
    helper.setBlock(DIRT_POS.above(), Blocks.STONE);
    ServerPlayer player = makePlayer(helper);
    ItemStack hoe = ElectricItemManager.getCharged(Ic2Items.ELECTRIC_HOE, Double.POSITIVE_INFINITY);
    player.setItemInHand(InteractionHand.MAIN_HAND, hoe);

    InteractionResult result =
        hoe.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, DIRT_POS, Direction.UP));

    helper.assertValueEqual(result, InteractionResult.PASS, "blocked electric hoe use result");
    helper.assertBlockPresent(Blocks.DIRT, DIRT_POS);
    helper.assertValueEqual(
        ElectricItem.manager.getCharge(hoe), MAX_CHARGE, "charge after rejected use");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void electricHoeHasParityMiningSpeedAndActions(GameTestHelper helper) {
    ItemStack charged =
        ElectricItemManager.getCharged(Ic2Items.ELECTRIC_HOE, Double.POSITIVE_INFINITY);
    ItemStack empty = new ItemStack(Ic2Items.ELECTRIC_HOE);

    helper.assertValueEqual(
        charged.getDestroySpeed(Blocks.HAY_BLOCK.defaultBlockState()),
        16.0F,
        "charged electric hoe mining speed");
    helper.assertValueEqual(
        empty.getDestroySpeed(Blocks.HAY_BLOCK.defaultBlockState()),
        1.0F,
        "empty electric hoe mining speed");
    helper.assertTrue(
        charged.canPerformAction(ItemAbilities.HOE_DIG), "electric hoe should perform hoe digging");
    helper.assertTrue(
        charged.canPerformAction(ItemAbilities.HOE_TILL),
        "electric hoe should perform hoe tilling");
    helper.succeed();
  }
}
