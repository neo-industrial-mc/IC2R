package ic2.core.gametest;

import ic2.core.block.heatgenerator.tileentity.TileEntitySolidHeatGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import ic2.core.block.machine.tileentity.TileEntitySteamGenerator;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.item.tool.ItemToolWrench;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Calcification mechanics of the steam generator: boiling regular water deposits scale at 1 point
 * per mB while distilled water is scale-free, water merely preheated and passed through below the
 * boiling point never calcifies, and at 100,000 points the boiler shuts down permanently.
 */
@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class SteamBoilerCalcificationGameTests {
  private static final String EMPTY_LARGE = "gametest/empty7x7x7";

  private static final BlockPos BOILER_POS = new BlockPos(2, 1, 3);
  private static final BlockPos TURBINE_POS = new BlockPos(3, 1, 3);
  private static final int WATER_CHARGE = 1000;
  private static final int MAX_CALCIFICATION = 100000;
  private static final String STEAM_OUTPUT = "ic2.SteamGenerator.output.steam";
  private static final String WATER_OUTPUT = "ic2.SteamGenerator.output.water";

  // with the input valve at 1 mB/t and the system preheated well past 100C, the boiler converts
  // exactly 1 mB of water into 100 mB of steam per tick, so the scale ticks up in lockstep with
  // the water level ticking down
  @GameTest(template = EMPTY_LARGE, timeoutTicks = 200)
  public static void boilingRegularWaterCalcifiesMbForMb(GameTestHelper helper) {
    TileEntitySteamGenerator boiler = setupBoiler(helper, 1, 150.0F, 0);
    placeHeatGenerator(helper, BOILER_POS.west(), Direction.EAST);
    placeSteamSink(helper);
    fillBoiler(helper, boiler, Fluids.WATER);

    boolean[] sawSteam = {false};
    helper.succeedWhen(
        () -> {
          sawSteam[0] |= STEAM_OUTPUT.equals(boiler.getOutputFluidName());
          int calcification = rawCalcification(helper, boiler);
          helper.assertValueEqual(
              calcification,
              WATER_CHARGE - boiler.waterTank.getFluidAmount(),
              "calcification points vs regular water mB boiled away");
          helper.assertTrue(sawSteam[0], "the boiler should be producing steam");
          helper.assertTrue(
              calcification >= 40,
              "calcification should keep growing while boiling, is " + calcification);
        });
  }

  // the same rig fed with distilled water boils just as well but never deposits any scale
  @GameTest(template = EMPTY_LARGE, timeoutTicks = 200)
  public static void boilingDistilledWaterDoesNotCalcify(GameTestHelper helper) {
    TileEntitySteamGenerator boiler = setupBoiler(helper, 1, 150.0F, 0);
    placeHeatGenerator(helper, BOILER_POS.west(), Direction.EAST);
    placeSteamSink(helper);
    fillBoiler(helper, boiler, Ic2Fluids.DISTILLED_WATER.still());

    helper.succeedWhen(
        () -> {
          helper.assertValueEqual(
              rawCalcification(helper, boiler), 0, "calcification on distilled water");
          helper.assertValueEqual(
              boiler.getCalcification(), 0.0F, "calcification gauge on distilled water");
          helper.assertTrue(
              boiler.waterTank.getFluidAmount() <= WATER_CHARGE - 40,
              "the boiler should have boiled at least 40 mB of distilled water, tank holds "
                  + boiler.waterTank.getFluidAmount());
        });
  }

  // a boiler started 10 points short of the limit finishes those 10 mB, hits exactly 100,000,
  // and locks up: inactive, gauge pegged at 100%, not a drop of water consumed afterwards
  @GameTest(template = EMPTY_LARGE, timeoutTicks = 100)
  public static void boilerShutsDownAtCalcificationLimit(GameTestHelper helper) {
    TileEntitySteamGenerator boiler = setupBoiler(helper, 1, 150.0F, MAX_CALCIFICATION - 10);
    placeHeatGenerator(helper, BOILER_POS.west(), Direction.EAST);
    placeSteamSink(helper);
    fillBoiler(helper, boiler, Fluids.WATER);

    helper.succeedWhen(
        () -> {
          int calcification = rawCalcification(helper, boiler);
          int water = boiler.waterTank.getFluidAmount();
          helper.assertTrue(
              calcification <= MAX_CALCIFICATION,
              "calcification must not pass the limit, is " + calcification);
          helper.assertTrue(
              water >= WATER_CHARGE - 10,
              "a calcified boiler must stop consuming water, tank holds " + water);
          helper.assertValueEqual(calcification, MAX_CALCIFICATION, "calcification at the limit");
          helper.assertValueEqual(water, WATER_CHARGE - 10, "water consumed before the shutdown");
          helper.assertTrue(boiler.isCalcified(), "the boiler should report itself calcified");
          helper.assertFalse(boiler.getActive(), "a calcified boiler must shut down");
          helper.assertValueEqual(
              boiler.getCalcification(), 100.0F, "calcification gauge at the limit");
        });
  }

  // a fully calcified boiler is permanently dead: with heat and distilled water freely available
  // it never reactivates, never draws water, and the scale never goes away (the preset also
  // proves calcification survives an NBT save/load round trip)
  @GameTest(template = EMPTY_LARGE, timeoutTicks = 150)
  public static void calcifiedBoilerStaysInert(GameTestHelper helper) {
    TileEntitySteamGenerator boiler = setupBoiler(helper, 1000, 150.0F, MAX_CALCIFICATION);
    placeHeatGenerator(helper, BOILER_POS.west(), Direction.EAST);
    fillBoiler(helper, boiler, Ic2Fluids.DISTILLED_WATER.still());

    helper.onEachTick(
        () -> {
          helper.assertFalse(boiler.getActive(), "a calcified boiler must never turn on");
          helper.assertValueEqual(
              boiler.waterTank.getFluidAmount(), WATER_CHARGE, "water in the inert boiler");
          helper.assertValueEqual(
              rawCalcification(helper, boiler),
              MAX_CALCIFICATION,
              "calcification of the inert boiler");
        });
    helper.runAfterDelay(100, helper::succeed);
  }

  // wrenching a calcified boiler must not launder the scale away: the drop carries the
  // calcification in its NBT and placing it back down yields a boiler that is still calcified
  @GameTest(template = EMPTY_LARGE, timeoutTicks = 100)
  public static void wrenchedCalcifiedBoilerStaysCalcified(GameTestHelper helper) {
    setupBoiler(helper, 1, 150.0F, MAX_CALCIFICATION);
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    BlockPos absolutePos = helper.absolutePos(BOILER_POS);
    Direction facing =
        ((Ic2TileEntityBlock) Ic2Blocks.STEAM_GENERATOR).getFacing(helper.getLevel(), absolutePos);
    ItemToolWrench.WrenchResult result =
        ItemToolWrench.wrenchBlock(helper.getLevel(), absolutePos, facing, player, true);

    helper.assertValueEqual(result, ItemToolWrench.WrenchResult.Removed, "wrench result");
    helper.assertBlockPresent(Blocks.AIR, BOILER_POS);

    helper.runAtTickTime(
        2,
        () -> {
          List<ItemEntity> drops = getBoilerDrops(helper);
          helper.assertTrue(!drops.isEmpty(), "the wrenched boiler should drop itself");
          ItemStack dropStack = drops.get(0).getItem().copy();
          drops.get(0).discard();
          helper.assertValueEqual(
              StackUtil.getOrCreateNbtData(dropStack).getInt("calcification"),
              MAX_CALCIFICATION,
              "calcification carried by the dropped stack");

          InteractionResult placeResult = placeStack(helper, player, dropStack);
          helper.assertTrue(placeResult.consumesAction(), "placing the drop should succeed");
          TileEntitySteamGenerator restored =
              getTe(helper, BOILER_POS, TileEntitySteamGenerator.class);
          helper.assertValueEqual(
              rawCalcification(helper, restored),
              MAX_CALCIFICATION,
              "calcification of the re-placed boiler");
          helper.assertTrue(
              restored.isCalcified(), "the re-placed boiler must still report itself calcified");
          helper.succeed();
        });
  }

  // the ordinary mining path runs through the loot table instead of the wrench hook; it must
  // carry the scale onto the drop all the same
  @GameTest(template = EMPTY_LARGE, timeoutTicks = 100)
  public static void minedCalcifiedBoilerKeepsScaleOnDrop(GameTestHelper helper) {
    setupBoiler(helper, 1, 150.0F, MAX_CALCIFICATION);
    // GameTestHelper.destroyBlock suppresses drops, so break through the level instead
    helper.getLevel().destroyBlock(helper.absolutePos(BOILER_POS), true);

    helper.succeedWhen(
        () -> {
          List<ItemEntity> drops = getBoilerDrops(helper);
          helper.assertTrue(!drops.isEmpty(), "the mined boiler should drop itself");
          helper.assertValueEqual(
              StackUtil.getOrCreateNbtData(drops.get(0).getItem()).getInt("calcification"),
              MAX_CALCIFICATION,
              "calcification carried by the mined drop");
        });
  }

  private static List<ItemEntity> getBoilerDrops(GameTestHelper helper) {
    return helper
        .getLevel()
        .getEntitiesOfClass(
            ItemEntity.class,
            new AABB(helper.absolutePos(BOILER_POS)).inflate(2.0),
            entity -> entity.getItem().is(Ic2Blocks.STEAM_GENERATOR.asItem()));
  }

  private static InteractionResult placeStack(
      GameTestHelper helper, ServerPlayer player, ItemStack stack) {
    if (!(stack.getItem() instanceof BlockItem blockItem)) {
      return InteractionResult.FAIL;
    }

    BlockPos placePos = helper.absolutePos(BOILER_POS);
    player.setPos(placePos.getX() + 0.5, placePos.getY() + 3.0, placePos.getZ() + 0.5);
    BlockHitResult hit =
        new BlockHitResult(Vec3.atCenterOf(placePos), Direction.UP, placePos, false);
    return blockItem.place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit));
  }

  // below the boiling point at zero pressure the boiler is a preheating pass-through: the water
  // leaves unchanged towards the neighbor, and since nothing boils, nothing calcifies
  @GameTest(template = EMPTY_LARGE, timeoutTicks = 200)
  public static void passThroughBelowBoilingDoesNotCalcify(GameTestHelper helper) {
    TileEntitySteamGenerator boiler = setupBoiler(helper, 1000, 0.0F, 0);
    placeHeatGenerator(helper, BOILER_POS.north(), Direction.SOUTH);
    helper.setBlock(BOILER_POS.west(), Ic2Blocks.STEAM_GENERATOR);
    TileEntitySteamGenerator receiver =
        getTe(helper, BOILER_POS.west(), TileEntitySteamGenerator.class);
    fillBoiler(helper, boiler, Fluids.WATER);

    boolean[] sawWaterOutput = {false};
    helper.succeedWhen(
        () -> {
          sawWaterOutput[0] |= WATER_OUTPUT.equals(boiler.getOutputFluidName());
          int source = boiler.waterTank.getFluidAmount();
          int passed = receiver.waterTank.getFluidAmount();
          helper.assertValueEqual(
              source + passed, WATER_CHARGE, "total water across the pass-through");
          helper.assertValueEqual(
              rawCalcification(helper, boiler), 0, "calcification of the pass-through boiler");
          helper.assertTrue(sawWaterOutput[0], "the boiler should report water as its output");
          helper.assertValueEqual(passed, WATER_CHARGE, "water passed through to the neighbor");
        });
  }

  // the calcification counter is not exposed as a raw value, read it off the save data
  private static int rawCalcification(GameTestHelper helper, TileEntitySteamGenerator boiler) {
    return boiler
        .saveWithFullMetadata(helper.getLevel().registryAccess())
        .getInt("calcification");
  }

  // preset the operating point through the save data: the GUI dials by their NBT names, the
  // system heat to skip the hours-long warmup, and the scale level under test
  private static TileEntitySteamGenerator setupBoiler(
      GameTestHelper helper, int inputMb, float systemHeat, int calcification) {
    helper.setBlock(BOILER_POS, Ic2Blocks.STEAM_GENERATOR);
    TileEntitySteamGenerator boiler = getTe(helper, BOILER_POS, TileEntitySteamGenerator.class);
    CompoundTag nbt = boiler.saveWithFullMetadata(helper.getLevel().registryAccess());
    nbt.putInt("inputmb", inputMb);
    nbt.putFloat("systemheat", systemHeat);
    nbt.putInt("calcification", calcification);
    boiler.loadWithComponents(nbt, helper.getLevel().registryAccess());
    return boiler;
  }

  private static void fillBoiler(
      GameTestHelper helper, TileEntitySteamGenerator boiler, Fluid fluid) {
    int filled = boiler.waterTank.fillMb(Ic2FluidStack.create(fluid, WATER_CHARGE), false);
    helper.assertValueEqual(filled, WATER_CHARGE, "water accepted by the boiler");
  }

  private static void placeHeatGenerator(GameTestHelper helper, BlockPos pos, Direction facing) {
    helper.setBlock(
        pos,
        Ic2Blocks.SOLID_HEAT_GENERATOR
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, facing));
    getTe(helper, pos, TileEntitySolidHeatGenerator.class)
        .fuelSlot
        .put(0, new ItemStack(Items.COAL, 64));
  }

  // a turbine with a condenser on top swallows the boiler's steam so nothing is vented; without
  // ejector upgrades the condensate stays where it falls instead of flowing back into the boiler
  private static void placeSteamSink(GameTestHelper helper) {
    helper.setBlock(
        TURBINE_POS,
        Ic2Blocks.STEAM_KINETIC_GENERATOR
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.EAST));
    TileEntitySteamKineticGenerator turbine =
        getTe(helper, TURBINE_POS, TileEntitySteamKineticGenerator.class);
    turbine.turbineSlot.put(0, new ItemStack(Ic2Items.STEAM_TURBINE));
    helper.setBlock(TURBINE_POS.above(), Ic2Blocks.CONDENSER);
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
