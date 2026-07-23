package ic2.core.gametest;

import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public final class IridiumLootGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final int SAMPLE_COUNT = 128;

  private IridiumLootGameTests() {}

  @GameTest(template = EMPTY)
  public static void vanillaDungeonLootCanGenerateIridium(GameTestHelper helper) {
    LootTable dungeonLoot =
        helper
            .getLevel()
            .getServer()
            .reloadableRegistries()
            .getLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
    LootParams params =
        new LootParams.Builder(helper.getLevel())
            .withParameter(
                LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
            .create(LootContextParamSets.CHEST);

    for (int i = 0; i < SAMPLE_COUNT; i++) {
      for (ItemStack stack : dungeonLoot.getRandomItems(params)) {
        if (stack.is(Ic2Items.IRIDIUM_ORE) || stack.is(Ic2Items.IRIDIUM_SHARD)) {
          helper.succeed();
          return;
        }
      }
    }

    helper.fail(
        "Vanilla simple dungeon loot generated no IC2 iridium in " + SAMPLE_COUNT + " samples");
  }
}
