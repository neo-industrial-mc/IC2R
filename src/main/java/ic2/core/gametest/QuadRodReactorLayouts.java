package ic2.core.gametest;

/**
 * Full-size (9 columns by 6 rows) reactor layouts for one through six quad uranium rods.
 *
 * <p>{@code S}, {@code D}, and {@code Q} are single, dual, and quad uranium fuel rods; {@code O} is
 * an overclocked heat vent; and {@code C} is a component heat vent. The progression layouts keep
 * rods separated and use progressively larger direct-cooling vent networks while leaving
 * unnecessary slots empty.
 */
final class QuadRodReactorLayouts {
  static final char EMPTY = '.';
  static final char SINGLE_ROD = 'S';
  static final char DUAL_ROD = 'D';
  static final char QUAD_ROD = 'Q';
  static final char SINGLE_MOX_ROD = 'm';
  static final char DUAL_MOX_ROD = 'M';
  static final char QUAD_MOX_ROD = 'q';
  static final char OVERCLOCKED_VENT = 'O';
  static final char COMPONENT_VENT = 'C';
  static final char REACTOR_VENT = 'R';

  private static final String[][] LAYOUTS = {
    {"CO.......", "OQO......", ".OC......", ".........", ".........", "........."},
    {"CO.......", "OQO......", ".OCO.....", "..OQO....", "...OC....", "........."},
    {".OC.OC...", "OQOOQO...", "COOCO....", ".OQO.....", ".CO......", "........."},
    {"CO.CO....", "OQOOQO...", ".OCOOCO..", "..OQOOQO.", "...OC.OC.", "........."},
    {".OC.OC.OC", "OQOOQOOQO", "COOCOOCO.", ".OQOOQO..", ".CO.CO...", "........."},
    {"......CO.", ".CO.COOQO", ".OQOOQOOC", "COOCOOCO.", "OQOOQOOQO", ".OC.OC.OC"}
  };

  private static final int[] VENT_COUNTS = {6, 11, 17, 22, 28, 34};

  // 84 reactor output units (420 EU/t with the default x5 nuclear multiplier).
  private static final String[] MAX_OUTPUT_SIX_QUAD_LAYOUT = {
    "DCODSOCO.", "OCOOCOOQO", ".OQOOQOOC", "COOCOOCOO", "OQOOQOOQO", ".OCOOCCOC"
  };

  // Four dual and two single MOX rods, clocked for 13 powered cycles and one cooling cycle.
  private static final String[] CLOCKED_MIXED_MOX_LAYOUT = {
    "MMMCCRCRR", "MmmCOCOCR", "CCCOCOCOC", "RCOCOCOCR", "RRCRCRCRR", "RRRRRRRRR"
  };

  // Five dual and one single MOX rod, clocked for four powered cycles and one cooling cycle.
  private static final String[] CLOCKED_HIGH_OUTPUT_MOX_LAYOUT = {
    "MMMCCCCCC", "MmMCOCOCC", "CCCOCOCOC", "COCCOCOCC", "RCOCCRCRR", "RRCRRRRRR"
  };

  // Two quad, two dual, and one single MOX rod, clocked for 41 powered cycles and one cooling
  // cycle.
  private static final String[] CLOCKED_1020_EU_MOX_LAYOUT = {
    "qmMMqCRCR", "CCCCCOCOC", "RCOCOCOCR", "COCOCOCOC", "RCOCOCOCR", "RRCRCRCRR"
  };

  static String[] forRodCount(int rodCount) {
    if (rodCount < 1 || rodCount > LAYOUTS.length) {
      throw new IllegalArgumentException("quad rod count must be between 1 and 6: " + rodCount);
    }

    return LAYOUTS[rodCount - 1].clone();
  }

  static int ventCount(int rodCount) {
    if (rodCount < 1 || rodCount > VENT_COUNTS.length) {
      throw new IllegalArgumentException("quad rod count must be between 1 and 6: " + rodCount);
    }

    return VENT_COUNTS[rodCount - 1];
  }

  static String[] maximumOutputSixQuadLayout() {
    return MAX_OUTPUT_SIX_QUAD_LAYOUT.clone();
  }

  static String[] clockedMixedMoxLayout() {
    return CLOCKED_MIXED_MOX_LAYOUT.clone();
  }

  static String[] clockedHighOutputMoxLayout() {
    return CLOCKED_HIGH_OUTPUT_MOX_LAYOUT.clone();
  }

  static String[] clocked1020EuMoxLayout() {
    return CLOCKED_1020_EU_MOX_LAYOUT.clone();
  }

  private QuadRodReactorLayouts() {}
}
