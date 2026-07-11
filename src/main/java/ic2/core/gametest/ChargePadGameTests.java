package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.core.block.wiring.tileentity.TileEntityChargePadBlock;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ChargePadGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  @GameTest(template = EMPTY)
  public static void batboxChargePadChargesWornItems(GameTestHelper helper) {
    runChargePadTest(helper, Ic2Blocks.BATBOX_CHARGEPAD, 32.0);
  }

  @GameTest(template = EMPTY)
  public static void cesuChargePadChargesWornItems(GameTestHelper helper) {
    runChargePadTest(helper, Ic2Blocks.CESU_CHARGEPAD, 128.0);
  }

  @GameTest(template = EMPTY)
  public static void mfeChargePadChargesWornItems(GameTestHelper helper) {
    runChargePadTest(helper, Ic2Blocks.MFE_CHARGEPAD, 512.0);
  }

  @GameTest(template = EMPTY)
  public static void mfsuChargePadChargesWornItems(GameTestHelper helper) {
    runChargePadTest(helper, Ic2Blocks.MFSU_CHARGEPAD, 2048.0);
  }

  // each pad charges the items worn by a player standing on it at the pad's output EU/t
  private static void runChargePadTest(GameTestHelper helper, Block padBlock, double output) {
    BlockPos pos = new BlockPos(1, 0, 1);
    helper.setBlock(pos, padBlock);
    TileEntityChargePadBlock pad = getPad(helper, pos);
    double initialEnergy = output * 60.0;
    pad.energy.addEnergy(initialEnergy);

    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack lappack = new ItemStack(Ic2Items.LAPPACK);
    player.setItemSlot(EquipmentSlot.CHEST, lappack);

    // pads only notice colliding players, so simulate standing on the pad every tick
    helper.onEachTick(
        () ->
            helper
                .getBlockState(pos)
                .entityInside(helper.getLevel(), helper.absolutePos(pos), player));

    // 20 ticks worth of the pad's output rate
    double target = output * 20.0;
    helper.succeedWhen(
        () -> {
          double charge = ElectricItem.manager.getCharge(lappack);
          helper.assertTrue(
              charge >= target,
              "worn lappack should charge at the pad's rate, has " + charge + " of " + target);
          Ic2GameTestAssertions.assertNear(
              helper,
              pad.energy.getEnergy() + charge,
              initialEnergy,
              "pad energy plus item charge");
          helper.assertTrue(pad.getActive(), "pad should be active while charging");
        });
  }

  private static TileEntityChargePadBlock getPad(GameTestHelper helper, BlockPos pos) {
    BlockEntity be = helper.getBlockEntity(pos);
    if (!(be instanceof TileEntityChargePadBlock pad)) {
      throw new IllegalStateException("expected a charge pad at " + pos + ", found " + be);
    }

    return pad;
  }
}
