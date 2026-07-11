package ic2.core.gametest;

import ic2.core.block.comp.Fluids;
import ic2.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import ic2.core.block.machine.tileentity.TileEntityBlastFurnace;
import ic2.core.block.machine.tileentity.TileEntityBlockCutter;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.block.machine.tileentity.TileEntityCentrifuge;
import ic2.core.block.machine.tileentity.TileEntityCompressor;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityExtractor;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.block.machine.tileentity.TileEntityIronFurnace;
import ic2.core.block.machine.tileentity.TileEntityMetalFormer;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.block.machine.tileentity.TileEntitySolidCanner;
import ic2.core.block.machine.tileentity.TileEntityTank;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ProcessingMachineGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);

  // compressor: 2 EU/t over 300 ticks, tier 1; 4 clay balls -> 1 clay block
  @GameTest(template = EMPTY, timeoutTicks = 400)
  public static void compressorCompressesClayBallsToClay(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.COMPRESSOR);
    TileEntityCompressor te = getMachine(helper, TileEntityCompressor.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.inputSlot.put(0, new ItemStack(Items.CLAY_BALL, 4));

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Items.CLAY && output.getCount() == 1,
              "compressor should produce 1 clay, has " + output);
          helper.assertTrue(te.inputSlot.get(0).isEmpty(), "compressor input should be consumed");
        });
  }

  // extractor: 2 EU/t over 300 ticks, tier 1; sticky resin -> 3 rubber
  @GameTest(template = EMPTY, timeoutTicks = 400)
  public static void extractorExtractsResinToRubber(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.EXTRACTOR);
    TileEntityExtractor te = getMachine(helper, TileEntityExtractor.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.inputSlot.put(0, new ItemStack(Ic2Items.RESIN));

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Ic2Items.RUBBER && output.getCount() == 3,
              "extractor should produce 3 rubber, has " + output);
          helper.assertTrue(te.inputSlot.get(0).isEmpty(), "extractor input should be consumed");
        });
  }

  // recycler: 1 EU/t over 45 ticks, tier 1; scrap has a 1/8 (12.5%) chance per operation.
  // feed a full stack and assert at least one scrap drops within the timeout.
  @GameTest(template = EMPTY, timeoutTicks = 3200)
  public static void recyclerProducesScrapFromCobblestone(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.RECYCLER);
    TileEntityRecycler te = getMachine(helper, TileEntityRecycler.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.inputSlot.put(0, new ItemStack(Items.COBBLESTONE, 64));

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Ic2Items.SCRAP && output.getCount() >= 1,
              "recycler should eventually produce scrap, has " + output);
        });
  }

  // recycler blacklist: sticks are on the default balance blacklist, so they are consumed without
  // producing scrap.
  @GameTest(template = EMPTY, timeoutTicks = 120)
  public static void recyclerBlacklistedInputProducesNoScrap(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.RECYCLER);
    TileEntityRecycler te = getMachine(helper, TileEntityRecycler.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.inputSlot.put(0, new ItemStack(Items.STICK));

    // one operation (45 ticks) is enough to consume the blacklisted input
    helper.runAtTickTime(
        90,
        () -> {
          helper.assertTrue(te.inputSlot.get(0).isEmpty(), "blacklisted stick should be consumed");
          helper.assertTrue(
              te.outputSlot.get(0).isEmpty(),
              "blacklisted input must not produce scrap, has " + te.outputSlot.get(0));
          helper.succeed();
        });
  }

  // thermal centrifuge: 48 EU/t over 500 ticks, tier 2; cobblestone -> stone dust requires minHeat
  // 100.
  // preheat the machine directly so the recipe is valid from the first tick.
  @GameTest(template = EMPTY, timeoutTicks = 700)
  public static void centrifugeProcessesCobblestoneWhenHot(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.CENTRIFUGE);
    TileEntityCentrifuge te = getMachine(helper, TileEntityCentrifuge.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.ADVANCED_RE_BATTERY, Double.POSITIVE_INFINITY));
    te.heat = 100;
    te.inputSlot.put(0, new ItemStack(Items.COBBLESTONE));

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Ic2Items.STONE_DUST && output.getCount() == 1,
              "centrifuge should produce 1 stone dust, has " + output);
          helper.assertTrue(te.inputSlot.get(0).isEmpty(), "centrifuge input should be consumed");
        });
  }

  // metal former, extruding mode (0): copper ingot -> 3 copper cable
  @GameTest(template = EMPTY, timeoutTicks = 300)
  public static void metalFormerExtrudesCopperIngotToCable(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.METAL_FORMER);
    TileEntityMetalFormer te = getMachine(helper, TileEntityMetalFormer.class);
    te.setMode(0);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.inputSlot.put(0, new ItemStack(Items.COPPER_INGOT));

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Ic2Items.COPPER_CABLE && output.getCount() == 3,
              "extruding should produce 3 copper cable, has " + output);
          helper.assertTrue(te.inputSlot.get(0).isEmpty(), "metal former input should be consumed");
        });
  }

  // metal former, rolling mode (1): iron ingot -> iron plate
  @GameTest(template = EMPTY, timeoutTicks = 300)
  public static void metalFormerRollsIronIngotToPlate(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.METAL_FORMER);
    TileEntityMetalFormer te = getMachine(helper, TileEntityMetalFormer.class);
    te.setMode(1);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.inputSlot.put(0, new ItemStack(Items.IRON_INGOT));

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Ic2Items.IRON_PLATE && output.getCount() == 1,
              "rolling should produce 1 iron plate, has " + output);
          helper.assertTrue(te.inputSlot.get(0).isEmpty(), "metal former input should be consumed");
        });
  }

  // metal former, cutting mode (2): iron casing -> coin
  @GameTest(template = EMPTY, timeoutTicks = 300)
  public static void metalFormerCutsIronCasingToCoin(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.METAL_FORMER);
    TileEntityMetalFormer te = getMachine(helper, TileEntityMetalFormer.class);
    te.setMode(2);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.inputSlot.put(0, new ItemStack(Ic2Items.IRON_CASING));

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Ic2Items.COIN && output.getCount() == 1,
              "cutting should produce 1 coin, has " + output);
          helper.assertTrue(te.inputSlot.get(0).isEmpty(), "metal former input should be consumed");
        });
  }

  // ore washer: 16 EU/t over 500 ticks, tier 1; needs 1000 mb water in its tank.
  // fill the tank from water cells placed in the fluid slot, then wash crushed iron.
  @GameTest(template = EMPTY, timeoutTicks = 700)
  public static void oreWasherWashesCrushedIron(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.ORE_WASHING_PLANT);
    TileEntityOreWashing te = getMachine(helper, TileEntityOreWashing.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.fluidSlot.put(0, new ItemStack(Ic2Items.WATER_CELL, 2));
    te.inputSlot.put(0, new ItemStack(Ic2Items.CRUSHED_IRON));

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              hasOutput(te.outputSlot, Ic2Items.PURIFIED_IRON),
              "ore washer should produce purified iron, outputs " + describe(te.outputSlot));
          helper.assertTrue(te.inputSlot.get(0).isEmpty(), "ore washer input should be consumed");
        });
  }

  // blast furnace: no EU, needs heat >= 50000 and (compressed) air fluid; crushed iron -> steel
  // ingot + slag.
  // heat and progress are set directly for a deterministic, fast-completing test (recipe duration
  // is 6000 ticks).
  @GameTest(template = EMPTY, timeoutTicks = 200)
  public static void blastFurnaceSmeltsCrushedIronToSteel(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.BLAST_FURNACE);
    TileEntityBlastFurnace te = getMachine(helper, TileEntityBlastFurnace.class);
    te.heat = 55000;
    te.tankInputSlot.put(0, new ItemStack(Ic2Items.AIR_CELL));
    te.inputSlot.put(0, new ItemStack(Ic2Items.CRUSHED_IRON));
    setBlastFurnaceProgress(te, 5990);

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              hasOutput(te.outputSlot, Ic2Items.STEEL_INGOT),
              "blast furnace should produce a steel ingot, outputs " + describe(te.outputSlot));
          helper.assertTrue(
              te.inputSlot.get(0).isEmpty(), "blast furnace input should be consumed");
        });
  }

  // block cutter: 4 EU/t over 450 ticks, tier 1; needs a cutting blade of sufficient hardness.
  // iron block requires hardness 5; the steel blade (hardness 6) can cut it into 9 iron plates.
  @GameTest(template = EMPTY, timeoutTicks = 600)
  public static void blockCutterCutsIronBlockWithSteelBlade(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.BLOCK_CUTTER);
    TileEntityBlockCutter te = getMachine(helper, TileEntityBlockCutter.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.cutterSlot.put(0, new ItemStack(Ic2Items.STEEL_CUTTING_BLADE));
    te.inputSlot.put(0, new ItemStack(Blocks.IRON_BLOCK));

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Ic2Items.IRON_PLATE && output.getCount() == 9,
              "block cutter should produce 9 iron plates, has " + output);
          helper.assertTrue(te.inputSlot.get(0).isEmpty(), "block cutter input should be consumed");
        });
  }

  // block cutter: an iron blade (hardness 3) is too weak for an iron block (hardness 5), so nothing
  // is cut.
  @GameTest(template = EMPTY, timeoutTicks = 120)
  public static void blockCutterWeakBladeProducesNothing(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.BLOCK_CUTTER);
    TileEntityBlockCutter te = getMachine(helper, TileEntityBlockCutter.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.cutterSlot.put(0, new ItemStack(Ic2Items.IRON_CUTTING_BLADE));
    te.inputSlot.put(0, new ItemStack(Blocks.IRON_BLOCK));

    helper.runAtTickTime(
        100,
        () -> {
          helper.assertTrue(
              te.outputSlot.get(0).isEmpty(),
              "too-weak blade must not cut anything, has " + te.outputSlot.get(0));
          helper.assertValueEqual(
              te.inputSlot.get(0).getCount(), 1, "block cutter input with weak blade");
          helper.succeed();
        });
  }

  // solid canner: 2 EU/t over 200 ticks, tier 1; 2 tin cans + 1 cod -> 2 filled tin cans
  @GameTest(template = EMPTY, timeoutTicks = 300)
  public static void solidCannerFillsTinCansWithFood(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.SOLID_CANNER);
    TileEntitySolidCanner te = getMachine(helper, TileEntitySolidCanner.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.canInputSlot.put(0, new ItemStack(Ic2Items.TIN_CAN, 2));
    te.inputSlot.put(0, new ItemStack(Items.COD));

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Ic2Items.FILLED_TIN_CAN && output.getCount() == 2,
              "solid canner should produce 2 filled tin cans, has " + output);
          helper.assertTrue(
              te.inputSlot.get(0).isEmpty(), "solid canner food input should be consumed");
          helper.assertTrue(
              te.canInputSlot.get(0).isEmpty(), "solid canner tin cans should be consumed");
        });
  }

  // iron furnace: no EU, burns furnace fuel to run vanilla smelting recipes over 160 ticks
  @GameTest(template = EMPTY, timeoutTicks = 300)
  public static void ironFurnaceSmeltsRawIronWithCoal(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.IRON_FURNACE);
    TileEntityIronFurnace te = getMachine(helper, TileEntityIronFurnace.class);
    te.fuelSlot.put(0, new ItemStack(Items.COAL));
    te.inputSlot.put(0, new ItemStack(Items.RAW_IRON));

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Items.IRON_INGOT && output.getCount() == 1,
              "iron furnace should produce 1 iron ingot, has " + output);
          helper.assertTrue(te.inputSlot.get(0).isEmpty(), "iron furnace input should be consumed");
          helper.assertTrue(te.fuel > 0, "iron furnace should still be burning the consumed coal");
        });
  }

  // canner, bottle solid mode: an empty fuel rod is filled with uranium
  @GameTest(template = EMPTY, timeoutTicks = 300)
  public static void cannerBottlesUraniumIntoFuelRod(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.CANNER);
    TileEntityCanner te = getMachine(helper, TileEntityCanner.class);
    te.setMode(TileEntityCanner.Mode.BottleSolid);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.canInputSlot.put(0, new ItemStack(Ic2Items.FUEL_ROD));
    te.inputSlot.put(0, new ItemStack(Ic2Items.URANIUM));

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Ic2Items.URANIUM_FUEL_ROD && output.getCount() == 1,
              "canner should produce 1 uranium fuel rod, has " + output);
          helper.assertTrue(te.inputSlot.get(0).isEmpty(), "canner uranium should be consumed");
          helper.assertTrue(te.canInputSlot.get(0).isEmpty(), "canner fuel rod should be consumed");
        });
  }

  // canner, bottle liquid mode: an empty cell is filled with 1000 mB water from the input tank
  @GameTest(template = EMPTY, timeoutTicks = 300)
  public static void cannerFillsEmptyCellFromInputTank(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.CANNER);
    TileEntityCanner te = getMachine(helper, TileEntityCanner.class);
    te.setMode(TileEntityCanner.Mode.BottleLiquid);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    int filled =
        te.inputTank.fillMb(
            Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000), false);
    helper.assertValueEqual(filled, 1000, "water accepted by the canner input tank");
    te.canInputSlot.put(0, new ItemStack(Ic2Items.EMPTY_CELL));

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Ic2Items.WATER_CELL && output.getCount() == 1,
              "canner should produce 1 water cell, has " + output);
          helper.assertTrue(
              te.canInputSlot.get(0).isEmpty(), "canner empty cell should be consumed");
          helper.assertTrue(te.inputTank.isEmpty(), "canner water should be consumed");
        });
  }

  // canner, empty liquid mode: a water cell is drained into the output tank, leaving an empty cell
  @GameTest(template = EMPTY, timeoutTicks = 300)
  public static void cannerDrainsWaterCellIntoOutputTank(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.CANNER);
    TileEntityCanner te = getMachine(helper, TileEntityCanner.class);
    te.setMode(TileEntityCanner.Mode.EmptyLiquid);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    te.canInputSlot.put(0, new ItemStack(Ic2Items.WATER_CELL));

    helper.succeedWhen(
        () -> {
          assertTankContains(
              helper,
              te.outputTank,
              net.minecraft.world.level.material.Fluids.WATER,
              1000,
              "canner output tank");
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Ic2Items.EMPTY_CELL && output.getCount() == 1,
              "canner should return 1 empty cell, has " + output);
          helper.assertTrue(
              te.canInputSlot.get(0).isEmpty(), "canner water cell should be consumed");
        });
  }

  // canner, enrich mode: 1000 mB water + 8 lapis dust -> 1000 mB coolant in the output tank
  @GameTest(template = EMPTY, timeoutTicks = 300)
  public static void cannerEnrichesWaterWithLapisIntoCoolant(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.CANNER);
    TileEntityCanner te = getMachine(helper, TileEntityCanner.class);
    te.setMode(TileEntityCanner.Mode.EnrichLiquid);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    int filled =
        te.inputTank.fillMb(
            Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000), false);
    helper.assertValueEqual(filled, 1000, "water accepted by the canner input tank");
    te.inputSlot.put(0, new ItemStack(Ic2Items.LAPIS_DUST, 8));

    helper.succeedWhen(
        () -> {
          assertTankContains(
              helper, te.outputTank, Ic2Fluids.COOLANT.still(), 1000, "canner output tank");
          helper.assertTrue(te.inputSlot.get(0).isEmpty(), "canner lapis dust should be consumed");
          helper.assertTrue(te.inputTank.isEmpty(), "canner water should be consumed");
        });
  }

  // electrolyzer: 32 EU/t over 200 ticks, tier 2; 40 mB water -> 26 mB hydrogen (down) + 13 mB
  // oxygen (up),
  // each output pushed into an adjacent fluid tank.
  @GameTest(template = EMPTY, timeoutTicks = 300)
  public static void electrolyzerSplitsWaterIntoHydrogenAndOxygen(GameTestHelper helper) {
    BlockPos hydrogenPos = MACHINE_POS.below();
    BlockPos oxygenPos = MACHINE_POS.above();
    helper.setBlock(MACHINE_POS, Ic2Blocks.ELECTROLYZER);
    helper.setBlock(hydrogenPos, Ic2Blocks.TANK);
    helper.setBlock(oxygenPos, Ic2Blocks.TANK);
    TileEntityElectrolyzer te = getMachine(helper, TileEntityElectrolyzer.class);
    te.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.ADVANCED_RE_BATTERY, Double.POSITIVE_INFINITY));
    int filled =
        te.getInput()
            .fillMb(
                Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000), false);
    helper.assertValueEqual(filled, 1000, "water accepted by the electrolyzer tank");

    Ic2FluidTank hydrogenTank = getTankBlockTank(helper, hydrogenPos);
    Ic2FluidTank oxygenTank = getTankBlockTank(helper, oxygenPos);
    helper.succeedWhen(
        () -> {
          assertTankContains(
              helper, hydrogenTank, Ic2Fluids.HYDROGEN.still(), 26, "tank below the electrolyzer");
          assertTankContains(
              helper, oxygenTank, Ic2Fluids.OXYGEN.still(), 13, "tank above the electrolyzer");
          helper.assertValueEqual(
              te.getInput().getFluidAmount(),
              1000 - 40,
              "electrolyzer water left after one operation");
        });
  }

  // fermenter: consumes 4000 hU from an adjacent heat source to turn 20 mB biomass into 400 mB
  // biogas.
  // the electric heat generator emits 10 hU/t per coil, so 10 coils saturate the fermenter's 100
  // hU/t draw.
  @GameTest(template = EMPTY, timeoutTicks = 400)
  public static void fermenterFermentsBiomassIntoBiogas(GameTestHelper helper) {
    BlockPos heaterPos = MACHINE_POS.east();
    helper.setBlock(
        MACHINE_POS,
        Ic2Blocks.FERMENTER
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.EAST));
    helper.setBlock(
        heaterPos,
        Ic2Blocks.ELECTRIC_HEAT_GENERATOR
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.WEST));
    TileEntityFermenter te = getMachine(helper, TileEntityFermenter.class);
    BlockEntity heaterBe = helper.getBlockEntity(heaterPos);
    TileEntityElectricHeatGenerator heater = (TileEntityElectricHeatGenerator) heaterBe;
    heater.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    for (int i = 0; i < heater.coilSlot.size(); i++) {
      heater.coilSlot.put(i, new ItemStack(Ic2Items.COIL));
    }

    int filled =
        te.getInputTank().fillMb(Ic2FluidStack.create(Ic2Fluids.BIOMASS.still(), 20), false);
    helper.assertValueEqual(filled, 20, "biomass accepted by the fermenter input tank");

    helper.succeedWhen(
        () -> {
          assertTankContains(
              helper, te.getOutputTank(), Ic2Fluids.BIOGAS.still(), 400, "fermenter output tank");
          helper.assertTrue(te.getInputTank().isEmpty(), "fermenter biomass should be consumed");
        });
  }

  // fermenter balance across runs: every run turns exactly 20 mB biomass into 400 mB biogas,
  // so 40 mB biomass yields exactly 800 mB biogas over 2 runs.
  @GameTest(template = EMPTY, timeoutTicks = 900)
  public static void fermenterBiomassBiogasBalanceOverMultipleRuns(GameTestHelper helper) {
    BlockPos heaterPos = MACHINE_POS.east();
    helper.setBlock(
        MACHINE_POS,
        Ic2Blocks.FERMENTER
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.EAST));
    helper.setBlock(
        heaterPos,
        Ic2Blocks.ELECTRIC_HEAT_GENERATOR
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.WEST));
    TileEntityFermenter te = getMachine(helper, TileEntityFermenter.class);
    TileEntityElectricHeatGenerator heater =
        (TileEntityElectricHeatGenerator) helper.getBlockEntity(heaterPos);
    heater.dischargeSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
    for (int i = 0; i < heater.coilSlot.size(); i++) {
      heater.coilSlot.put(i, new ItemStack(Ic2Items.COIL));
    }

    int filled =
        te.getInputTank().fillMb(Ic2FluidStack.create(Ic2Fluids.BIOMASS.still(), 40), false);
    helper.assertValueEqual(filled, 40, "biomass accepted by the fermenter input tank");

    helper.succeedWhen(
        () -> {
          assertTankContains(
              helper, te.getOutputTank(), Ic2Fluids.BIOGAS.still(), 800, "fermenter output tank");
          helper.assertValueEqual(
              te.getOutputTank().getFluidAmount(), 800, "biogas from 40 mB biomass");
          helper.assertTrue(
              te.getInputTank().isEmpty(), "fermenter biomass should be fully consumed");
        });
  }

  private static void assertTankContains(
      GameTestHelper helper, Ic2FluidTank tank, Fluid fluid, int minAmountMb, String what) {
    Ic2FluidStack stack = tank.getFluidStack();
    helper.assertTrue(
        stack != null
            && !stack.isEmpty()
            && stack.getFluid() == fluid
            && stack.getAmountMb() >= minAmountMb,
        what
            + " should contain at least "
            + minAmountMb
            + " mB of "
            + fluid
            + ", has "
            + (stack == null ? "nothing" : stack.getAmountMb() + " mB of " + stack.getFluid()));
  }

  private static Ic2FluidTank getTankBlockTank(GameTestHelper helper, BlockPos pos) {
    BlockEntity be = helper.getBlockEntity(pos);
    if (!(be instanceof TileEntityTank tank)) {
      throw new IllegalStateException("expected a fluid tank at " + pos + ", found " + be);
    }

    return tank.getComponent(Fluids.class).getAllTanks().iterator().next();
  }

  private static boolean hasOutput(
      ic2.core.block.invslot.InvSlotOutput slot, net.minecraft.world.item.Item item) {
    for (int i = 0; i < slot.size(); i++) {
      ItemStack stack = slot.get(i);
      if (!stack.isEmpty() && stack.getItem() == item) {
        return true;
      }
    }

    return false;
  }

  private static String describe(ic2.core.block.invslot.InvSlotOutput slot) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < slot.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }

      sb.append(slot.get(i));
    }

    return sb.append(']').toString();
  }

  private static void setBlastFurnaceProgress(TileEntityBlastFurnace te, int value) {
    try {
      Field field = TileEntityBlastFurnace.class.getDeclaredField("progress");
      field.setAccessible(true);
      field.setInt(te, value);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("could not set blast furnace progress", e);
    }
  }

  private static <T extends BlockEntity> T getMachine(GameTestHelper helper, Class<T> type) {
    BlockEntity be = helper.getBlockEntity(MACHINE_POS);
    if (!type.isInstance(be)) {
      throw new IllegalStateException(
          "expected " + type.getSimpleName() + " at " + MACHINE_POS + ", found " + be);
    }

    return type.cast(be);
  }
}
