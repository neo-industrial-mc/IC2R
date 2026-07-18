package ic2.core.gametest;

import ic2.core.item.ItemClassicCell;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class AirCellGameTests {
  private static final String TEMPLATE = "gametest/empty3x9x3";
  private static final BlockPos TARGET_POS = new BlockPos(1, 3, 1);

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void compressedAirCellCannotBePlaced(GameTestHelper helper) {
    ItemClassicCell airCell = (ItemClassicCell) Ic2Items.AIR_CELL;
    boolean placed =
        airCell.emptyContents(null, helper.getLevel(), helper.absolutePos(TARGET_POS), null);

    helper.assertTrue(!placed, "compressed air cell should not place its fluid");
    helper.assertBlockPresent(Blocks.AIR, TARGET_POS);
    helper.succeed();
  }
}
