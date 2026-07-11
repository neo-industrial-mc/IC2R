package ic2.core.gametest;

import ic2.core.block.machine.tileentity.TileEntityPump;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class PumpGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final String EMPTY_LARGE = "gametest/empty7x7x7";

  private static final BlockPos PUMP_POS = new BlockPos(1, 2, 1);
  private static final BlockPos WATER_POS = new BlockPos(1, 1, 1);

  // pump: 20 ticks at 1 EU/t per operation, drains the facing fluid source into the tank and fills
  // containers from it
  @GameTest(template = EMPTY, timeoutTicks = 200)
  public static void pumpDrainsWaterSourceIntoBucket(GameTestHelper helper) {
    TileEntityPump te = setupPumpOverWater(helper);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.containerSlot.put(0, new ItemStack(Items.BUCKET));

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              te.outputSlot.get(0).getItem() == Items.WATER_BUCKET,
              "the pump should fill the bucket with the pumped water, has " + te.outputSlot.get(0));
          helper.assertTrue(
              !helper.getBlockState(WATER_POS).is(Blocks.WATER),
              "the water source block should be drained");
        });
  }

  // regression for the multi-overclocker breakage: the per-tick source cache and the
  // operation length guard must keep an overclocked pump draining sources and filling buckets
  @GameTest(template = EMPTY, timeoutTicks = 200)
  public static void overclockedPumpDrainsWaterSourceIntoBucket(GameTestHelper helper) {
    TileEntityPump te = setupPumpOverWater(helper);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.containerSlot.put(0, new ItemStack(Items.BUCKET));
    te.upgradeSlot.put(0, new ItemStack(Ic2Items.OVERCLOCKER_UPGRADE, 4));
    // a container interaction would trigger this; the direct slot access above does not
    te.setChanged();

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              te.outputSlot.get(0).getItem() == Items.WATER_BUCKET,
              "the overclocked pump should fill the bucket with the pumped water, has "
                  + te.outputSlot.get(0));
          helper.assertTrue(
              !helper.getBlockState(WATER_POS).is(Blocks.WATER),
              "the water source block should be drained");
        });
  }

  // the pump bridges horizontal air gaps to reach fluids that are not directly below it;
  // lava is used because it spreads too slowly (30 tick delay) to reach the pump column first
  @GameTest(template = EMPTY_LARGE, timeoutTicks = 200)
  public static void pumpDrainsLavaSourceAcrossAirGap(GameTestHelper helper) {
    BlockPos lavaPos = new BlockPos(5, 1, 1);
    // enclose the lava source except towards the pump column, 4 blocks of air away
    helper.setBlock(new BlockPos(5, 0, 1), Blocks.STONE);
    helper.setBlock(new BlockPos(5, 1, 0), Blocks.STONE);
    helper.setBlock(new BlockPos(5, 1, 2), Blocks.STONE);
    helper.setBlock(new BlockPos(6, 1, 1), Blocks.STONE);
    helper.setBlock(lavaPos, Blocks.LAVA);
    helper.setBlock(
        PUMP_POS,
        Ic2Blocks.PUMP
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.DOWN));
    TileEntityPump te = getTe(helper, PUMP_POS, TileEntityPump.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.containerSlot.put(0, new ItemStack(Items.BUCKET));

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              te.outputSlot.get(0).getItem() == Items.LAVA_BUCKET,
              "the pump should fill the bucket with the pumped lava, has " + te.outputSlot.get(0));
          helper.assertTrue(
              !helper.getBlockState(lavaPos).is(Blocks.LAVA),
              "the lava source block should be drained");
        });
  }

  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void pumpWithoutEnergyDoesNothing(GameTestHelper helper) {
    TileEntityPump te = setupPumpOverWater(helper);
    te.containerSlot.put(0, new ItemStack(Items.BUCKET));

    helper.runAtTickTime(
        60,
        () -> {
          helper.assertTrue(
              helper.getBlockState(WATER_POS).is(Blocks.WATER),
              "an unpowered pump must not drain the water source");
          helper.assertTrue(
              te.outputSlot.get(0).isEmpty(), "an unpowered pump must not fill the bucket");
          helper.succeed();
        });
  }

  private static TileEntityPump setupPumpOverWater(GameTestHelper helper) {
    // enclose the water source so it cannot flow away
    helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
    helper.setBlock(new BlockPos(0, 1, 1), Blocks.STONE);
    helper.setBlock(new BlockPos(2, 1, 1), Blocks.STONE);
    helper.setBlock(new BlockPos(1, 1, 0), Blocks.STONE);
    helper.setBlock(new BlockPos(1, 1, 2), Blocks.STONE);
    helper.setBlock(WATER_POS, Blocks.WATER);
    helper.setBlock(
        PUMP_POS,
        Ic2Blocks.PUMP
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.DOWN));
    return getTe(helper, PUMP_POS, TileEntityPump.class);
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
