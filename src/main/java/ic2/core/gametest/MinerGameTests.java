package ic2.core.gametest;

import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityMiner;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class MinerGameTests {
  private static final String EMPTY_TALL = "gametest/empty3x9x3";

  private static final BlockPos MINER_POS = new BlockPos(1, 6, 1);

  // diamond drill: 50 ticks and 20 EU/t per block, one mining pipe per level
  @GameTest(template = EMPTY_TALL, timeoutTicks = 400)
  public static void minerDigsDownPlacingMiningPipe(GameTestHelper helper) {
    TileEntityMiner te = setupMiner(helper);
    te.drillSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.DIAMOND_DRILL, Double.POSITIVE_INFINITY));
    te.pipeSlot.put(0, new ItemStack(Ic2Items.MINING_PIPE, 8));
    helper.setBlock(new BlockPos(1, 5, 1), Blocks.DIRT);
    helper.setBlock(new BlockPos(1, 4, 1), Blocks.DIRT);
    helper.setBlock(new BlockPos(1, 3, 1), Blocks.BEDROCK);

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              helper.getBlockState(new BlockPos(1, 5, 1)).is(Ic2Blocks.MINING_PIPE),
              "the first dug level should hold a mining pipe, is "
                  + helper.getBlockState(new BlockPos(1, 5, 1)));
          helper.assertValueEqual(
              countItems(te.buffer, Items.DIRT), 2, "dirt stored in the miner buffer");
        });
  }

  // OD scanner: scans a 3 block radius per layer and mines any ore it finds
  @GameTest(template = EMPTY_TALL, timeoutTicks = 400)
  public static void minerWithOdScannerMinesOreOnLayer(GameTestHelper helper) {
    scannerMinesSideOre(helper, Ic2Items.SCANNER);
  }

  // OV scanner: same behavior with a 6 block radius
  @GameTest(template = EMPTY_TALL, timeoutTicks = 400)
  public static void minerWithOvScannerMinesOreOnLayer(GameTestHelper helper) {
    scannerMinesSideOre(helper, Ic2Items.ADVANCED_SCANNER);
  }

  private static void scannerMinesSideOre(GameTestHelper helper, Item scanner) {
    TileEntityMiner te = setupMiner(helper);
    te.drillSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.DIAMOND_DRILL, Double.POSITIVE_INFINITY));
    te.scannerSlot.put(0, ElectricItemManager.getCharged(scanner, Double.POSITIVE_INFINITY));
    te.pipeSlot.put(0, new ItemStack(Ic2Items.MINING_PIPE, 8));
    helper.setBlock(new BlockPos(0, 5, 1), Blocks.IRON_ORE);
    helper.setBlock(new BlockPos(1, 4, 1), Blocks.BEDROCK);

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              helper.getBlockState(new BlockPos(0, 5, 1)).isAir(),
              "the scanner should have located and mined the iron ore");
          helper.assertValueEqual(
              countItems(te.buffer, Items.RAW_IRON), 1, "raw iron stored in the miner buffer");
        });
  }

  // without a drill the miner retracts the pipe column, 20 ticks and 60 EU per pipe
  @GameTest(template = EMPTY_TALL, timeoutTicks = 300)
  public static void minerWithoutDrillWithdrawsPipes(GameTestHelper helper) {
    TileEntityMiner te = setupMiner(helper);
    helper.setBlock(new BlockPos(1, 5, 1), Ic2Blocks.MINING_PIPE);
    helper.setBlock(new BlockPos(1, 4, 1), Ic2Blocks.MINING_PIPE_TIP);

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              helper.getBlockState(new BlockPos(1, 5, 1)).isAir(),
              "the withdrawn pipe should be removed from the world");
          helper.assertTrue(
              helper.getBlockState(new BlockPos(1, 4, 1)).isAir(),
              "the withdrawn pipe tip should be removed from the world");
          helper.assertValueEqual(
              countItems(te.buffer, Ic2Items.MINING_PIPE),
              2,
              "mining pipes returned to the miner buffer");
        });
  }

  private static TileEntityMiner setupMiner(GameTestHelper helper) {
    helper.setBlock(MINER_POS, Ic2Blocks.MINER);
    TileEntityMiner te = getTe(helper, MINER_POS, TileEntityMiner.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    return te;
  }

  private static int countItems(InvSlot slot, Item item) {
    int total = 0;

    for (int i = 0; i < slot.size(); i++) {
      ItemStack stack = slot.get(i);
      if (!StackUtil.isEmpty(stack) && stack.getItem() == item) {
        total += stack.getCount();
      }
    }

    return total;
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
