package ic2.core.energy.grid;

import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public final class EnergyCalculatorUnifiedGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final BlockPos SOURCE_POS = new BlockPos(1, 2, 1);
  private static final BlockPos SINK_POS = new BlockPos(1, 1, 1);

  private EnergyCalculatorUnifiedGameTests() {}

  @GameTest(template = EMPTY)
  public static void cacheRebuildIgnoresLinkToNodeOutsideGrid(GameTestHelper helper) {
    helper.setBlock(SOURCE_POS, Ic2Blocks.BATBOX);
    helper.setBlock(SINK_POS, Ic2Blocks.MACERATOR);

    helper.runAtTickTime(
        10,
        () -> {
          EnergyNetLocal enet = EnergyNetGlobal.getLocal(helper.getLevel());
          Tile sourceTile = enet.getTile(helper.absolutePos(SOURCE_POS));
          Tile sinkTile = enet.getTile(helper.absolutePos(SINK_POS));
          helper.assertTrue(sourceTile != null, "source must be registered in the energy net");
          helper.assertTrue(sinkTile != null, "sink must be registered in the energy net");

          Node source = sourceTile.nodes.get(0);
          Node sink = sinkTile.nodes.get(0);
          Grid grid = source.getGrid();
          helper.assertTrue(grid == sink.getGrid(), "source and sink must share a grid");

          Node detachedSink = new Node(enet.allocateNodeId(), sink.tile, sink.nodeType);
          NodeLink danglingLink = new NodeLink(source, detachedSink, 0.0);
          source.links.add(danglingLink);
          try {
            helper.assertTrue(
                detachedSink.getGrid() == null,
                "the dangling link endpoint must be outside the grid snapshot");
            new EnergyCalculatorUnified().handleGridChange(grid);
          } finally {
            source.links.remove(danglingLink);
          }

          helper.succeed();
        });
  }
}
