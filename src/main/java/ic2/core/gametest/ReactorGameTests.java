package ic2.core.gametest;

import ic2.core.block.comp.Energy;
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.reactor.tileentity.TileEntityRCI_LZH;
import ic2.core.block.reactor.tileentity.TileEntityRCI_RSH;
import ic2.core.block.reactor.tileentity.TileEntityReactorAccessHatch;
import ic2.core.block.reactor.tileentity.TileEntityReactorFluidPort;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.wiring.tileentity.TileEntityElectricCESU;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFSU;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.item.reactor.AbstractDamageableReactorComponent;
import ic2.core.item.reactor.ItemReactorCondensator;
import ic2.core.item.reactor.ItemReactorHeatStorage;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ReactorGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final String EMPTY_LARGE = "gametest/empty7x7x7";
  private static final BlockPos REACTOR_POS = new BlockPos(1, 1, 1);
  // center of the 7x7x7 template, so the 5x5x5 pressure vessel fits with one block of margin
  private static final BlockPos VESSEL_CENTER = new BlockPos(3, 3, 3);

  // the reactor only runs fuel while it has a redstone signal
  @GameTest(template = EMPTY)
  public static void reactorIdlesWithoutRedstoneSignal(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));

    // 60 ticks covers at least two reactor work ticks (one every 20)
    helper.runAtTickTime(
        60,
        () -> {
          Ic2GameTestAssertions.assertNear(
              helper, reactor.getReactorEnergyOutput(), 0.0, "output without redstone");
          helper.assertValueEqual(reactor.getHeat(), 0, "hull heat without redstone");
          helper.succeed();
        });
  }

  // an isolated single uranium rod pulses itself once: 1 output unit = 5 EU/t, 4 hull heat per
  // second
  @GameTest(template = EMPTY)
  public static void singleUraniumRodOutputsOnePulse(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));

    helper.succeedWhen(
        () -> {
          Ic2GameTestAssertions.assertNear(
              helper, reactor.getReactorEnergyOutput(), 1.0, "single rod output");
          Ic2GameTestAssertions.assertNear(
              helper, reactor.getOfferedEnergy(), 5.0, "single rod EU/t");
          helper.assertTrue(
              reactor.getHeat() >= 4 && reactor.getHeat() % 4 == 0,
              "uncooled single rod should add 4 hull heat per work tick, hull has "
                  + reactor.getHeat());
        });
  }

  // two adjacent rods pulse each other: 2 output units per rod instead of 1
  @GameTest(template = EMPTY)
  public static void adjacentRodsPulseEachOther(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
    reactor.reactorSlot.put(1, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));

    helper.succeedWhen(
        () ->
            Ic2GameTestAssertions.assertNear(
                helper, reactor.getReactorEnergyOutput(), 4.0, "output of two adjacent rods"));
  }

  // a neutron reflector bounces the rod's pulse back, doubling its output without extra fuel
  @GameTest(template = EMPTY)
  public static void neutronReflectorDoublesRodOutput(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
    reactor.reactorSlot.put(1, 0, new ItemStack(Ic2Items.NEUTRON_REFLECTOR));

    helper.succeedWhen(
        () ->
            Ic2GameTestAssertions.assertNear(
                helper, reactor.getReactorEnergyOutput(), 2.0, "reflected rod output"));
  }

  // an adjacent coolant cell soaks up the rod's 4 heat per work tick, keeping the hull at 0
  @GameTest(template = EMPTY)
  public static void coolantCellAbsorbsRodHeat(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
    reactor.reactorSlot.put(1, 0, new ItemStack(Ic2Items.REACTOR_COOLANT_CELL));

    ItemReactorHeatStorage cellItem = (ItemReactorHeatStorage) Ic2Items.REACTOR_COOLANT_CELL;

    helper.succeedWhen(
        () -> {
          Ic2GameTestAssertions.assertNear(
              helper, reactor.getReactorEnergyOutput(), 1.0, "rod output next to coolant cell");
          int cellHeat = cellItem.getCurrentHeat(reactor.getItemAt(1, 0), reactor, 1, 0);
          helper.assertTrue(
              cellHeat >= 4 && cellHeat % 4 == 0,
              "coolant cell should hold the rod's heat, holds " + cellHeat);
          helper.assertValueEqual(reactor.getHeat(), 0, "hull heat with coolant cell");
        });
  }

  // a reactor heat vent pulls 5 heat per work tick out of the hull and vents it away
  @GameTest(template = EMPTY)
  public static void reactorHeatVentDrainsHullHeat(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.REACTOR_HEAT_VENT));
    reactor.setHeat(10);

    ItemReactorHeatStorage ventItem = (ItemReactorHeatStorage) Ic2Items.REACTOR_HEAT_VENT;

    helper.succeedWhen(
        () -> {
          helper.assertValueEqual(reactor.getHeat(), 0, "hull heat after venting");
          helper.assertValueEqual(
              ventItem.getCurrentHeat(reactor.getItemAt(0, 0), reactor, 0, 0),
              0,
              "vent's own stored heat");
        });
  }

  // each reactor plating adds 1000 heat capacity and dampens heat effects by 5%
  @GameTest(template = EMPTY)
  public static void platingRaisesHeatCapacity(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.REACTOR_PLATING));

    helper.succeedWhen(
        () -> {
          helper.assertValueEqual(
              reactor.getMaxHeat(), 11000, "hull heat capacity with one plating");
          Ic2GameTestAssertions.assertNear(
              helper,
              reactor.getHeatEffectModifier(),
              0.95,
              "heat effect modifier with one plating");
        });
  }

  // a rod on its last work tick turns into a depleted rod instead of vanishing
  @GameTest(template = EMPTY)
  public static void uraniumRodDepletesIntoDepletedRod(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);

    ItemStack rod = new ItemStack(Ic2Items.URANIUM_FUEL_ROD);
    // uranium rods last 20000 work ticks, fast-forward to the very last one
    ((AbstractDamageableReactorComponent) Ic2Items.URANIUM_FUEL_ROD).setUse(rod, 19999);
    reactor.reactorSlot.put(0, 0, rod);

    helper.succeedWhen(
        () -> {
          ItemStack stack = reactor.getItemAt(0, 0);
          helper.assertTrue(
              stack != null && stack.getItem() == Ic2Items.DEPLETED_URANIUM_FUEL_ROD,
              "spent rod should become a depleted uranium fuel rod, slot has " + stack);
        });
  }

  // items that aren't reactor components get thrown out of the grid
  @GameTest(template = EMPTY)
  public static void nonComponentItemsAreEjected(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    reactor.reactorSlot.put(0, 0, new ItemStack(Items.STICK));

    helper.succeedWhen(
        () -> {
          ItemStack stack = reactor.getItemAt(0, 0);
          helper.assertTrue(
              stack == null || stack.isEmpty(),
              "stick should have been ejected from the reactor, slot has " + stack);
          helper.assertItemEntityPresent(Items.STICK, REACTOR_POS, 2.0);
        });
  }

  // each attached reactor chamber adds a grid column and joins the reactor's energy net delegate
  @GameTest(template = EMPTY)
  public static void chambersExpandReactorGrid(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.west(), Ic2Blocks.REACTOR_CHAMBER);
    helper.setBlock(REACTOR_POS.east(), Ic2Blocks.REACTOR_CHAMBER);

    helper.succeedWhen(
        () -> {
          helper.assertValueEqual(reactor.getReactorSize(), 5, "grid columns with two chambers");
          helper.assertValueEqual(
              reactor.reactorSlot.size(), 30, "usable grid slots with two chambers");
          helper.assertValueEqual(
              reactor.getSubTiles().size(), 3, "energy net sub-tiles with two chambers");
        });
  }

  // the running reactor is an energy net source: an adjacent MFSU banks its 5 EU/t
  @GameTest(template = EMPTY)
  public static void reactorPowersAdjacentStorage(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
    helper.setBlock(REACTOR_POS.below(), Ic2Blocks.MFSU);
    TileEntityElectricMFSU mfsu = getTe(helper, REACTOR_POS.below(), TileEntityElectricMFSU.class);

    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));

    helper.succeedWhen(
        () ->
            helper.assertTrue(
                mfsu.energy.getEnergy() >= 5.0,
                "MFSU should have banked reactor EU, has " + mfsu.energy.getEnergy()));
  }

  // a MOX rod scales its output with hull heat: at 50% heat each pulse is worth 4 * 0.5 + 1 = 3
  // output units
  @GameTest(template = EMPTY)
  public static void moxRodOutputScalesWithHullHeat(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.MOX_FUEL_ROD));
    reactor.setHeat(5000);

    helper.succeedWhen(
        () -> {
          // the work tick adds the rod's own heat before computing output, so compare against the
          // live hull heat
          float expected = 4.0F * ((float) reactor.getHeat() / reactor.getMaxHeat()) + 1.0F;
          helper.assertTrue(
              reactor.getReactorEnergyOutput() > 3.0F,
              "hot MOX rod should out-produce a cold rod, output is "
                  + reactor.getReactorEnergyOutput());
          Ic2GameTestAssertions.assertNear(
              helper,
              reactor.getReactorEnergyOutput(),
              expected,
              "MOX rod output at ~50% hull heat");
        });
  }

  // multi-cell rods pulse themselves: a dual rod yields 4 output units and 24 heat, a quad 12 units
  // and 96 heat
  @GameTest(template = EMPTY)
  public static void dualAndQuadRodsScaleOutputAndHeat(GameTestHelper helper) {
    TileEntityNuclearReactorElectric dual = placeReactor(helper, new BlockPos(0, 1, 1));
    TileEntityNuclearReactorElectric quad = placeReactor(helper, new BlockPos(2, 1, 1));
    helper.setBlock(new BlockPos(1, 1, 1), Blocks.REDSTONE_BLOCK);
    dual.reactorSlot.put(0, 0, new ItemStack(Ic2Items.DUAL_URANIUM_FUEL_ROD));
    quad.reactorSlot.put(0, 0, new ItemStack(Ic2Items.QUAD_URANIUM_FUEL_ROD));

    helper.succeedWhen(
        () -> {
          Ic2GameTestAssertions.assertNear(
              helper, dual.getReactorEnergyOutput(), 4.0, "dual rod output");
          Ic2GameTestAssertions.assertNear(
              helper, quad.getReactorEnergyOutput(), 12.0, "quad rod output");
          helper.assertTrue(
              dual.getHeat() >= 24 && dual.getHeat() % 24 == 0,
              "dual rod should add 24 hull heat per work tick, hull has " + dual.getHeat());
          helper.assertTrue(
              quad.getHeat() >= 96 && quad.getHeat() % 96 == 0,
              "quad rod should add 96 hull heat per work tick, hull has " + quad.getHeat());
        });
  }

  // a lithium rod next to a pulsing rod breeds on a hot hull, turning into a tritium rod once full
  @GameTest(template = EMPTY)
  public static void lithiumRodBreedsIntoTritium(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);

    ItemStack lithium = new ItemStack(Ic2Items.LITHIUM_FUEL_ROD);
    // lithium needs 10000 breeding levels; at 3000 hull heat it gains heat/3000 = 1 per pulse, so
    // start one short
    ((AbstractDamageableReactorComponent) Ic2Items.LITHIUM_FUEL_ROD).setUse(lithium, 9999);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
    reactor.reactorSlot.put(1, 0, lithium);
    reactor.setHeat(3000);

    helper.succeedWhen(
        () -> {
          ItemStack stack = reactor.getItemAt(1, 0);
          helper.assertTrue(
              stack != null && stack.getItem() == Ic2Items.TRITIUM_FUEL_ROD,
              "fully bred lithium rod should become a tritium fuel rod, slot has " + stack);
        });
  }

  // the iridium reflector doubles rod output like a regular reflector but takes no wear
  @GameTest(template = EMPTY)
  public static void iridiumReflectorBouncesWithoutWear(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
    reactor.reactorSlot.put(1, 0, new ItemStack(Ic2Items.IRIDIUM_NEUTRON_REFLECTOR));

    helper.succeedWhen(
        () -> {
          Ic2GameTestAssertions.assertNear(
              helper, reactor.getReactorEnergyOutput(), 2.0, "rod output with iridium reflector");
          ItemStack stack = reactor.getItemAt(1, 0);
          helper.assertTrue(
              stack != null && stack.getItem() == Ic2Items.IRIDIUM_NEUTRON_REFLECTOR,
              "iridium reflector should survive reflecting, slot has " + stack);
        });
  }

  // a neutron reflector on its last pulse is destroyed, dropping the rod back to single output
  @GameTest(template = EMPTY)
  public static void wornNeutronReflectorBreaks(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);

    ItemStack reflector = new ItemStack(Ic2Items.NEUTRON_REFLECTOR);
    // neutron reflectors absorb 30000 pulses, fast-forward to the very last one
    ((AbstractDamageableReactorComponent) Ic2Items.NEUTRON_REFLECTOR).setUse(reflector, 29999);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
    reactor.reactorSlot.put(1, 0, reflector);

    helper.succeedWhen(
        () -> {
          ItemStack stack = reactor.getItemAt(1, 0);
          helper.assertTrue(
              stack == null || stack.isEmpty(),
              "worn-out reflector should be destroyed, slot has " + stack);
          Ic2GameTestAssertions.assertNear(
              helper,
              reactor.getReactorEnergyOutput(),
              1.0,
              "rod output after losing its reflector");
        });
  }

  // a coolant cell pushed past its 10000 heat capacity shatters instead of storing the overflow
  @GameTest(template = EMPTY)
  public static void overfilledCoolantCellShatters(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);

    ItemStack cell = new ItemStack(Ic2Items.REACTOR_COOLANT_CELL);
    // the rod's next 4 heat push the cell past its 10000 capacity
    ((AbstractDamageableReactorComponent) Ic2Items.REACTOR_COOLANT_CELL).setUse(cell, 9998);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
    reactor.reactorSlot.put(1, 0, cell);

    helper.succeedWhen(
        () -> {
          ItemStack stack = reactor.getItemAt(1, 0);
          helper.assertTrue(
              stack == null || stack.isEmpty(),
              "overfilled coolant cell should shatter, slot has " + stack);
        });
  }

  // condensators soak up rod heat like coolant cells, keeping the hull cold
  @GameTest(template = EMPTY)
  public static void condensatorAbsorbsRodHeat(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
    reactor.reactorSlot.put(1, 0, new ItemStack(Ic2Items.LZH_CONDENSATOR));

    ItemReactorCondensator condensator = (ItemReactorCondensator) Ic2Items.LZH_CONDENSATOR;

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              condensator.getUseFraction(reactor.getItemAt(1, 0)) > 0.0,
              "condensator should hold the rod's heat");
          helper.assertValueEqual(reactor.getHeat(), 0, "hull heat with condensator");
        });
  }

  // the RSH/LZH coolant interfaces recharge nearly-full condensators in the reactor for a resource
  // block and 1000 EU
  @GameTest(template = EMPTY)
  public static void rciRechargesCondensators(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    // each RCI services the reactor behind its back face
    helper.setBlock(
        new BlockPos(2, 1, 1),
        Ic2Blocks.RCI_RSH
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.EAST));
    helper.setBlock(
        new BlockPos(0, 1, 1),
        Ic2Blocks.RCI_LZH
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.WEST));
    TileEntityRCI_RSH rsh = getTe(helper, new BlockPos(2, 1, 1), TileEntityRCI_RSH.class);
    TileEntityRCI_LZH lzh = getTe(helper, new BlockPos(0, 1, 1), TileEntityRCI_LZH.class);

    // both condensators start at 90% heat, above the 85% recharge threshold
    ItemStack rshCondensator = new ItemStack(Ic2Items.RSH_CONDENSATOR);
    ((ItemReactorCondensator) Ic2Items.RSH_CONDENSATOR).setUse(rshCondensator, 18000);
    ItemStack lzhCondensator = new ItemStack(Ic2Items.LZH_CONDENSATOR);
    ((ItemReactorCondensator) Ic2Items.LZH_CONDENSATOR).setUse(lzhCondensator, 90000);
    reactor.reactorSlot.put(0, 0, rshCondensator);
    reactor.reactorSlot.put(1, 0, lzhCondensator);

    rsh.getComponent(Energy.class).addEnergy(2000.0);
    lzh.getComponent(Energy.class).addEnergy(2000.0);
    rsh.inputSlot.put(0, new ItemStack(Blocks.REDSTONE_BLOCK));
    lzh.inputSlot.put(0, new ItemStack(Blocks.LAPIS_BLOCK));

    helper.succeedWhen(
        () -> {
          Ic2GameTestAssertions.assertNear(
              helper,
              ((ItemReactorCondensator) Ic2Items.RSH_CONDENSATOR)
                  .getUseFraction(reactor.getItemAt(0, 0)),
              0.0,
              "RSH condensator heat after recharge");
          Ic2GameTestAssertions.assertNear(
              helper,
              ((ItemReactorCondensator) Ic2Items.LZH_CONDENSATOR)
                  .getUseFraction(reactor.getItemAt(1, 0)),
              0.0,
              "LZH condensator heat after recharge");
          helper.assertTrue(
              rsh.inputSlot.isEmpty(), "RSH interface should consume its redstone block");
          helper.assertTrue(
              lzh.inputSlot.isEmpty(), "LZH interface should consume its lapis block");
        });
  }

  // a reactor heat exchanger pulls up to 72 heat per work tick from the hull into itself
  @GameTest(template = EMPTY)
  public static void reactorHeatExchangerDrainsHull(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.REACTOR_HEAT_EXCHANGER));
    reactor.setHeat(2000);

    ItemReactorHeatStorage exchanger = (ItemReactorHeatStorage) Ic2Items.REACTOR_HEAT_EXCHANGER;

    helper.succeedWhen(
        () -> {
          int held = exchanger.getCurrentHeat(reactor.getItemAt(0, 0), reactor, 0, 0);
          helper.assertTrue(
              held >= 72 && held % 72 == 0,
              "exchanger should pull 72 hull heat per work tick, holds " + held);
          helper.assertValueEqual(
              reactor.getHeat(), 2000 - held, "hull heat after exchanger transfer");
        });
  }

  // a component heat exchanger levels heat between itself and its neighbors, up to 36 per work tick
  @GameTest(template = EMPTY)
  public static void componentHeatExchangerBalancesNeighbors(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    ItemStack cell = new ItemStack(Ic2Items.REACTOR_COOLANT_CELL);
    ((AbstractDamageableReactorComponent) Ic2Items.REACTOR_COOLANT_CELL).setUse(cell, 5000);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.COMPONENT_HEAT_EXCHANGER));
    reactor.reactorSlot.put(1, 0, cell);

    ItemReactorHeatStorage exchanger = (ItemReactorHeatStorage) Ic2Items.COMPONENT_HEAT_EXCHANGER;
    ItemReactorHeatStorage cellItem = (ItemReactorHeatStorage) Ic2Items.REACTOR_COOLANT_CELL;

    helper.succeedWhen(
        () -> {
          int held = exchanger.getCurrentHeat(reactor.getItemAt(0, 0), reactor, 0, 0);
          int cellHeat = cellItem.getCurrentHeat(reactor.getItemAt(1, 0), reactor, 1, 0);
          helper.assertTrue(held > 0, "exchanger should take heat from the hot coolant cell");
          helper.assertValueEqual(held + cellHeat, 5000, "total heat during balancing");
          helper.assertValueEqual(reactor.getHeat(), 0, "hull heat during component balancing");
        });
  }

  // a component heat vent strips 4 heat per work tick from each neighboring component
  @GameTest(template = EMPTY)
  public static void componentHeatVentCoolsNeighbors(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    ItemReactorHeatStorage cellItem = (ItemReactorHeatStorage) Ic2Items.REACTOR_COOLANT_CELL;

    ItemStack west = new ItemStack(Ic2Items.REACTOR_COOLANT_CELL);
    ItemStack east = new ItemStack(Ic2Items.REACTOR_COOLANT_CELL);
    ((AbstractDamageableReactorComponent) Ic2Items.REACTOR_COOLANT_CELL).setUse(west, 100);
    ((AbstractDamageableReactorComponent) Ic2Items.REACTOR_COOLANT_CELL).setUse(east, 100);
    reactor.reactorSlot.put(0, 0, west);
    reactor.reactorSlot.put(1, 0, new ItemStack(Ic2Items.COMPONENT_HEAT_VENT));
    reactor.reactorSlot.put(2, 0, east);

    helper.succeedWhen(
        () -> {
          int westHeat = cellItem.getCurrentHeat(reactor.getItemAt(0, 0), reactor, 0, 0);
          int eastHeat = cellItem.getCurrentHeat(reactor.getItemAt(2, 0), reactor, 2, 0);
          helper.assertValueEqual(
              westHeat, eastHeat, "both neighbors should cool at the same rate");
          helper.assertTrue(
              westHeat < 100 && (100 - westHeat) % 4 == 0,
              "spread vent should cool 4 heat per work tick, cell holds " + westHeat);
        });
  }

  // an overclocked heat vent pulls 36 hull heat per work tick but only vents 20, banking the
  // difference
  @GameTest(template = EMPTY)
  public static void overclockedVentTradesStorageForHullCooling(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.OVERCLOCKED_HEAT_VENT));
    reactor.setHeat(720);

    ItemReactorHeatStorage vent = (ItemReactorHeatStorage) Ic2Items.OVERCLOCKED_HEAT_VENT;

    helper.succeedWhen(
        () -> {
          int drained = 720 - reactor.getHeat();
          int held = vent.getCurrentHeat(reactor.getItemAt(0, 0), reactor, 0, 0);
          helper.assertTrue(
              drained >= 36 && drained % 36 == 0,
              "vent should drain 36 hull heat per work tick, drained " + drained);
          helper.assertValueEqual(
              held, drained / 36 * 16, "vent should bank 16 of every 36 drained heat");
        });
  }

  // a full 5x5x5 pressure vessel flips the reactor to fluid mode: off the energy net, boiling
  // coolant into hot coolant
  @GameTest(template = EMPTY_LARGE, timeoutTicks = 300)
  public static void pressureVesselBoilsCoolantIntoHotCoolant(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = buildFluidReactor(helper);
    helper.setBlock(VESSEL_CENTER.offset(-2, 0, 0), Ic2Blocks.REACTOR_REDSTONE_PORT);
    helper.setBlock(VESSEL_CENTER.offset(-3, 0, 0), Blocks.REDSTONE_BLOCK);

    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
    reactor.reactorSlot.put(1, 0, new ItemStack(Ic2Items.REACTOR_HEAT_VENT));
    int filled =
        reactor.inputTank.fillMb(Ic2FluidStack.create(Ic2Fluids.COOLANT.still(), 1000), false);
    helper.assertValueEqual(filled, 1000, "coolant accepted by the reactor input tank");

    helper.succeedWhen(
        () -> {
          helper.assertTrue(reactor.isFluidCooled(), "reactor should detect the pressure vessel");
          helper.assertTrue(
              !reactor.addedToEnergyNet, "fluid-cooled reactor should leave the energy net");
          Ic2GameTestAssertions.assertNear(
              helper,
              reactor.getReactorEnergyOutput(),
              1.0,
              "rod output via the redstone port signal");
          helper.assertTrue(
              reactor.outputTank.hasExactFluid(Ic2Fluids.HOT_COOLANT.still())
                  && reactor.outputTank.getFluidAmount() > 0,
              "output tank should hold hot coolant, has "
                  + reactor.outputTank.getFluidAmount()
                  + " mB");
          helper.assertTrue(
              reactor.inputTank.getFluidAmount() < 1000, "input tank coolant should be consumed");
        });
  }

  // the redstone port relays an external signal through the vessel wall to the sealed reactor
  @GameTest(template = EMPTY_LARGE, timeoutTicks = 300)
  public static void redstonePortControlsSealedReactor(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = buildFluidReactor(helper);
    helper.setBlock(VESSEL_CENTER.offset(-2, 0, 0), Ic2Blocks.REACTOR_REDSTONE_PORT);
    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));

    helper.runAtTickTime(
        60,
        () -> {
          helper.assertTrue(reactor.isFluidCooled(), "reactor should detect the pressure vessel");
          Ic2GameTestAssertions.assertNear(
              helper,
              reactor.getReactorEnergyOutput(),
              0.0,
              "output with an unpowered redstone port");
          helper.setBlock(VESSEL_CENTER.offset(-3, 0, 0), Blocks.REDSTONE_BLOCK);
        });

    helper.succeedWhen(
        () ->
            Ic2GameTestAssertions.assertNear(
                helper,
                reactor.getReactorEnergyOutput(),
                1.0,
                "output once the redstone port is powered"));
  }

  // the access hatch exposes the sealed reactor's grid as a container through the vessel wall
  @GameTest(template = EMPTY_LARGE, timeoutTicks = 200)
  public static void accessHatchExposesSealedReactorGrid(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = buildFluidReactor(helper);
    helper.setBlock(VESSEL_CENTER.offset(0, 2, 0), Ic2Blocks.REACTOR_ACCESS_HATCH);
    TileEntityReactorAccessHatch hatch =
        getTe(helper, VESSEL_CENTER.offset(0, 2, 0), TileEntityReactorAccessHatch.class);

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              hatch.getReactorInstance() == reactor, "hatch should find the sealed reactor");
          helper.assertValueEqual(
              hatch.getContainerSize(), reactor.getContainerSize(), "hatch container size");
          hatch.setItem(0, new ItemStack(Ic2Items.REACTOR_COOLANT_CELL));
          ItemStack stack = reactor.getItemAt(0, 0);
          helper.assertTrue(
              stack != null && stack.getItem() == Ic2Items.REACTOR_COOLANT_CELL,
              "cell inserted through the hatch should land in grid slot (0,0), slot has " + stack);
        });
  }

  // full hot-coolant loop: fluid port ejects into a liquid heat exchanger whose heat drives a
  // stirling generator
  @GameTest(template = EMPTY_LARGE, timeoutTicks = 600)
  public static void hotCoolantLoopDrivesLiquidHeatExchanger(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = buildFluidReactor(helper);
    helper.setBlock(VESSEL_CENTER.offset(-2, 0, 0), Ic2Blocks.REACTOR_REDSTONE_PORT);
    helper.setBlock(VESSEL_CENTER.offset(-3, 0, 0), Blocks.REDSTONE_BLOCK);
    helper.setBlock(VESSEL_CENTER.offset(2, 0, 0), Ic2Blocks.REACTOR_FLUID_PORT);
    helper.setBlock(
        VESSEL_CENTER.offset(3, 0, 0),
        Ic2Blocks.LIQUID_HEAT_EXCHANGER
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.NORTH));
    helper.setBlock(
        VESSEL_CENTER.offset(3, 0, -1),
        Ic2Blocks.STIRLING_GENERATOR
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.SOUTH));
    helper.setBlock(VESSEL_CENTER.offset(3, 0, -2), Ic2Blocks.CESU);

    TileEntityReactorFluidPort port =
        getTe(helper, VESSEL_CENTER.offset(2, 0, 0), TileEntityReactorFluidPort.class);
    TileEntityLiquidHeatExchanger exchanger =
        getTe(helper, VESSEL_CENTER.offset(3, 0, 0), TileEntityLiquidHeatExchanger.class);
    TileEntityElectricCESU cesu =
        getTe(helper, VESSEL_CENTER.offset(3, 0, -2), TileEntityElectricCESU.class);

    port.upgradeSlot.put(0, new ItemStack(Ic2Items.FLUID_EJECTOR_UPGRADE));
    for (int i = 0; i < exchanger.heatexchangerslots.size(); i++) {
      exchanger.heatexchangerslots.put(i, new ItemStack(Ic2Items.HEAT_CONDUCTOR));
    }

    reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
    reactor.reactorSlot.put(1, 0, new ItemStack(Ic2Items.REACTOR_HEAT_VENT));
    reactor.inputTank.fillMb(Ic2FluidStack.create(Ic2Fluids.COOLANT.still(), 1000), false);

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              port.getReactorInstance() == reactor, "fluid port should find the sealed reactor");
          helper.assertTrue(
              exchanger.getOutputTank().hasExactFluid(Ic2Fluids.COOLANT.still())
                  && exchanger.getOutputTank().getFluidAmount() > 0,
              "heat exchanger should cool hot coolant back into coolant, has "
                  + exchanger.getOutputTank().getFluidAmount()
                  + " mB");
          helper.assertTrue(
              cesu.energy.getEnergy() > 0.0,
              "CESU should bank stirling EU made from reactor heat, has "
                  + cesu.energy.getEnergy());
        });
  }

  // heat beyond the hull's capacity melts the reactor down; containment plating shrinks the blast
  @GameTest(template = EMPTY, batch = "ic2ReactorMeltdown")
  public static void overheatedReactorMeltsDown(GameTestHelper helper) {
    TileEntityNuclearReactorElectric reactor = placeReactor(helper);

    // 18 containment platings raise capacity to 19000 but damp the explosion to well below one TNT
    for (int y = 0; y < 6; y++) {
      for (int x = 0; x < 3; x++) {
        reactor.reactorSlot.put(x, y, new ItemStack(Ic2Items.CONTAINMENT_REACTOR_PLATING));
      }
    }

    reactor.setHeat(100000);

    helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.AIR, REACTOR_POS));
  }

  private static TileEntityNuclearReactorElectric placeReactor(GameTestHelper helper) {
    return placeReactor(helper, REACTOR_POS);
  }

  private static TileEntityNuclearReactorElectric placeReactor(
      GameTestHelper helper, BlockPos pos) {
    helper.setBlock(pos, Ic2Blocks.NUCLEAR_REACTOR);
    return getTe(helper, pos, TileEntityNuclearReactorElectric.class);
  }

  // hollow 5x5x5 reactor vessel shell around a fully chambered reactor; tests punch ports into the
  // shell afterwards
  private static TileEntityNuclearReactorElectric buildFluidReactor(GameTestHelper helper) {
    for (int x = -2; x <= 2; x++) {
      for (int y = -2; y <= 2; y++) {
        for (int z = -2; z <= 2; z++) {
          if (Math.abs(x) == 2 || Math.abs(y) == 2 || Math.abs(z) == 2) {
            helper.setBlock(VESSEL_CENTER.offset(x, y, z), Ic2Blocks.REACTOR_VESSEL);
          }
        }
      }
    }

    TileEntityNuclearReactorElectric reactor = placeReactor(helper, VESSEL_CENTER);

    for (Direction dir : Direction.values()) {
      helper.setBlock(VESSEL_CENTER.relative(dir), Ic2Blocks.REACTOR_CHAMBER);
    }

    return reactor;
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
