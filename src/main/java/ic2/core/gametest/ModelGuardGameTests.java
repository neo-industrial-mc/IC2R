package ic2.core.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Guards block-model JSON shapes that fix client-only rendering bugs; the render result itself is
 * not observable from a server gametest, so these assert on the shipped resources instead.
 */
@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ModelGuardGameTests {
  private static final String TEMPLATE = "gametest/empty3x3x3";

  // The old elements-based personal_chest_shape parent left gaps you could see the world through
  // (x-ray). The model must be a full opaque cube with a distinct front face (upstream 3ad6936d).
  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void personalChestModelIsFullCube(GameTestHelper helper) {
    JsonObject model = readModel(helper, "assets/ic2/models/block/personal/personal_chest.json");
    helper.assertTrue(
        "block/cube".equals(model.get("parent").getAsString()),
        "personal chest model must use the full block/cube parent, got " + model.get("parent"));
    JsonObject textures = model.getAsJsonObject("textures");
    for (String face : new String[] {"down", "up", "north", "east", "south", "west"}) {
      helper.assertTrue(textures.has(face), "personal chest model must texture face " + face);
    }
    helper.assertTrue(
        !textures.get("north").getAsString().equals(textures.get("east").getAsString()),
        "personal chest front face must keep its distinct texture");
    helper.succeed();
  }

  static JsonObject readModel(GameTestHelper helper, String path) {
    try (InputStream in = ModelGuardGameTests.class.getClassLoader().getResourceAsStream(path)) {
      if (in == null) {
        helper.fail("missing resource " + path);
      }
      return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
          .getAsJsonObject();
    } catch (IOException e) {
      throw new RuntimeException("failed to read " + path, e);
    }
  }
}
