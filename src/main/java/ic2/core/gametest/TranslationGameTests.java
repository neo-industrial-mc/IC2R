package ic2.core.gametest;

import ic2.core.entity.block.ITntEntity;
import ic2.core.entity.block.NukeEntity;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class TranslationGameTests {
  private static final String TEMPLATE = "gametest/empty3x3x3";

  // Primed ITNT/Nuke entities had no entity.ic2.* lang keys, so death screens and F3 showed the
  // raw translation key (upstream ebc73b78). NeoForge loads mod en_us on the server, so getName
  // must resolve to a real name here.
  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void primedExplosiveEntitiesHaveTranslatedNames(GameTestHelper helper) {
    Entity itnt = new ITntEntity(helper.getLevel(), 0.0, 0.0, 0.0);
    Entity nuke = new NukeEntity(helper.getLevel(), 0.0, 0.0, 0.0, 1.0F, 0);

    assertTranslated(helper, itnt, "entity.ic2.itnt");
    assertTranslated(helper, nuke, "entity.ic2.nuke");
    helper.succeed();
  }

  private static void assertTranslated(GameTestHelper helper, Entity entity, String key) {
    String name = entity.getName().getString();
    helper.assertTrue(
        !name.isEmpty() && !name.equals(key),
        "entity name for " + key + " must be translated, got \"" + name + "\"");
  }
}
