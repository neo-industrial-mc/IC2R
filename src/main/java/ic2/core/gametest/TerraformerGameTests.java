package ic2.core.gametest;

import ic2.api.item.ITerraformingBP;
import ic2.core.block.comp.Energy;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class TerraformerGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);

  // the blueprint logic only operates at y >= 0, so terrain for the blueprint tests is built high
  // above the test structure
  private static final int WORK_Y = 100;

  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void terraformerRunsOnBlueprintAndConsumesEnergy(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.TERRAFORMER);
    TileEntityTerra te = getTe(helper, MACHINE_POS, TileEntityTerra.class);
    te.getComponent(Energy.class).addEnergy(100000.0);
    te.tfbpSlot.put(new ItemStack(Ic2Items.CHILLING_TFBP));

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              te.getActive(), "terraformer with blueprint and energy should be active");
          helper.assertTrue(
              te.getComponent(Energy.class).getEnergy() < 100000.0 - 1.0,
              "terraformer should consume energy for terraforming attempts, has "
                  + te.getComponent(Energy.class).getEnergy());
        });
  }

  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void terraformerWithoutBlueprintStaysIdle(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.TERRAFORMER);
    TileEntityTerra te = getTe(helper, MACHINE_POS, TileEntityTerra.class);
    te.getComponent(Energy.class).addEnergy(100000.0);

    helper.runAtTickTime(
        50,
        () -> {
          helper.assertFalse(te.getActive(), "terraformer without blueprint must not run");
          Ic2GameTestAssertions.assertNear(
              helper,
              te.getComponent(Energy.class).getEnergy(),
              100000.0,
              "energy of an idle terraformer");
          helper.succeed();
        });
  }

  @GameTest(template = EMPTY)
  public static void chillingBlueprintFreezesWater(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos pos = workPos(helper);
    level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());

    try {
      helper.assertTrue(
          terraform(Ic2Items.CHILLING_TFBP, level, pos),
          "chilling terraform on water should succeed");
      helper.assertTrue(
          level.getBlockState(pos).is(Blocks.ICE),
          "water should freeze to ice, is " + level.getBlockState(pos));
    } finally {
      level.removeBlock(pos, false);
    }

    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void cultivationBlueprintTurnsSandIntoDirt(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos pos = workPos(helper);
    level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
    level.setBlockAndUpdate(pos, Blocks.SAND.defaultBlockState());

    try {
      helper.assertTrue(
          terraform(Ic2Items.CULTIVATION_TFBP, level, pos),
          "cultivation terraform on sand should succeed");
      helper.assertTrue(
          level.getBlockState(pos).is(Blocks.DIRT),
          "sand should turn into dirt, is " + level.getBlockState(pos));
    } finally {
      level.removeBlock(pos, false);
      level.removeBlock(pos.below(), false);
    }

    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void desertificationBlueprintTurnsDirtIntoSand(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos pos = workPos(helper);
    level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
    level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());

    try {
      helper.assertTrue(
          terraform(Ic2Items.DESERTIFICATION_TFBP, level, pos),
          "desertification terraform on dirt should succeed");
      helper.assertTrue(
          level.getBlockState(pos).is(Blocks.SAND),
          "dirt should turn into sand, is " + level.getBlockState(pos));
    } finally {
      level.removeBlock(pos, false);
      level.removeBlock(pos.below(), false);
    }

    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void flatificationBlueprintLevelsTerrain(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos highColumn = workPos(helper);
    BlockPos lowColumn = workPos(helper).east(2);
    // a stone spike two blocks above the reference level gets removed
    level.setBlockAndUpdate(highColumn.above(2), Blocks.STONE.defaultBlockState());
    // a depression five blocks below the reference level gets filled with dirt
    level.setBlockAndUpdate(lowColumn.below(5), Blocks.STONE.defaultBlockState());

    try {
      helper.assertTrue(
          terraform(Ic2Items.FLATIFICATION_TFBP, level, highColumn),
          "flatification should remove blocks above the reference level");
      helper.assertTrue(
          level.getBlockState(highColumn.above(2)).isAir(),
          "stone above the reference level should be removed");

      helper.assertTrue(
          terraform(Ic2Items.FLATIFICATION_TFBP, level, lowColumn),
          "flatification should fill ground below the reference level");
      helper.assertTrue(
          level.getBlockState(lowColumn.below(4)).is(Blocks.DIRT),
          "dirt should be stacked on the low ground, is "
              + level.getBlockState(lowColumn.below(4)));
    } finally {
      level.removeBlock(highColumn.above(2), false);
      level.removeBlock(lowColumn.below(5), false);
      level.removeBlock(lowColumn.below(4), false);
    }

    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void irrigationBlueprintBonemealsCrops(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos pos = workPos(helper);
    level.setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState());
    level.setBlockAndUpdate(pos, Blocks.WHEAT.defaultBlockState());

    try {
      // the irrigation blueprint has a 1/48000 chance to make it rain instead of terraforming, so
      // allow a retry
      for (int attempt = 0; attempt < 2 && getWheatAge(level, pos) == 0; attempt++) {
        helper.assertTrue(
            terraform(Ic2Items.IRRIGATION_TFBP, level, pos),
            "irrigation terraform on wheat should succeed");
      }

      helper.assertTrue(
          getWheatAge(level, pos) > 0,
          "wheat should be bonemealed, age is " + getWheatAge(level, pos));
    } finally {
      level.removeBlock(pos, false);
      level.removeBlock(pos.below(), false);
    }

    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void mushroomBlueprintSpreadsMycelium(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos center = workPos(helper);

    try {
      for (int dx = -1; dx <= 0; dx++) {
        for (int dz = -1; dz <= 0; dz++) {
          level.setBlockAndUpdate(center.offset(dx, 0, dz), Blocks.DIRT.defaultBlockState());
        }
      }

      helper.assertTrue(
          terraform(Ic2Items.MUSHROOM_TFBP, level, center),
          "mushroom terraform on dirt should succeed");

      boolean foundMycelium = false;
      for (int dx = -1; dx <= 0; dx++) {
        for (int dz = -1; dz <= 0; dz++) {
          foundMycelium |= level.getBlockState(center.offset(dx, 0, dz)).is(Blocks.MYCELIUM);
        }
      }

      helper.assertTrue(
          foundMycelium, "one of the dirt blocks should have been converted to mycelium");
    } finally {
      for (int dx = -1; dx <= 0; dx++) {
        for (int dz = -1; dz <= 0; dz++) {
          level.removeBlock(center.offset(dx, 0, dz), false);
        }
      }
    }

    helper.succeed();
  }

  private static boolean terraform(Item blueprint, ServerLevel level, BlockPos pos) {
    ItemStack stack = new ItemStack(blueprint);
    return ((ITerraformingBP) blueprint).terraform(stack, level, pos);
  }

  private static int getWheatAge(ServerLevel level, BlockPos pos) {
    BlockState state = level.getBlockState(pos);
    if (!state.is(Blocks.WHEAT)) {
      throw new IllegalStateException("expected wheat at " + pos + ", found " + state);
    }

    return state.getValue(CropBlock.AGE);
  }

  private static BlockPos workPos(GameTestHelper helper) {
    return helper.absolutePos(new BlockPos(1, 1, 1)).atY(WORK_Y);
  }

  private static <T extends BlockEntity> T getTe(
      GameTestHelper helper, BlockPos pos, Class<T> type) {
    BlockEntity be = helper.getBlockEntity(pos);
    if (!type.isInstance(be)) {
      throw new IllegalStateException(
          "expected " + type.getSimpleName() + " at " + pos + ", found " + be);
    }

    return type.cast(be);
  }
}
