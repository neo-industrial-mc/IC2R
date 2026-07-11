package ic2.core.gametest;

import ic2.core.fluid.Ic2FluidStack;
import ic2.core.item.tool.ItemSprayer;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
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
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class FoamSprayerGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final String TALL = "gametest/empty3x9x3";

  private static final int CAPACITY = 8000;
  private static final int FLUID_PER_FOAM = 100;

  private static ItemSprayer sprayer() {
    return (ItemSprayer) Ic2Items.FOAM_SPRAYER;
  }

  private static ItemStack makeFilledSprayer() {
    ItemStack stack = new ItemStack(Ic2Items.FOAM_SPRAYER);
    int filled =
        sprayer()
            .fillMb(
                stack,
                Ic2FluidStack.create(Ic2Fluids.CONSTRUCTION_FOAM.still(), CAPACITY),
                false,
                null);
    if (filled != CAPACITY) {
      throw new IllegalStateException("failed to fill the sprayer: " + filled);
    }

    return stack;
  }

  private static int getFluidAmount(ItemStack stack) {
    Ic2FluidStack fluid = Ic2FluidStack.get(stack);
    return fluid.isEmpty() ? 0 : fluid.getAmountMb();
  }

  private static ServerPlayer makePlayer(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    player.setGameMode(GameType.SURVIVAL);
    return player;
  }

  // foam flood-fills an enclosed cavity, using 100 mB per block
  @GameTest(template = TALL)
  public static void sprayerFillsEnclosedCavity(GameTestHelper helper) {
    // dirt shell with a 1x7x1 air shaft in the middle
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 9; y++) {
        for (int z = 0; z < 3; z++) {
          helper.setBlock(
              new BlockPos(x, y, z),
              x == 1 && z == 1 && y >= 1 && y <= 7 ? Blocks.AIR : Blocks.DIRT);
        }
      }
    }

    ServerPlayer player = makePlayer(helper);
    ItemStack stack = makeFilledSprayer();
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    // spray onto the top face of the shaft's floor block
    InteractionResult result =
        sprayer().useOn(Ic2GameTestUtil.useOn(helper, player, new BlockPos(1, 0, 1), Direction.UP));

    helper.assertValueEqual(result, InteractionResult.SUCCESS, "sprayer use result");
    for (int y = 1; y <= 7; y++) {
      helper.assertBlockPresent(Ic2Blocks.FOAM, new BlockPos(1, y, 1));
    }

    helper.assertValueEqual(
        getFluidAmount(player.getMainHandItem()),
        CAPACITY - 7 * FLUID_PER_FOAM,
        "fluid after filling seven blocks");
    helper.succeed();
  }

  // single mode places exactly one foam block
  @GameTest(template = TALL)
  public static void sprayerSingleModePlacesOneBlock(GameTestHelper helper) {
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 9; y++) {
        for (int z = 0; z < 3; z++) {
          helper.setBlock(
              new BlockPos(x, y, z),
              x == 1 && z == 1 && y >= 1 && y <= 7 ? Blocks.AIR : Blocks.DIRT);
        }
      }
    }

    ServerPlayer player = makePlayer(helper);
    ItemStack stack = makeFilledSprayer();
    StackUtil.getOrCreateNbtData(stack).putInt("mode", 1);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    InteractionResult result =
        sprayer().useOn(Ic2GameTestUtil.useOn(helper, player, new BlockPos(1, 0, 1), Direction.UP));

    helper.assertValueEqual(result, InteractionResult.SUCCESS, "sprayer use result");
    helper.assertBlockPresent(Ic2Blocks.FOAM, new BlockPos(1, 1, 1));
    helper.assertBlockPresent(Blocks.AIR, new BlockPos(1, 2, 1));
    helper.assertValueEqual(
        getFluidAmount(player.getMainHandItem()),
        CAPACITY - FLUID_PER_FOAM,
        "fluid after a single foam block");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void sprayerModeSwitchTogglesSingleMode(GameTestHelper helper) {
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = makeFilledSprayer();
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    Ic2GameTestUtil.pressModeSwitchKey(player);
    try {
      sprayer().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
      helper.assertValueEqual(
          StackUtil.getOrCreateNbtData(stack).getInt("mode"), 1, "mode after first toggle");

      sprayer().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
      helper.assertValueEqual(
          StackUtil.getOrCreateNbtData(stack).getInt("mode"), 0, "mode after second toggle");
    } finally {
      Ic2GameTestUtil.releaseModeSwitchKey(player);
    }

    helper.succeed();
  }

  // spraying scaffolding replaces it with foam
  @GameTest(template = EMPTY)
  public static void sprayerConvertsScaffolding(GameTestHelper helper) {
    BlockPos pos = new BlockPos(1, 1, 1);
    helper.setBlock(
        pos, Blocks.SCAFFOLDING.defaultBlockState().setValue(ScaffoldingBlock.DISTANCE, 0));
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = makeFilledSprayer();
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    InteractionResult result =
        sprayer().useOn(Ic2GameTestUtil.useOn(helper, player, pos, Direction.NORTH));

    helper.assertValueEqual(result, InteractionResult.SUCCESS, "sprayer use result");
    helper.assertBlockPresent(Ic2Blocks.FOAM, pos);
    helper.assertValueEqual(
        getFluidAmount(player.getMainHandItem()),
        CAPACITY - FLUID_PER_FOAM,
        "fluid after foaming scaffolding");
    helper.succeed();
  }

  // spraying a cable wraps it in foam, turning it into the foamed cable block
  @GameTest(template = EMPTY)
  public static void sprayerFoamsCable(GameTestHelper helper) {
    BlockPos pos = new BlockPos(1, 1, 1);
    helper.setBlock(pos, Ic2Blocks.GLASS_FIBRE_CABLE);
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = makeFilledSprayer();
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    InteractionResult result =
        sprayer().useOn(Ic2GameTestUtil.useOn(helper, player, pos, Direction.NORTH));

    helper.assertValueEqual(result, InteractionResult.SUCCESS, "sprayer use result");
    helper.assertBlockPresent(Ic2Blocks.GLASS_FIBRE_FOAM_CABLE, pos);
    helper.assertValueEqual(
        getFluidAmount(player.getMainHandItem()),
        CAPACITY - FLUID_PER_FOAM,
        "fluid after foaming a cable");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void emptySprayerFails(GameTestHelper helper) {
    ServerPlayer player = makePlayer(helper);
    ItemStack stack = new ItemStack(Ic2Items.FOAM_SPRAYER);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    helper.setBlock(new BlockPos(1, 0, 1), Blocks.DIRT);
    InteractionResult result =
        sprayer().useOn(Ic2GameTestUtil.useOn(helper, player, new BlockPos(1, 0, 1), Direction.UP));

    helper.assertValueEqual(result, InteractionResult.FAIL, "empty sprayer use result");
    helper.assertBlockPresent(Blocks.AIR, new BlockPos(1, 1, 1));
    helper.succeed();
  }
}
