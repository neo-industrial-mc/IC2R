package me.halfcooler.ic2r.core.gametest;

import me.halfcooler.ic2r.core.ref.Ic2rItems;
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

	private static void assertMaxDamage(GameTestHelper helper, ItemStack stack, int expected, String piece)
	{
		helper.assertValueEqual(stack.getMaxDamage(), expected, "bronze " + piece + " max durability");
	}

	private static void assertDamageConsumed(GameTestHelper helper, ItemStack stack, String piece)
	{
		helper.assertTrue(stack.getDamageValue() > 0, "bronze " + piece + " should lose durability");
	}
}
