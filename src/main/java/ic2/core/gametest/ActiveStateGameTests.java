package ic2.core.gametest;

import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ActiveStateGameTests {
  private static final String TEMPLATE = "gametest/empty3x3x3";
  private static final BlockPos POS = new BlockPos(1, 1, 1);

  // TEs that call Ic2TileEntity.setActive directly (trade-o-mat, kinetic generators, liquid heat
  // exchanger) only sent a network field update; the server blockstate stayed active=false, so any
  // later chunk resync snapped clients back to the idle model (upstream 4d98c9cb).
  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void setActiveSyncsServerBlockstate(GameTestHelper helper) {
    helper.setBlock(POS, Ic2Blocks.CREATIVE_GENERATOR);
    Ic2TileEntity te = (Ic2TileEntity) helper.getBlockEntity(POS);
    helper.assertTrue(te != null, "creative generator must have a tile entity");
    te.setActive(true);

    helper.runAfterDelay(
        1,
        () -> {
          helper.assertTrue(te.getActive(), "tile entity must report active");
          helper.assertTrue(
              helper.getBlockState(POS).getValue(Ic2TileEntityBlock.ACTIVE),
              "setActive must flip the server-side blockstate ACTIVE property");
          te.setActive(false);
          helper.assertTrue(
              !helper.getBlockState(POS).getValue(Ic2TileEntityBlock.ACTIVE),
              "setActive(false) must clear the server-side blockstate ACTIVE property");
          helper.succeed();
        });
  }
}
