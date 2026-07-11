package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.tool.AbstractItemNanoSaber;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class NanoSaberGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final double MAX_CHARGE = 160000.0;
  private static final double HIT_COST = 400.0;

  @GameTest(template = EMPTY)
  public static void nanosaberActivatesWhenCharged(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack saber = ElectricItemManager.getCharged(Ic2Items.NANO_SABER, Double.POSITIVE_INFINITY);
    player.setItemInHand(InteractionHand.MAIN_HAND, saber);

    helper.assertFalse(AbstractItemNanoSaber.isActive(saber), "saber should start inactive");

    saber.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    helper.assertTrue(
        AbstractItemNanoSaber.isActive(saber), "charged saber should activate on use");

    saber.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    helper.assertFalse(
        AbstractItemNanoSaber.isActive(saber), "second use should deactivate the saber");

    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void nanosaberRequiresChargeToActivate(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack saber = new ItemStack(Ic2Items.NANO_SABER);
    player.setItemInHand(InteractionHand.MAIN_HAND, saber);

    saber.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    helper.assertFalse(AbstractItemNanoSaber.isActive(saber), "empty saber must not activate");

    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void nanosaberAttackDrainsEnergy(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack saber = ElectricItemManager.getCharged(Ic2Items.NANO_SABER, Double.POSITIVE_INFINITY);
    player.setItemInHand(InteractionHand.MAIN_HAND, saber);
    saber.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    helper.assertTrue(
        AbstractItemNanoSaber.isActive(saber), "saber should be active before attacking");

    LivingEntity target = helper.spawn(EntityType.PIG, new BlockPos(1, 1, 1));
    saber.getItem().hurtEnemy(saber, target, player);

    helper.assertValueEqual(
        ElectricItem.manager.getCharge(saber), MAX_CHARGE - HIT_COST, "charge after one hit");
    helper.assertTrue(
        AbstractItemNanoSaber.isActive(saber), "saber should stay active while charged");

    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void nanosaberDeactivatesWhenDepleted(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    // enough to activate (16 EU), not enough for a hit (400 EU)
    ItemStack saber = ElectricItemManager.getCharged(Ic2Items.NANO_SABER, 300.0);
    player.setItemInHand(InteractionHand.MAIN_HAND, saber);
    saber.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    helper.assertTrue(
        AbstractItemNanoSaber.isActive(saber), "saber with 300 EU should still activate");

    LivingEntity target = helper.spawn(EntityType.PIG, new BlockPos(1, 1, 1));
    saber.getItem().hurtEnemy(saber, target, player);

    helper.assertFalse(
        AbstractItemNanoSaber.isActive(saber),
        "saber should turn off when it cannot pay the hit cost");

    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void nanosaberDamageBoostWhenActive(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack saber = ElectricItemManager.getCharged(Ic2Items.NANO_SABER, Double.POSITIVE_INFINITY);
    player.setItemInHand(InteractionHand.MAIN_HAND, saber);

    helper.assertValueEqual(getAttackDamageBonus(saber), 4.0, "inactive attack damage bonus");

    saber.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    helper.assertValueEqual(getAttackDamageBonus(saber), 20.0, "active attack damage bonus");

    helper.succeed();
  }

  private static double getAttackDamageBonus(ItemStack stack) {
    double[] total = {0.0};
    stack.forEachModifier(
        EquipmentSlotGroup.MAINHAND,
        (attribute, modifier) -> {
          if (attribute == Attributes.ATTACK_DAMAGE) {
            total[0] += modifier.amount();
          }
        });

    return total[0];
  }
}
