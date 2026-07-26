package ic2.core.energy;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Pure EU↔FE conversion contracts for {@link EnergyBridgeMath} (no blocks involved). */
@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public final class EnergyBridgeMathGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final double EPS = 1e-9;

  private EnergyBridgeMathGameTests() {}

  private static void assertEquals(GameTestHelper helper, long expected, long actual, String what) {
    helper.assertTrue(expected == actual, what + ": expected " + expected + ", got " + actual);
  }

  private static void assertEquals(
      GameTestHelper helper, double expected, double actual, String what) {
    helper.assertTrue(
        Math.abs(expected - actual) <= EPS, what + ": expected " + expected + ", got " + actual);
  }

  @GameTest(template = EMPTY)
  public static void euToFeCeilAndFloorAtDefaultRatio(GameTestHelper helper) {
    assertEquals(helper, 2.0, EnergyBridgeMath.DEFAULT_FE_PER_EU, "default ratio");

    assertEquals(helper, 2L, EnergyBridgeMath.euToFeCeil(1.0), "ceil(1.0)");
    assertEquals(helper, 2L, EnergyBridgeMath.euToFeFloor(1.0), "floor(1.0)");
    assertEquals(helper, 10L, EnergyBridgeMath.euToFeCeil(5.0), "ceil(5.0)");
    assertEquals(helper, 10L, EnergyBridgeMath.euToFeFloor(5.0), "floor(5.0)");

    assertEquals(helper, 1L, EnergyBridgeMath.euToFeCeil(0.1), "ceil(0.1)");
    assertEquals(helper, 0L, EnergyBridgeMath.euToFeFloor(0.1), "floor(0.1)");
    assertEquals(helper, 3L, EnergyBridgeMath.euToFeCeil(1.1), "ceil(1.1)");
    assertEquals(helper, 2L, EnergyBridgeMath.euToFeFloor(1.1), "floor(1.1)");

    assertEquals(helper, 4L, EnergyBridgeMath.euToFeCeil(1.0, 4.0), "ceil(1.0, ratio 4)");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void feToEuAndRoundTrip(GameTestHelper helper) {
    assertEquals(helper, 1.0, EnergyBridgeMath.feToEu(2L), "feToEu(2)");
    assertEquals(helper, 0.5, EnergyBridgeMath.feToEu(1L), "feToEu(1)");
    assertEquals(helper, 5.0, EnergyBridgeMath.feToEu(10L), "feToEu(10)");
    assertEquals(helper, 1.5, EnergyBridgeMath.feToEu(3L), "feToEu(3)");
    assertEquals(helper, 1.0, EnergyBridgeMath.feToEu(4L, 4.0), "feToEu(4, ratio 4)");

    long fe = EnergyBridgeMath.euToFeCeil(1.0);
    assertEquals(helper, 1.0, EnergyBridgeMath.feToEu(fe), "round trip 1 EU");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void residualAfterPartialAccept(GameTestHelper helper) {
    assertEquals(helper, 60L, EnergyBridgeMath.residualFe(100L, 40L), "residualFe partial");
    assertEquals(helper, 0L, EnergyBridgeMath.residualFe(100L, 100L), "residualFe full");
    assertEquals(helper, 0L, EnergyBridgeMath.residualFe(50L, 80L), "residualFe over-accept");
    assertEquals(helper, 100L, EnergyBridgeMath.residualFe(100L, 0L), "residualFe none");

    assertEquals(
        helper, 5.0, EnergyBridgeMath.residualEuAfterFeTransfer(10.0, 10L), "half accepted");
    long feReq = EnergyBridgeMath.euToFeCeil(10.0);
    assertEquals(
        helper, 0.0, EnergyBridgeMath.residualEuAfterFeTransfer(10.0, feReq), "fully accepted");
    assertEquals(
        helper, 7.5, EnergyBridgeMath.residualEuAfterFeTransfer(7.5, 0L), "nothing accepted");
    assertEquals(
        helper, 75.0, EnergyBridgeMath.residualEuAfterFeTransfer(100.0, 50L), "quarter accepted");
    assertEquals(
        helper, 0.0, EnergyBridgeMath.residualEuAfterFeTransfer(1.0, 100L), "over-accepted");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void zeroAndInvalidInputsYieldIdentity(GameTestHelper helper) {
    assertEquals(helper, 0L, EnergyBridgeMath.euToFeCeil(0.0), "ceil(0)");
    assertEquals(helper, 0L, EnergyBridgeMath.euToFeFloor(0.0), "floor(0)");
    assertEquals(helper, 0L, EnergyBridgeMath.euToFeCeil(-1.0), "ceil(-1)");
    assertEquals(helper, 0L, EnergyBridgeMath.euToFeFloor(-3.5), "floor(-3.5)");
    assertEquals(helper, 0.0, EnergyBridgeMath.feToEu(0L), "feToEu(0)");
    assertEquals(helper, 0.0, EnergyBridgeMath.feToEu(-5L), "feToEu(-5)");

    assertEquals(helper, 0L, EnergyBridgeMath.residualFe(0L, 0L), "residualFe(0,0)");
    assertEquals(helper, 0L, EnergyBridgeMath.residualFe(0L, 10L), "residualFe(0,10)");
    assertEquals(
        helper, 0.0, EnergyBridgeMath.residualEuAfterFeTransfer(0.0, 10L), "residual of 0 offer");
    assertEquals(
        helper,
        0.0,
        EnergyBridgeMath.residualEuAfterFeTransfer(-1.0, 10L),
        "residual of negative offer");

    helper.assertTrue(!EnergyBridgeMath.isValidRatio(0.0), "ratio 0 invalid");
    helper.assertTrue(!EnergyBridgeMath.isValidRatio(-2.0), "negative ratio invalid");
    helper.assertTrue(!EnergyBridgeMath.isValidRatio(Double.NaN), "NaN ratio invalid");
    helper.assertTrue(
        !EnergyBridgeMath.isValidRatio(Double.POSITIVE_INFINITY), "infinite ratio invalid");
    helper.assertTrue(
        EnergyBridgeMath.isValidRatio(EnergyBridgeMath.DEFAULT_FE_PER_EU), "default ratio valid");

    assertEquals(helper, 0L, EnergyBridgeMath.euToFeCeil(5.0, 0.0), "ceil with ratio 0");
    assertEquals(helper, 0L, EnergyBridgeMath.euToFeFloor(5.0, -1.0), "floor with ratio -1");
    assertEquals(helper, 0.0, EnergyBridgeMath.feToEu(10L, 0.0), "feToEu with ratio 0");
    assertEquals(
        helper,
        5.0,
        EnergyBridgeMath.residualEuAfterFeTransfer(5.0, 10L, 0.0),
        "residual with invalid ratio keeps offer");

    assertEquals(helper, 0L, EnergyBridgeMath.clampToIntEnergy(0L), "clamp(0)");
    assertEquals(helper, 0L, EnergyBridgeMath.clampToIntEnergy(-1L), "clamp(-1)");
    assertEquals(helper, 42L, EnergyBridgeMath.clampToIntEnergy(42L), "clamp(42)");
    assertEquals(
        helper,
        Integer.MAX_VALUE,
        EnergyBridgeMath.clampToIntEnergy(Integer.MAX_VALUE + 1L),
        "clamp saturates at int max");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void overflowSaturatesInsteadOfWrapping(GameTestHelper helper) {
    assertEquals(
        helper, Long.MAX_VALUE, EnergyBridgeMath.euToFeCeil(5e18, 4.0), "huge ceil saturates");
    assertEquals(
        helper, Long.MAX_VALUE, EnergyBridgeMath.euToFeFloor(5e18, 4.0), "huge floor saturates");

    assertEquals(
        helper, 0L, EnergyBridgeMath.euToFeCeil(Double.MAX_VALUE, 4.0), "infinite product ceil");
    assertEquals(
        helper, 0L, EnergyBridgeMath.euToFeFloor(Double.MAX_VALUE, 4.0), "infinite product floor");

    assertEquals(helper, 1L, EnergyBridgeMath.euToFeCeil(1e-9), "tiny EU still ceils to 1 FE");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void pushAccountingUsesFloorOfferAndResidualSpend(GameTestHelper helper) {
    double maxEu = 2048.0;
    long maxFe = EnergyBridgeMath.euToFeFloor(maxEu);
    assertEquals(helper, 4096L, maxFe, "2048 EU floors to 4096 FE");

    long accepted = maxFe / 2;
    assertEquals(
        helper,
        1024.0,
        EnergyBridgeMath.residualEuAfterFeTransfer(maxEu, accepted),
        "half accepted leaves half the EU");

    assertEquals(helper, 10.0, EnergyBridgeMath.feToEu(20L), "feToEu(20)");
    assertEquals(helper, 9.5, EnergyBridgeMath.feToEu(19L), "feToEu(19)");
    helper.succeed();
  }
}
