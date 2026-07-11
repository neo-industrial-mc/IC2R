package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.api.item.IKineticRotor;
import ic2.core.IC2;
import ic2.core.WindSim;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class WindGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final double WIND_METER_MAX_CHARGE = 10000.0;
  private static final double WIND_METER_USE_COST = 50.0;

  // the five rotor tiers: diameter, efficiency, usable wind band, durability and which gearboxes
  // they fit
  @GameTest(template = EMPTY)
  public static void windRotorTierStats(GameTestHelper helper) {
    assertRotor(helper, Ic2Items.WOODEN_ROTOR, 5, 0.25F, 10, 60, 10800, false);
    assertRotor(helper, Ic2Items.BRONZE_ROTOR, 7, 0.5F, 14, 75, 86400, true);
    assertRotor(helper, Ic2Items.IRON_ROTOR, 7, 0.5F, 14, 75, 86400, true);
    assertRotor(helper, Ic2Items.STEEL_ROTOR, 9, 0.75F, 17, 90, 172800, true);
    assertRotor(helper, Ic2Items.CARBON_ROTOR, 11, 1.0F, 20, 110, 604800, true);
    helper.succeed();
  }

  // wind sim height profile: no wind at height 0, a peak of base strength (5..24) x 2.4 halfway
  // between
  // sea level and the world top, falling off on both sides and clamped to zero above 1.125x the
  // world height
  @GameTest(template = EMPTY)
  public static void windSimScalesWithAltitude(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    WindSim sim = new WindSim(level);
    double weather = weatherMultiplier(level);
    double peakY = windPeakY(level);
    double topY = IC2.getWorldMaxHeight(level) * 1.125;
    double peak = sim.getWindAt(peakY) / weather;

    helper.assertValueEqual(sim.getWindAt(0.0), 0.0, "wind at height 0");
    helper.assertTrue(
        peak >= 5 * 2.4 - 0.001 && peak <= 24 * 2.4 + 0.001,
        "peak wind should come from a base strength of 5..24, has " + peak);
    helper.assertTrue(
        peak >= sim.getWindAt(peakY / 2.0) / weather,
        "wind below the peak altitude should be weaker");
    helper.assertTrue(
        peak >= sim.getWindAt((peakY + topY) / 2.0) / weather,
        "wind above the peak altitude should be weaker");
    helper.assertValueEqual(
        sim.getWindAt(IC2.getWorldMaxHeight(level) * 1.3), 0.0, "wind far above the world height");
    helper.assertValueEqual(sim.getMaxWind(), 108.0, "max wind constant");
    helper.succeed();
  }

  // the wind strength random walk changes at most every 128 updates and self-corrects towards
  // 10..20,
  // so it can never leave the 0..30 band
  @GameTest(template = EMPTY)
  public static void windSimStrengthStaysBounded(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    WindSim sim = new WindSim(level);
    double weather = weatherMultiplier(level);
    double peakY = windPeakY(level);

    for (int step = 0; step < 300; step++) {
      for (int i = 0; i < 128; i++) {
        sim.updateWind();
      }

      double strength = sim.getWindAt(peakY) / weather / 2.4;
      helper.assertTrue(
          strength >= -0.001 && strength <= 30.001,
          "wind base strength should stay within 0..30, has "
              + strength
              + " after "
              + step
              + " steps");
    }

    helper.succeed();
  }

  // rain boosts the wind 1.25x and thunder 1.5x; isolated in its own batch so the weather change
  // cannot disturb the daylight-dependent tests
  @GameTest(template = EMPTY, timeoutTicks = 600, batch = "ic2WindWeather")
  public static void windSimWeatherBoostsWind(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    level.setWeatherParameters(100000, 0, false, false);
    WindSim sim = new WindSim(level);
    double peakY = windPeakY(level);
    double clear = sim.getWindAt(peakY);
    helper.assertFalse(level.isRaining(), "weather should start out clear");

    level.setWeatherParameters(0, 100000, true, false);
    helper.runAtTickTime(
        200,
        () -> {
          helper.assertTrue(level.isRaining() && !level.isThundering(), "rain should have set in");
          Ic2GameTestAssertions.assertNear(
              helper, sim.getWindAt(peakY), clear * 1.25, "wind strength in rain");
          level.setWeatherParameters(0, 100000, true, true);
        });
    helper.runAtTickTime(
        500,
        () -> {
          helper.assertTrue(level.isThundering(), "thunder should have set in");
          Ic2GameTestAssertions.assertNear(
              helper, sim.getWindAt(peakY), clear * 1.5, "wind strength in a thunderstorm");
          level.setWeatherParameters(100000, 0, false, false);
          helper.succeed();
        });
  }

  // wind meter used in the air: measures at the player's height for 50 EU, and does nothing when
  // empty
  @GameTest(template = EMPTY)
  public static void windMeterMeasuresAmbientWind(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack meter = ElectricItemManager.getCharged(Ic2Items.WIND_METER, Double.POSITIVE_INFINITY);
    player.setItemInHand(InteractionHand.MAIN_HAND, meter);

    InteractionResult result =
        meter.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult();
    helper.assertValueEqual(result, InteractionResult.SUCCESS, "wind meter use result");
    helper.assertValueEqual(
        ElectricItem.manager.getCharge(meter),
        WIND_METER_MAX_CHARGE - WIND_METER_USE_COST,
        "wind meter charge after one measurement");

    ItemStack empty = new ItemStack(Ic2Items.WIND_METER);
    player.setItemInHand(InteractionHand.MAIN_HAND, empty);
    InteractionResult emptyResult =
        empty.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult();
    helper.assertValueEqual(
        emptyResult, InteractionResult.PASS, "empty wind meter should not measure");
    helper.succeed();
  }

  // wind meter on an idle turbine: fails both without a rotor and with a blocked rotor, costing no
  // energy
  @GameTest(template = EMPTY)
  public static void windMeterRejectsIdleTurbine(GameTestHelper helper) {
    helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.WIND_KINETIC_GENERATOR);
    TileEntityWindKineticGenerator wind =
        getTe(helper, new BlockPos(1, 1, 1), TileEntityWindKineticGenerator.class);
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack meter = ElectricItemManager.getCharged(Ic2Items.WIND_METER, Double.POSITIVE_INFINITY);
    BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));

    helper.assertValueEqual(
        useMeterOn(meter, player, pos),
        InteractionResult.FAIL,
        "meter result with no rotor installed");

    // at ground level the 5x5 rotor plane overlaps the gametest platform, so the turbine stays
    // blocked
    wind.rotorSlot.put(0, new ItemStack(Ic2Items.WOODEN_ROTOR));
    helper.assertValueEqual(
        useMeterOn(meter, player, pos),
        InteractionResult.FAIL,
        "meter result with a blocked rotor");

    helper.assertValueEqual(
        ElectricItem.manager.getCharge(meter),
        WIND_METER_MAX_CHARGE,
        "failed measurements must not cost energy");
    helper.succeed();
  }

  // wind meter on a running turbine: reports the effective wind strength and consumes energy
  @GameTest(template = EMPTY, timeoutTicks = 200)
  public static void windMeterMeasuresRunningTurbine(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    // offset from the peak so simultaneously running wind tests cannot obstruct each other
    BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1)).atY(windPeakY(level) + 12);
    level.setBlockAndUpdate(pos, Ic2Blocks.WIND_KINETIC_GENERATOR.defaultBlockState());
    BlockEntity be = level.getBlockEntity(pos);
    if (!(be instanceof TileEntityWindKineticGenerator wind)) {
      throw new IllegalStateException(
          "expected TileEntityWindKineticGenerator at " + pos + ", found " + be);
    }

    wind.rotorSlot.put(0, new ItemStack(Ic2Items.WOODEN_ROTOR));
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack meter = ElectricItemManager.getCharged(Ic2Items.WIND_METER, Double.POSITIVE_INFINITY);

    helper.succeedWhen(
        () -> {
          helper.assertTrue(wind.getActive(), "turbine at y=" + pos.getY() + " should be active");
          helper.assertValueEqual(
              useMeterOn(meter, player, pos),
              InteractionResult.SUCCESS,
              "meter result on a running turbine");
          helper.assertTrue(
              ElectricItem.manager.getCharge(meter) <= WIND_METER_MAX_CHARGE - WIND_METER_USE_COST,
              "measurement should cost energy");
          level.removeBlock(pos, false);
        });
  }

  private static void assertRotor(
      GameTestHelper helper,
      Item item,
      int diameter,
      float efficiency,
      int minWind,
      int maxWind,
      int durability,
      boolean fitsWater) {
    ItemStack stack = new ItemStack(item);
    IKineticRotor rotor = (IKineticRotor) item;
    String name = stack.getDescriptionId();
    helper.assertValueEqual(rotor.getDiameter(stack), diameter, "diameter of " + name);
    helper.assertValueEqual(rotor.getEfficiency(stack), efficiency, "efficiency of " + name);
    helper.assertValueEqual(
        rotor.getMinWindStrength(stack), minWind, "min wind strength of " + name);
    helper.assertValueEqual(
        rotor.getMaxWindStrength(stack), maxWind, "max wind strength of " + name);
    helper.assertValueEqual(stack.getMaxDamage(), durability, "durability of " + name);
    helper.assertTrue(
        rotor.isAcceptedType(stack, IKineticRotor.GearboxType.WIND),
        name + " should fit wind turbines");
    helper.assertValueEqual(
        rotor.isAcceptedType(stack, IKineticRotor.GearboxType.WATER),
        fitsWater,
        "water gearbox fit of " + name);
  }

  private static InteractionResult useMeterOn(ItemStack meter, Player player, BlockPos pos) {
    UseOnContext context =
        new UseOnContext(
            player,
            InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
    return meter.getItem().onItemUseFirst(meter, context);
  }

  private static double weatherMultiplier(ServerLevel level) {
    return level.isThundering() ? 1.5 : level.isRaining() ? 1.25 : 1.0;
  }

  private static int windPeakY(ServerLevel level) {
    return KineticGeneratorGameTests.windPeakY(level);
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
