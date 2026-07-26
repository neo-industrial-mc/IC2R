package ic2.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Guards block-model JSON shapes that fix client-only rendering bugs; the render result itself is
 * not observable from a test, so these assert on the shipped resources instead.
 */
class ModelGuardTest {

  // The old elements-based personal_chest_shape parent left gaps you could see the world through
  // (x-ray). The model must be a full opaque cube with a distinct front face (upstream 3ad6936d).
  @Test
  void personalChestModelIsFullCube() {
    JsonObject model = readModel("assets/ic2/models/block/personal/personal_chest.json");
    assertEquals(
        "block/cube",
        model.get("parent").getAsString(),
        "personal chest model must use the full block/cube parent");
    JsonObject textures = model.getAsJsonObject("textures");
    for (String face : new String[] {"down", "up", "north", "east", "south", "west"}) {
      assertTrue(textures.has(face), "personal chest model must texture face " + face);
    }
    assertNotEquals(
        textures.get("north").getAsString(),
        textures.get("east").getAsString(),
        "personal chest front face must keep its distinct texture");
  }

  // Active-state machine models used cube_bottom_top, which painted the front ("side") texture on
  // all four sides. They must be full cubes with a front face distinct from the other sides
  // (upstream 4d98c9cb).
  @Test
  void activeMachineModelsKeepDistinctFrontFace() {
    String[] models = {
      "assets/ic2/models/block/generator/reactor/nuclear_reactor_active.json",
      "assets/ic2/models/block/machine/resource/cropmatron_active.json",
      "assets/ic2/models/block/wiring/storage/electrolyzer_active.json",
    };
    for (String path : models) {
      JsonObject model = readModel(path);
      assertEquals(
          "block/cube",
          model.get("parent").getAsString(),
          path + " must use the block/cube parent");
      JsonObject textures = model.getAsJsonObject("textures");
      assertNotEquals(
          textures.get("north").getAsString(),
          textures.get("east").getAsString(),
          path + " must keep a front texture distinct from its sides");
    }
  }

  private static JsonObject readModel(String path) {
    try (InputStream in = ModelGuardTest.class.getClassLoader().getResourceAsStream(path)) {
      assertNotNull(in, "missing resource " + path);
      return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
          .getAsJsonObject();
    } catch (IOException e) {
      throw new RuntimeException("failed to read " + path, e);
    }
  }
}
