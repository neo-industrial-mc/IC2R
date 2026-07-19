package me.halfcooler.ic2r.core.gametest;

import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.core.item.ElectricItemManager;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r")
@PrefixGameTestTemplate(false)
public final class ArmorGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";
	private static final double NIGHT_VISION_GOGGLES_MAX_CHARGE = 200000.0;
	private static final double NANO_HELMET_MAX_CHARGE = 1000000.0;

	private ArmorGameTests()
	{
	}

	@GameTest(template = EMPTY)
	public static void bronzeArmorHasExpectedDurability(GameTestHelper helper)
	{
		assertMaxDamage(helper, Ic2rItems.BRONZE_HELMET.getDefaultInstance(), 165, "helmet");
		assertMaxDamage(helper, Ic2rItems.BRONZE_CHESTPLATE.getDefaultInstance(), 240, "chestplate");
		assertMaxDamage(helper, Ic2rItems.BRONZE_LEGGINGS.getDefaultInstance(), 225, "leggings");
		assertMaxDamage(helper, Ic2rItems.BRONZE_BOOTS.getDefaultInstance(), 195, "boots");
		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void bronzeArmorConsumesDurabilityWhenPlayerIsDamaged(GameTestHelper helper)
	{
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack helmet = new ItemStack(Ic2rItems.BRONZE_HELMET);
		ItemStack chestplate = new ItemStack(Ic2rItems.BRONZE_CHESTPLATE);
		ItemStack leggings = new ItemStack(Ic2rItems.BRONZE_LEGGINGS);
		ItemStack boots = new ItemStack(Ic2rItems.BRONZE_BOOTS);
		player.setItemSlot(EquipmentSlot.HEAD, helmet);
		player.setItemSlot(EquipmentSlot.CHEST, chestplate);
		player.setItemSlot(EquipmentSlot.LEGS, leggings);
		player.setItemSlot(EquipmentSlot.FEET, boots);
		LivingEntity attacker = helper.spawn(EntityType.PIG, new BlockPos(1, 1, 1));
		DamageSource source = helper.getLevel().damageSources().mobAttack(attacker);

		helper.assertTrue(
			player.hurt(source, 4.0F),
			"the test player should take damage");
		assertDamageConsumed(helper, player.getItemBySlot(EquipmentSlot.HEAD), "helmet");
		assertDamageConsumed(helper, player.getItemBySlot(EquipmentSlot.CHEST), "chestplate");
		assertDamageConsumed(helper, player.getItemBySlot(EquipmentSlot.LEGS), "leggings");
		assertDamageConsumed(helper, player.getItemBySlot(EquipmentSlot.FEET), "boots");
		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void nightVisionGogglesRunFromEquippedHelmetSlot(GameTestHelper helper)
	{
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack goggles = ElectricItemManager.getCharged(Ic2rItems.NIGHT_VISION_GOGGLES, Double.POSITIVE_INFINITY);
		StackUtil.editTag(goggles, nbt -> nbt.putBoolean("active", true));
		player.setItemSlot(EquipmentSlot.HEAD, goggles);

		player.getInventory().tick();

		assertCharge(helper, goggles, NIGHT_VISION_GOGGLES_MAX_CHARGE - 1.0, "goggles");
		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void nanoHelmetNightVisionRunsFromEquippedHelmetSlot(GameTestHelper helper)
	{
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack helmet = ElectricItemManager.getCharged(Ic2rItems.NANO_HELMET, Double.POSITIVE_INFINITY);
		StackUtil.editTag(helmet, nbt -> nbt.putBoolean("night_vision", true));
		player.setItemSlot(EquipmentSlot.HEAD, helmet);

		player.getInventory().tick();

		assertCharge(helper, helmet, NANO_HELMET_MAX_CHARGE - 1.0, "nano helmet");
		helper.succeed();
	}

	private static void assertMaxDamage(GameTestHelper helper, ItemStack stack, int expected, String piece)
	{
		helper.assertValueEqual(stack.getMaxDamage(), expected, "bronze " + piece + " max durability");
	}

	private static void assertDamageConsumed(GameTestHelper helper, ItemStack stack, String piece)
	{
		helper.assertTrue(stack.getDamageValue() > 0, "bronze " + piece + " should lose durability");
	}

	private static void assertCharge(GameTestHelper helper, ItemStack stack, double expected, String item)
	{
		double actual = ElectricItem.manager.getCharge(stack);
		helper.assertTrue(Math.abs(actual - expected) < 1.0E-6, item + " charge after equipped inventory tick");
	}
}
