package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public final class ArmorGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final double NIGHT_VISION_GOGGLES_MAX_CHARGE = 200000.0;

  private ArmorGameTests() {}

  @GameTest(template = EMPTY)
  public static void bronzeArmorHasExpectedDurability(GameTestHelper helper) {
    assertMaxDamage(helper, Ic2Items.BRONZE_HELMET.getDefaultInstance(), 165, "bronze helmet");
    assertMaxDamage(
        helper, Ic2Items.BRONZE_CHESTPLATE.getDefaultInstance(), 240, "bronze chestplate");
    assertMaxDamage(helper, Ic2Items.BRONZE_LEGGINGS.getDefaultInstance(), 225, "bronze leggings");
    assertMaxDamage(helper, Ic2Items.BRONZE_BOOTS.getDefaultInstance(), 195, "bronze boots");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void bronzeArmorConsumesDurabilityWhenPlayerIsDamaged(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Ic2Items.BRONZE_HELMET));
    player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Ic2Items.BRONZE_CHESTPLATE));
    player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Ic2Items.BRONZE_LEGGINGS));
    player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Ic2Items.BRONZE_BOOTS));
    LivingEntity attacker = helper.spawn(EntityType.PIG, new BlockPos(1, 1, 1));
    DamageSource source = helper.getLevel().damageSources().mobAttack(attacker);

    helper.assertTrue(player.hurt(source, 4.0F), "the test player should take damage");
    assertDamageConsumed(helper, player.getItemBySlot(EquipmentSlot.HEAD), "helmet");
    assertDamageConsumed(helper, player.getItemBySlot(EquipmentSlot.CHEST), "chestplate");
    assertDamageConsumed(helper, player.getItemBySlot(EquipmentSlot.LEGS), "leggings");
    assertDamageConsumed(helper, player.getItemBySlot(EquipmentSlot.FEET), "boots");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void hazmatArmorHasExpectedProtectionAndDurability(GameTestHelper helper) {
    assertMaxDamage(helper, Ic2Items.HAZMAT_HELMET.getDefaultInstance(), 64, "hazmat helmet");
    assertMaxDamage(
        helper, Ic2Items.HAZMAT_CHESTPLATE.getDefaultInstance(), 64, "hazmat chestplate");
    assertMaxDamage(helper, Ic2Items.HAZMAT_LEGGINGS.getDefaultInstance(), 64, "hazmat leggings");
    assertMaxDamage(helper, Ic2Items.RUBBER_BOOTS.getDefaultInstance(), 64, "rubber boots");

    assertArmorProtection(helper, (ArmorItem) Ic2Items.RUBBER_BOOTS, 1, "rubber boots");
    assertArmorProtection(helper, (ArmorItem) Ic2Items.BRONZE_BOOTS, 2, "bronze boots");
    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void nightVisionGogglesRunFromEquippedHelmetSlot(GameTestHelper helper) {
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack goggles =
        ElectricItemManager.getCharged(Ic2Items.NIGHT_VISION_GOGGLES, Double.POSITIVE_INFINITY);
    StackUtil.getOrCreateNbtData(goggles).putBoolean("active", true);
    player.setItemSlot(EquipmentSlot.HEAD, goggles);

    player.getInventory().tick();

    Ic2GameTestAssertions.assertNear(
        helper,
        ElectricItem.manager.getCharge(goggles),
        NIGHT_VISION_GOGGLES_MAX_CHARGE - 1.0,
        "goggles charge after equipped inventory tick");
    helper.succeed();
  }

  private static void assertMaxDamage(
      GameTestHelper helper, ItemStack stack, int expected, String piece) {
    helper.assertValueEqual(stack.getMaxDamage(), expected, piece + " max durability");
  }

  private static void assertArmorProtection(
      GameTestHelper helper, ArmorItem armor, int expected, String piece) {
    helper.assertValueEqual(
        armor.getMaterial().value().getDefense(armor.getType()),
        expected,
        piece + " armor protection");
  }

  private static void assertDamageConsumed(GameTestHelper helper, ItemStack stack, String piece) {
    helper.assertTrue(stack.getDamageValue() > 0, "bronze " + piece + " should lose durability");
  }
}
