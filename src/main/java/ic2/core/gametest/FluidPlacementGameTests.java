package ic2.core.gametest;

import ic2.core.ref.Ic2Fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
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
    assertPlacedSourceSpreads(helper, Ic2Fluids.HYDROGEN.still());
  }

  // HotWaterBlock.onPlace only scheduled its cool-down block tick, never the fluid tick
  @GameTest(template = TEMPLATE, timeoutTicks = 60)
  public static void placedHotWaterSourceSpreads(GameTestHelper helper) {
    assertPlacedSourceSpreads(helper, Ic2Fluids.HOT_WATER.still());
  }

  // PahoehoeLavaBlock.onPlace only scheduled its basalt-conversion block tick, never the fluid
  // tick
  @GameTest(template = TEMPLATE, timeoutTicks = 60)
  public static void placedPahoehoeLavaSourceSpreads(GameTestHelper helper) {
    assertPlacedSourceSpreads(helper, Ic2Fluids.PAHOEHOE_LAVA.still());
  }

  // UUMatterBlock.neighborChanged now calls the LiquidBlock super; its custom interaction
  // (adjacent lava source -> obsidian) must keep working, and the placed source must still flow
  @GameTest(template = TEMPLATE, timeoutTicks = 60)
  public static void uuMatterKeepsLavaInteractionAndSpreads(GameTestHelper helper) {
    buildBasinFloor(helper);
    helper.setBlock(
        SOURCE_POS, Ic2Fluids.UU_MATTER.still().defaultFluidState().createLegacyBlock());
    helper.setBlock(SOURCE_POS.west(), Blocks.LAVA);

    helper.succeedWhen(
        () -> {
          helper.assertBlockPresent(Blocks.OBSIDIAN, SOURCE_POS.west());
          FluidState east = helper.getLevel().getFluidState(helper.absolutePos(SOURCE_POS.east()));
          helper.assertTrue(
              east.getType().isSame(Ic2Fluids.UU_MATTER.still()),
              "the UU matter source must spread away from the converted lava");
        });
  }

  private static void buildBasinFloor(GameTestHelper helper) {
    for (int x = 0; x <= 2; x++) {
      for (int z = 0; z <= 2; z++) {
        helper.setBlock(new BlockPos(x, 2, z), Blocks.STONE);
      }
    }
  }

  private static void assertPlacedSourceSpreads(GameTestHelper helper, Fluid fluid) {
    buildBasinFloor(helper);
    helper.setBlock(SOURCE_POS, fluid.defaultFluidState().createLegacyBlock());

    BlockPos[] neighbors = {
      SOURCE_POS.west(), SOURCE_POS.east(), SOURCE_POS.north(), SOURCE_POS.south()
    };
    helper.succeedWhen(
        () -> {
          FluidState source = helper.getLevel().getFluidState(helper.absolutePos(SOURCE_POS));
          helper.assertTrue(
              source.isSource() && source.getType().isSame(fluid),
              "the fluid source must remain at the placement position");
          for (BlockPos neighbor : neighbors) {
            FluidState state = helper.getLevel().getFluidState(helper.absolutePos(neighbor));
            if (state.getType().isSame(fluid) && !state.isSource()) {
              return;
            }
          }
          helper.fail("the placed fluid source must spread to a neighbor");
        });
  }
}
