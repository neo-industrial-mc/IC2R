package ic2.core.gametest;

import ic2.core.block.wiring.AbstractCableBlock;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class CableGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final BlockPos WEST_CABLE_POS = new BlockPos(1, 1, 1);
  private static final BlockPos EAST_CABLE_POS = new BlockPos(2, 1, 1);

  // cables connect when placed next to each other and drop the connection again once the neighbor
  // is gone
  @GameTest(template = EMPTY)
  public static void cableConnectionStateFollowsNeighbors(GameTestHelper helper) {
    helper.setBlock(WEST_CABLE_POS, Ic2Blocks.COPPER_CABLE);
    helper.setBlock(EAST_CABLE_POS, Ic2Blocks.COPPER_CABLE);

    helper.assertBlockProperty(WEST_CABLE_POS, AbstractCableBlock.EAST, true);
    helper.assertBlockProperty(EAST_CABLE_POS, AbstractCableBlock.WEST, true);
    helper.assertBlockProperty(WEST_CABLE_POS, AbstractCableBlock.WEST, false);
    helper.assertBlockProperty(WEST_CABLE_POS, AbstractCableBlock.UP, false);

    helper.setBlock(EAST_CABLE_POS, Blocks.AIR);

    helper.assertBlockProperty(WEST_CABLE_POS, AbstractCableBlock.EAST, false);
    helper.succeed();
  }

  // a machine placed and removed next to an existing cable must update the cable's connection via
  // neighborChanged
  @GameTest(template = EMPTY)
  public static void cableDisconnectsFromRemovedMachine(GameTestHelper helper) {
    helper.setBlock(WEST_CABLE_POS, Ic2Blocks.COPPER_CABLE);
    helper.setBlock(EAST_CABLE_POS, Ic2Blocks.MACERATOR);

    helper.assertBlockProperty(WEST_CABLE_POS, AbstractCableBlock.EAST, true);

    helper.setBlock(EAST_CABLE_POS, Blocks.AIR);

    helper.assertBlockProperty(WEST_CABLE_POS, AbstractCableBlock.EAST, false);
    helper.succeed();
  }
}
