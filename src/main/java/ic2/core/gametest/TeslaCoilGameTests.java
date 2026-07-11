package ic2.core.gametest;

import ic2.core.block.comp.Energy;
import ic2.core.block.machine.tileentity.TileEntityTesla;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class TeslaCoilGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final BlockPos COIL_POS = new BlockPos(1, 1, 1);

  // the coil shocks every 32 powered ticks for energy/400 damage split between all targets in a 4
  // block radius
  @GameTest(template = EMPTY, timeoutTicks = 150)
  public static void teslaCoilShocksNearbyMobWhenPowered(GameTestHelper helper) {
    placeFloor(helper);
    helper.setBlock(COIL_POS, Ic2Blocks.TESLA_COIL);
    helper.setBlock(new BlockPos(0, 1, 1), Blocks.REDSTONE_BLOCK);
    TileEntityTesla te = getTe(helper, COIL_POS, TileEntityTesla.class);
    te.getComponent(Energy.class).addEnergy(8000.0);

    Pig pig = helper.spawn(EntityType.PIG, new BlockPos(2, 1, 1));

    helper.succeedWhen(
        () -> {
          helper.assertFalse(
              pig.isAlive(), "a 8000 EU discharge deals ~19 damage and should kill the pig");
          helper.assertTrue(
              te.getComponent(Energy.class).getEnergy() < 8000.0 - 400.0,
              "the shock should consume 400 EU per point of damage, energy is "
                  + te.getComponent(Energy.class).getEnergy());
        });
  }

  @GameTest(template = EMPTY, timeoutTicks = 150)
  public static void teslaCoilStaysIdleWithoutRedstone(GameTestHelper helper) {
    placeFloor(helper);
    helper.setBlock(COIL_POS, Ic2Blocks.TESLA_COIL);
    TileEntityTesla te = getTe(helper, COIL_POS, TileEntityTesla.class);
    te.getComponent(Energy.class).addEnergy(8000.0);

    Pig pig = helper.spawn(EntityType.PIG, new BlockPos(2, 1, 1));

    helper.runAtTickTime(
        80,
        () -> {
          helper.assertTrue(
              pig.isAlive() && pig.getHealth() >= pig.getMaxHealth(),
              "an unpowered tesla coil must not hurt anything");
          Ic2GameTestAssertions.assertNear(
              helper,
              te.getComponent(Energy.class).getEnergy(),
              8000.0,
              "an unpowered tesla coil must not consume energy");
          helper.succeed();
        });
  }

  private static void placeFloor(GameTestHelper helper) {
    for (int x = 0; x < 3; x++) {
      for (int z = 0; z < 3; z++) {
        helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
      }
    }
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
