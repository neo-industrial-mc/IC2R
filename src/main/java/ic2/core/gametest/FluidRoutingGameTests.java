package ic2.core.gametest;

import ic2.core.block.comp.Fluids;
import ic2.core.block.machine.tileentity.TileEntityTank;
import ic2.core.block.machine.tileentity.TileEntityWeightedFluidDistributor;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class FluidRoutingGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);
  private static final BlockPos EAST_POS = new BlockPos(2, 1, 1);
  private static final BlockPos WEST_POS = new BlockPos(0, 1, 1);

  // weighted fluid distributor: pushes its tank content to the highest priority side with room
  @GameTest(template = EMPTY, timeoutTicks = 200)
  public static void weightedFluidDistributorPrefersFirstPriority(GameTestHelper helper) {
    helper.setBlock(EAST_POS, Ic2Blocks.TANK);
    helper.setBlock(WEST_POS, Ic2Blocks.TANK);
    helper.setBlock(
        MACHINE_POS,
        Ic2Blocks.WEIGHTED_FLUID_DISTRIBUTOR
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.UP));
    TileEntityWeightedFluidDistributor te =
        getTe(helper, MACHINE_POS, TileEntityWeightedFluidDistributor.class);
    te.getPriority().add(Direction.EAST);
    te.getPriority().add(Direction.WEST);

    int filled =
        te.fluidTank.fillMb(
            Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000), false);
    helper.assertValueEqual(filled, 1000, "water accepted by the distributor tank");

    helper.succeedWhen(
        () -> {
          helper.assertValueEqual(
              getTankAmount(helper, EAST_POS), 1000, "water in the first priority tank");
          helper.assertValueEqual(
              getTankAmount(helper, WEST_POS), 0, "water in the second priority tank");
          helper.assertValueEqual(
              te.fluidTank.getFluidAmount(), 0, "water left in the distributor");
        });
  }

  private static int getTankAmount(GameTestHelper helper, BlockPos pos) {
    TileEntityTank tank = getTe(helper, pos, TileEntityTank.class);
    return tank.getComponent(Fluids.class).getAllTanks().iterator().next().getFluidAmount();
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
