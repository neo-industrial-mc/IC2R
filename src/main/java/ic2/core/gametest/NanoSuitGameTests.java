package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.item.armor.ItemArmorNanoSuit;
import ic2.core.ref.Ic2Items;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class NanoSuitGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final double MAX_CHARGE = 1000000.0;
	private static final double ENERGY_PER_DAMAGE = 5000.0;

	@GameTest(template = EMPTY)
	public static void nanosuitAbsorbsDamageAndDrainsCharge(GameTestHelper helper)
	{
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack chest = ElectricItemManager.getCharged(Ic2Items.NANO_CHESTPLATE, Double.POSITIVE_INFINITY);
		player.setItemSlot(EquipmentSlot.CHEST, chest);

		LivingEntity attacker = helper.spawn(EntityType.PIG, new BlockPos(1, 1, 1));
		DamageSource source = helper.getLevel().damageSources().mobAttack(attacker);

		// chest absorbs baseRatio 0.4 * nano ratio 0.9 = 36% of incoming damage
		float remaining = ItemArmorElectric.damageArmor(player, source, 10.0F);

		assertNear(helper, remaining, 10.0 - 10.0 * 0.4 * 0.9, "damage left after absorption");
		assertNear(helper, ElectricItem.manager.getCharge(chest), MAX_CHARGE - 10.0 * 0.4 * 0.9 * ENERGY_PER_DAMAGE, "charge after absorption");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void nanosuitAbsorptionLimitedByCharge(GameTestHelper helper)
	{
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		// 5000 EU only covers 1 point of damage
		ItemStack chest = ElectricItemManager.getCharged(Ic2Items.NANO_CHESTPLATE, ENERGY_PER_DAMAGE);
		player.setItemSlot(EquipmentSlot.CHEST, chest);

		LivingEntity attacker = helper.spawn(EntityType.PIG, new BlockPos(1, 1, 1));
		DamageSource source = helper.getLevel().damageSources().mobAttack(attacker);

		float remaining = ItemArmorElectric.damageArmor(player, source, 10.0F);

		assertNear(helper, remaining, 9.0, "damage left with nearly empty suit");
		assertNear(helper, ElectricItem.manager.getCharge(chest), 0.0, "charge after absorption");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void nanobootsAbsorbShortFallsOnly(GameTestHelper helper)
	{
		ItemStack boots = ElectricItemManager.getCharged(Ic2Items.NANO_BOOTS, Double.POSITIVE_INFINITY);
		ItemArmorNanoSuit item = (ItemArmorNanoSuit) boots.getItem();

		// 6 blocks -> 3 points of fall damage, absorbed for 3 * 5000 EU
		helper.assertTrue(item.absorbFall(boots, 6.0F), "nano boots should absorb a 6 block fall");
		assertNear(helper, ElectricItem.manager.getCharge(boots), MAX_CHARGE - 3 * ENERGY_PER_DAMAGE, "charge after absorbed fall");

		// 12 blocks -> 9 points, at or above the 8 point cutoff
		helper.assertFalse(item.absorbFall(boots, 12.0F), "nano boots must not absorb a 12 block fall");
		assertNear(helper, ElectricItem.manager.getCharge(boots), MAX_CHARGE - 3 * ENERGY_PER_DAMAGE, "unabsorbed fall must not drain charge");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void nanochestArmorBonusRequiresCharge(GameTestHelper helper)
	{
		ItemStack uncharged = new ItemStack(Ic2Items.NANO_CHESTPLATE);
		assertNear(helper, getArmorBonus(uncharged, EquipmentSlotGroup.CHEST), 0.0, "uncharged chest armor");

		ItemStack charged = ElectricItemManager.getCharged(Ic2Items.NANO_CHESTPLATE, Double.POSITIVE_INFINITY);
		assertNear(helper, getArmorBonus(charged, EquipmentSlotGroup.CHEST), ItemArmorNanoSuit.CHARGED_PROTECTION[EquipmentSlot.CHEST.getIndex()], "charged chest armor");

		helper.succeed();
	}

	static double getArmorBonus(ItemStack stack, EquipmentSlotGroup slotGroup)
	{
		double[] total = {0.0};
		stack.forEachModifier(slotGroup, (attribute, modifier) ->
		{
			if (attribute == Attributes.ARMOR)
			{
				total[0] += modifier.amount();
			}
		});

		return total[0];
	}

	static void assertNear(GameTestHelper helper, double actual, double expected, String name)
	{
		helper.assertTrue(Math.abs(actual - expected) < 1.0E-6, name + ": expected " + expected + ", got " + actual);
	}
}
