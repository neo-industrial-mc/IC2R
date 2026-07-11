package ic2.core.gametest;

import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.tileentity.TileEntityCentrifuge;
import ic2.core.block.machine.tileentity.TileEntityCompressor;
import ic2.core.block.machine.tileentity.TileEntityTank;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class UpgradeGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);
  private static final BlockPos EAST_POS = MACHINE_POS.east();

  // 4 overclockers: process time x0.7^4 -> 300 ticks * 0.2401 rounds to 72,
  // energy demand x1.6^4 -> 2 EU/t * 6.5536 rounds to 13.
  @GameTest(template = EMPTY, timeoutTicks = 250)
  public static void overclockersSpeedUpProcessingAndRaiseEnergyDemand(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.COMPRESSOR);
    TileEntityCompressor te = getMachine(helper, MACHINE_POS, TileEntityCompressor.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.inputSlot.put(0, new ItemStack(Items.CLAY_BALL, 4));
    te.upgradeSlot.put(0, new ItemStack(Ic2Items.OVERCLOCKER_UPGRADE, 4));
    // a container interaction would trigger this; the direct slot access above does not
    te.setChanged();

    helper.succeedWhen(
        () -> {
          helper.assertValueEqual(te.operationLength, 72, "overclocked operation length");
          helper.assertValueEqual(te.energyConsume, 13, "overclocked energy demand");
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Items.CLAY && output.getCount() == 1,
              "overclocked compressor should produce 1 clay, has " + output);
        });
  }

  // each transformer upgrade raises the accepted voltage tier by 1
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void transformerUpgradesRaiseSinkTier(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.COMPRESSOR);
    TileEntityCompressor te = getMachine(helper, MACHINE_POS, TileEntityCompressor.class);
    helper.assertValueEqual(
        te.getComponent(Energy.class).getSinkTier(), 1, "compressor default sink tier");
    te.upgradeSlot.put(0, new ItemStack(Ic2Items.TRANSFORMER_UPGRADE, 2));
    te.setChanged();

    helper.succeedWhen(
        () ->
            helper.assertValueEqual(
                te.getComponent(Energy.class).getSinkTier(),
                3,
                "sink tier with 2 transformer upgrades"));
  }

  // the upgrade adds 10000 EU; the compressor also always buffers one full operation
  // (300 ticks * 2 EU/t = 600 EU) on top of its default 600 EU capacity -> 11200 EU total.
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void energyStorageUpgradeExtendsEnergyCapacity(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.COMPRESSOR);
    TileEntityCompressor te = getMachine(helper, MACHINE_POS, TileEntityCompressor.class);
    te.upgradeSlot.put(0, new ItemStack(Ic2Items.ENERGY_STORAGE_UPGRADE));
    te.setChanged();

    helper.succeedWhen(
        () ->
            helper.assertValueEqual(
                (int) te.getComponent(Energy.class).getCapacity(),
                11200,
                "capacity with an energy storage upgrade"));
  }

  // an unconfigured ejector pushes output items into any adjacent inventory
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void ejectorUpgradeMovesOutputIntoAdjacentChest(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.COMPRESSOR);
    helper.setBlock(EAST_POS, Blocks.CHEST);
    TileEntityCompressor te = getMachine(helper, MACHINE_POS, TileEntityCompressor.class);
    te.outputSlot.put(0, new ItemStack(Items.CLAY));
    te.upgradeSlot.put(0, new ItemStack(Ic2Items.EJECTOR_UPGRADE));

    helper.succeedWhen(
        () -> {
          helper.assertContainerContains(EAST_POS, Items.CLAY);
          helper.assertTrue(
              te.outputSlot.get(0).isEmpty(), "ejected item should leave the output slot");
        });
  }

  // an ejector configured to a side without an inventory must not eject anywhere else
  @GameTest(template = EMPTY, timeoutTicks = 150)
  public static void ejectorUpgradeOnlyEjectsToItsConfiguredSide(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.COMPRESSOR);
    helper.setBlock(EAST_POS, Blocks.CHEST);
    TileEntityCompressor te = getMachine(helper, MACHINE_POS, TileEntityCompressor.class);
    ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(EAST_POS);
    te.outputSlot.put(0, new ItemStack(Items.CLAY));
    ItemStack ejector = new ItemStack(Ic2Items.EJECTOR_UPGRADE);
    // "dir" is 1 + Direction ordinal, the encoding ItemUpgradeModule.useOn writes
    StackUtil.getOrCreateNbtData(ejector).putByte("dir", (byte) (1 + Direction.WEST.ordinal()));
    te.upgradeSlot.put(0, ejector);

    helper.runAtTickTime(
        100,
        () -> {
          helper.assertTrue(
              chest.isEmpty(), "chest to the east must stay empty with a west-facing ejector");
          helper.assertValueEqual(
              te.outputSlot.get(0).getCount(), 1, "output kept by the west-facing ejector");
          helper.succeed();
        });
  }

  // the pulling upgrade draws valid inputs from an adjacent inventory, which then get processed
  @GameTest(template = EMPTY, timeoutTicks = 450)
  public static void pullingUpgradeDrawsInputFromAdjacentChest(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.COMPRESSOR);
    helper.setBlock(EAST_POS, Blocks.CHEST);
    TileEntityCompressor te = getMachine(helper, MACHINE_POS, TileEntityCompressor.class);
    ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(EAST_POS);
    chest.setItem(0, new ItemStack(Items.CLAY_BALL, 4));
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.upgradeSlot.put(0, new ItemStack(Ic2Items.PULLING_UPGRADE));

    helper.succeedWhen(
        () -> {
          helper.assertTrue(chest.isEmpty(), "pulling upgrade should empty the chest");
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Items.CLAY && output.getCount() == 1,
              "pulled clay balls should be compressed, output has " + output);
        });
  }

  // baseline for the inverter tests: a redstone signal makes the idle thermal centrifuge preheat
  // itself
  @GameTest(template = EMPTY, timeoutTicks = 300)
  public static void redstoneSignalPreheatsIdleCentrifuge(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.CENTRIFUGE);
    helper.setBlock(EAST_POS, Blocks.REDSTONE_BLOCK);
    TileEntityCentrifuge te = getMachine(helper, MACHINE_POS, TileEntityCentrifuge.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.ADVANCED_RE_BATTERY, Double.POSITIVE_INFINITY));

    helper.succeedWhen(
        () ->
            helper.assertTrue(
                te.heat >= 100,
                "centrifuge should preheat from the redstone signal, heat is " + te.heat));
  }

  // the inverter turns "no signal" into a full-strength signal, so the centrifuge preheats without
  // any redstone
  @GameTest(template = EMPTY, timeoutTicks = 300)
  public static void redstoneInverterMakesCentrifugeSeeAbsentSignal(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.CENTRIFUGE);
    TileEntityCentrifuge te = getMachine(helper, MACHINE_POS, TileEntityCentrifuge.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.ADVANCED_RE_BATTERY, Double.POSITIVE_INFINITY));
    te.upgradeSlot.put(0, new ItemStack(Ic2Items.REDSTONE_INVERTER_UPGRADE));

    helper.succeedWhen(
        () ->
            helper.assertTrue(
                te.heat >= 100,
                "inverter should make the idle centrifuge preheat, heat is " + te.heat));
  }

  // the inverter turns an external signal into "no signal", so the centrifuge must not preheat
  @GameTest(template = EMPTY, timeoutTicks = 200)
  public static void redstoneInverterCancelsExternalSignal(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.CENTRIFUGE);
    helper.setBlock(EAST_POS, Blocks.REDSTONE_BLOCK);
    TileEntityCentrifuge te = getMachine(helper, MACHINE_POS, TileEntityCentrifuge.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.ADVANCED_RE_BATTERY, Double.POSITIVE_INFINITY));
    te.upgradeSlot.put(0, new ItemStack(Ic2Items.REDSTONE_INVERTER_UPGRADE));

    helper.runAtTickTime(
        150,
        () -> {
          helper.assertValueEqual((int) te.heat, 0, "heat with an inverted redstone signal");
          helper.succeed();
        });
  }

  // the slot only accepts upgrades matching the block's upgradable properties
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void upgradeSlotOnlyAcceptsSuitableUpgrades(GameTestHelper helper) {
    BlockPos tankPos = MACHINE_POS.north();
    helper.setBlock(MACHINE_POS, Ic2Blocks.COMPRESSOR);
    helper.setBlock(tankPos, Ic2Blocks.TANK);
    TileEntityCompressor compressor = getMachine(helper, MACHINE_POS, TileEntityCompressor.class);
    TileEntityTank tank = getMachine(helper, tankPos, TileEntityTank.class);

    assertAccepts(
        helper,
        compressor.upgradeSlot,
        "compressor",
        true,
        Ic2Items.OVERCLOCKER_UPGRADE,
        Ic2Items.TRANSFORMER_UPGRADE,
        Ic2Items.ENERGY_STORAGE_UPGRADE,
        Ic2Items.EJECTOR_UPGRADE,
        Ic2Items.PULLING_UPGRADE);
    assertAccepts(
        helper,
        compressor.upgradeSlot,
        "compressor",
        false,
        Ic2Items.FLUID_EJECTOR_UPGRADE,
        Ic2Items.FLUID_PULLING_UPGRADE,
        Ic2Items.REDSTONE_INVERTER_UPGRADE);
    assertAccepts(
        helper,
        tank.upgradeSlot,
        "tank",
        true,
        Ic2Items.FLUID_EJECTOR_UPGRADE,
        Ic2Items.FLUID_PULLING_UPGRADE);
    assertAccepts(
        helper,
        tank.upgradeSlot,
        "tank",
        false,
        Ic2Items.OVERCLOCKER_UPGRADE,
        Ic2Items.EJECTOR_UPGRADE);
    helper.succeed();
  }

  // the fluid ejector moves up to 50 mB/t, so 1000 mB of water drains within 20 ticks
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void fluidEjectorUpgradePushesFluidToAdjacentTank(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.TANK);
    helper.setBlock(EAST_POS, Ic2Blocks.TANK);
    TileEntityTank source = getMachine(helper, MACHINE_POS, TileEntityTank.class);
    Ic2FluidTank sourceTank = getTank(helper, MACHINE_POS);
    Ic2FluidTank destTank = getTank(helper, EAST_POS);
    int filled =
        sourceTank.fillMb(
            Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000), false);
    helper.assertValueEqual(filled, 1000, "water accepted by the source tank");
    source.upgradeSlot.put(0, new ItemStack(Ic2Items.FLUID_EJECTOR_UPGRADE));

    helper.succeedWhen(
        () -> {
          assertTankHasWater(helper, destTank, "tank next to the fluid ejector");
          helper.assertTrue(
              sourceTank.isEmpty(), "source tank should be drained by the fluid ejector");
        });
  }

  // the fluid pulling upgrade drains adjacent fluid handlers into its own block
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void fluidPullingUpgradeDrawsFluidFromAdjacentTank(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.TANK);
    helper.setBlock(EAST_POS, Ic2Blocks.TANK);
    TileEntityTank puller = getMachine(helper, MACHINE_POS, TileEntityTank.class);
    Ic2FluidTank pullerTank = getTank(helper, MACHINE_POS);
    Ic2FluidTank sourceTank = getTank(helper, EAST_POS);
    int filled =
        sourceTank.fillMb(
            Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000), false);
    helper.assertValueEqual(filled, 1000, "water accepted by the source tank");
    puller.upgradeSlot.put(0, new ItemStack(Ic2Items.FLUID_PULLING_UPGRADE));

    helper.succeedWhen(
        () -> {
          assertTankHasWater(helper, pullerTank, "tank with the fluid pulling upgrade");
          helper.assertTrue(
              sourceTank.isEmpty(), "source tank should be drained by the fluid pulling upgrade");
        });
  }

  private static void assertTankHasWater(GameTestHelper helper, Ic2FluidTank tank, String what) {
    Ic2FluidStack stack = tank.getFluidStack();
    helper.assertTrue(
        stack != null
            && stack.getFluid() == net.minecraft.world.level.material.Fluids.WATER
            && stack.getAmountMb() == 1000,
        what
            + " should contain 1000 mB of water, has "
            + (stack == null || stack.isEmpty()
                ? "nothing"
                : stack.getAmountMb() + " mB of " + stack.getFluid()));
  }

  private static void assertAccepts(
      GameTestHelper helper,
      InvSlotUpgrade slot,
      String machine,
      boolean expected,
      Item... upgrades) {
    for (Item upgrade : upgrades) {
      helper.assertTrue(
          slot.accepts(new ItemStack(upgrade)) == expected,
          machine + " upgrade slot should " + (expected ? "accept " : "reject ") + upgrade);
    }
  }

  private static Ic2FluidTank getTank(GameTestHelper helper, BlockPos pos) {
    return getMachine(helper, pos, TileEntityTank.class)
        .getComponent(Fluids.class)
        .getAllTanks()
        .iterator()
        .next();
  }

  private static <T extends BlockEntity> T getMachine(
      GameTestHelper helper, BlockPos pos, Class<T> type) {
    BlockEntity be = helper.getBlockEntity(pos);
    if (!type.isInstance(be)) {
      throw new IllegalStateException(
          "expected " + type.getSimpleName() + " at " + pos + ", found " + be);
    }

    return type.cast(be);
  }
}
