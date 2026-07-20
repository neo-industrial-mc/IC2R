package ic2.core.gametest;

import ic2.api.recipe.Recipes;
import ic2.core.ref.Ic2Items;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public final class ScrapBoxGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private ScrapBoxGameTests() {}

  @GameTest(template = EMPTY)
  public static void rightClickConsumesScrapBoxAndDropsReward(GameTestHelper helper) {
    helper.assertTrue(Recipes.scrapboxDrops != null, "scrap box drops should be initialized");

    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack scrapBoxes = new ItemStack(Ic2Items.SCRAP_BOX, 2);
    player.setItemInHand(InteractionHand.MAIN_HAND, scrapBoxes);

    InteractionResultHolder<ItemStack> result =
        scrapBoxes.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

    helper.assertTrue(
        result.getResult().consumesAction(), "right-clicking a scrap box should succeed");
    helper.assertValueEqual(scrapBoxes.getCount(), 1, "remaining scrap box count");

    List<ItemEntity> drops =
        helper
            .getLevel()
            .getEntitiesOfClass(
                ItemEntity.class,
                player.getBoundingBox().inflate(2.0),
                entity -> !entity.getItem().isEmpty());
    helper.assertTrue(!drops.isEmpty(), "right-clicking a scrap box should spawn a reward");
    helper.succeed();
  }
}
