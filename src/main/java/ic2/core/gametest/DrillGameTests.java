package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class DrillGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final BlockPos STONE_POS = new BlockPos(1, 1, 1);

  @GameTest(template = EMPTY)
  public static void drillMiningCostsEnergy(GameTestHelper helper) {
    assertMiningCost(helper, Ic2Items.DRILL, 30000.0, 50.0);
  }

  @GameTest(template = EMPTY)
  public static void diamondDrillMiningCostsEnergy(GameTestHelper helper) {
    assertMiningCost(helper, Ic2Items.DIAMOND_DRILL, 30000.0, 80.0);
  }

  @GameTest(template = EMPTY)
  public static void iridiumDrillMiningCostsEnergy(GameTestHelper helper) {
    assertMiningCost(helper, Ic2Items.IRIDIUM_DRILL, 300000.0, 800.0);
  }

  private static void assertMiningCost(
      GameTestHelper helper, Item drill, double maxCharge, double cost) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    player.setGameMode(GameType.SURVIVAL);
    ItemStack stack = ElectricItemManager.getCharged(drill, Double.POSITIVE_INFINITY);
    helper.assertValueEqual(ElectricItem.manager.getCharge(stack), maxCharge, "initial charge");

    helper.setBlock(STONE_POS, Blocks.STONE);
    BlockPos pos = helper.absolutePos(STONE_POS);
    stack
        .getItem()
        .mineBlock(stack, helper.getLevel(), helper.getLevel().getBlockState(pos), pos, player);

    helper.assertValueEqual(
        ElectricItem.manager.getCharge(stack), maxCharge - cost, "charge after mining one block");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void drillSpeedScalesWithTier(GameTestHelper helper) {
    assertSpeedOnStone(helper, Ic2Items.DRILL, 8.0F);
    assertSpeedOnStone(helper, Ic2Items.DIAMOND_DRILL, 16.0F);
    assertSpeedOnStone(helper, Ic2Items.IRIDIUM_DRILL, 24.0F);
    helper.succeed();
  }

  private static void assertSpeedOnStone(GameTestHelper helper, Item drill, float expectedSpeed) {
    ItemStack charged = ElectricItemManager.getCharged(drill, Double.POSITIVE_INFINITY);
    Ic2GameTestAssertions.assertNear(
        helper,
        charged.getDestroySpeed(Blocks.STONE.defaultBlockState()),
        expectedSpeed,
        drill + " charged speed on stone");

    ItemStack empty = new ItemStack(drill);
    Ic2GameTestAssertions.assertNear(
        helper,
        empty.getDestroySpeed(Blocks.STONE.defaultBlockState()),
        1.0,
        drill + " empty speed on stone");
  }

  @GameTest(template = EMPTY)
  public static void drillHarvestLevelsByTier(GameTestHelper helper) {
    helper.assertTrue(
        new ItemStack(Ic2Items.DRILL).isCorrectToolForDrops(Blocks.STONE.defaultBlockState()),
        "drill should harvest stone");
    helper.assertFalse(
        new ItemStack(Ic2Items.DRILL).isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()),
        "iron-tier drill must not harvest obsidian");
    helper.assertTrue(
        new ItemStack(Ic2Items.DIAMOND_DRILL)
            .isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()),
        "diamond drill should harvest obsidian");
    helper.assertTrue(
        new ItemStack(Ic2Items.IRIDIUM_DRILL)
            .isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()),
        "iridium drill should harvest obsidian");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void drillWithoutChargeMinesForFree(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    player.setGameMode(GameType.SURVIVAL);
    ItemStack stack = new ItemStack(Ic2Items.DRILL);

    helper.setBlock(STONE_POS, Blocks.STONE);
    BlockPos pos = helper.absolutePos(STONE_POS);
    stack
        .getItem()
        .mineBlock(stack, helper.getLevel(), helper.getLevel().getBlockState(pos), pos, player);

    helper.assertValueEqual(
        ElectricItem.manager.getCharge(stack), 0.0, "empty drill charge must not go negative");
    helper.succeed();
  }

  // the iridium drill's mode switch key toggles between fortune III and silk touch
  @GameTest(template = EMPTY)
  public static void iridiumDrillModeSwitchTogglesSilkTouch(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    player.setGameMode(GameType.SURVIVAL);
    ItemStack stack =
        ElectricItemManager.getCharged(Ic2Items.IRIDIUM_DRILL, Double.POSITIVE_INFINITY);
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

    HolderLookup.RegistryLookup<Enchantment> enchants =
        helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    Holder<Enchantment> silkTouch = enchants.getOrThrow(Enchantments.SILK_TOUCH);
    Holder<Enchantment> fortune = enchants.getOrThrow(Enchantments.FORTUNE);

    Ic2GameTestUtil.pressModeSwitchKey(player);
    try {
      stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
      helper.assertValueEqual(
          EnchantmentHelper.getItemEnchantmentLevel(silkTouch, stack),
          1,
          "silk touch level after first toggle");
      helper.assertValueEqual(
          EnchantmentHelper.getItemEnchantmentLevel(fortune, stack),
          0,
          "fortune level after first toggle");

      stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
      helper.assertValueEqual(
          EnchantmentHelper.getItemEnchantmentLevel(silkTouch, stack),
          0,
          "silk touch level after second toggle");
      helper.assertValueEqual(
          EnchantmentHelper.getItemEnchantmentLevel(fortune, stack),
          3,
          "fortune level after second toggle");
    } finally {
      Ic2GameTestUtil.releaseModeSwitchKey(player);
    }

    helper.succeed();
  }
}
