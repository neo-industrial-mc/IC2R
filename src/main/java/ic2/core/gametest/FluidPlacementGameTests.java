package ic2.core.gametest;

import ic2.core.ref.Ic2Fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class FluidPlacementGameTests {
  private static final String TEMPLATE = "gametest/empty3x9x3";
  private static final BlockPos SOURCE_POS = new BlockPos(1, 3, 1);

  // HydrogenBlock overrides onPlace/neighborChanged; if the LiquidBlock supers are skipped, a
  // placed source never gets a fluid tick scheduled and never spreads (upstream 8bee6d7b).
  @GameTest(template = TEMPLATE, timeoutTicks = 60)
  public static void placedHydrogenSourceSpreads(GameTestHelper helper) {
    for (int x = 0; x <= 2; x++) {
      for (int z = 0; z <= 2; z++) {
        helper.setBlock(new BlockPos(x, 2, z), Blocks.STONE);
      }
    }
    helper.setBlock(
        SOURCE_POS, Ic2Fluids.HYDROGEN.still().defaultFluidState().createLegacyBlock());

    BlockPos[] neighbors = {
      SOURCE_POS.west(), SOURCE_POS.east(), SOURCE_POS.north(), SOURCE_POS.south()
    };
    helper.succeedWhen(
        () -> {
          FluidState source = helper.getLevel().getFluidState(helper.absolutePos(SOURCE_POS));
          helper.assertTrue(
              source.isSource() && source.getType().isSame(Ic2Fluids.HYDROGEN.still()),
              "hydrogen source must remain at the placement position");
          for (BlockPos neighbor : neighbors) {
            FluidState state = helper.getLevel().getFluidState(helper.absolutePos(neighbor));
            if (state.getType().isSame(Ic2Fluids.HYDROGEN.still()) && !state.isSource()) {
              return;
            }
          }
          helper.fail("placed hydrogen source must spread flowing hydrogen to a neighbor");
        });
  }
}
