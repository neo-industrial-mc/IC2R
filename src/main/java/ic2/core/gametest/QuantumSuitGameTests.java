package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.core.Ic2Potion;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import ic2.core.ref.Ic2Items;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class QuantumSuitGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final double MAX_CHARGE = 1.0E7;

	@GameTest(template = EMPTY)
	public static void quantumHelmetReplenishesAir(GameTestHelper helper)
	{
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack helmet = ElectricItemManager.getCharged(Ic2Items.QUANTUM_HELMET, Double.POSITIVE_INFINITY);
		player.setItemSlot(EquipmentSlot.HEAD, helmet);
		player.setAirSupply(0);

		helmet.getItem().inventoryTick(helmet, helper.getLevel(), player, 0, false);

		helper.assertValueEqual(player.getAirSupply(), 200, "air supply after tick");
		Ic2GameTestAssertions.assertNear(helper, ElectricItem.manager.getCharge(helmet), MAX_CHARGE - 1000.0, "charge after air refill");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void quantumHelmetCuresPoison(GameTestHelper helper)
	{
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack helmet = ElectricItemManager.getCharged(Ic2Items.QUANTUM_HELMET, Double.POSITIVE_INFINITY);
		player.setItemSlot(EquipmentSlot.HEAD, helmet);
		player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
		helper.assertTrue(player.hasEffect(MobEffects.POISON), "player should be poisoned before the tick");

		helmet.getItem().inventoryTick(helmet, helper.getLevel(), player, 0, false);

		helper.assertFalse(player.hasEffect(MobEffects.POISON), "poison should be cured");
		Ic2GameTestAssertions.assertNear(helper, ElectricItem.manager.getCharge(helmet), MAX_CHARGE - 10000.0, "charge after curing poison");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void quantumHelmetCuresRadiationAtFlatCost(GameTestHelper helper)
	{
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack helmet = ElectricItemManager.getCharged(Ic2Items.QUANTUM_HELMET, Double.POSITIVE_INFINITY);
		player.setItemSlot(EquipmentSlot.HEAD, helmet);
		player.addEffect(new MobEffectInstance(Ic2Potion.radiationHolder(), 200, 200));
		helper.assertTrue(player.hasEffect(Ic2Potion.radiationHolder()), "player should be irradiated before the tick");

		helmet.getItem().inventoryTick(helmet, helper.getLevel(), player, 0, false);

		helper.assertFalse(player.hasEffect(Ic2Potion.radiationHolder()), "radiation should be cured");
		// radiation amplifiers encode damage scaling, so the cost is base 10000 EU + amplifier * 100
		// rather than base * (amplifier + 1), which would drain 2010000 EU here
		Ic2GameTestAssertions.assertNear(helper, ElectricItem.manager.getCharge(helmet), MAX_CHARGE - (10000.0 + 200 * 100), "charge after curing radiation");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void quantumChestExtinguishesFire(GameTestHelper helper)
	{
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack chest = ElectricItemManager.getCharged(Ic2Items.QUANTUM_CHESTPLATE, Double.POSITIVE_INFINITY);
		player.setItemSlot(EquipmentSlot.CHEST, chest);
		player.setRemainingFireTicks(100);

		chest.getItem().inventoryTick(chest, helper.getLevel(), player, 0, false);

		helper.assertTrue(player.getRemainingFireTicks() <= 0, "fire should be extinguished");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void quantumBootsAbsorbFallWhileCharged(GameTestHelper helper)
	{
		ItemStack boots = ElectricItemManager.getCharged(Ic2Items.QUANTUM_BOOTS, Double.POSITIVE_INFINITY);
		ItemArmorQuantumSuit item = (ItemArmorQuantumSuit) boots.getItem();

		// 12 blocks -> 2 points beyond the 10 block grace, 2 * 20000 EU
		helper.assertTrue(item.absorbFall(boots, 12.0F), "charged quantum boots should absorb a 12 block fall");
		Ic2GameTestAssertions.assertNear(helper, ElectricItem.manager.getCharge(boots), MAX_CHARGE - 40000.0, "charge after absorbed fall");

		// unlike nano boots there is no damage cutoff, only the energy limit
		ItemStack lowBoots = ElectricItemManager.getCharged(Ic2Items.QUANTUM_BOOTS, 10000.0);
		helper.assertFalse(item.absorbFall(lowBoots, 12.0F), "boots without enough charge must not absorb the fall");
		Ic2GameTestAssertions.assertNear(helper, ElectricItem.manager.getCharge(lowBoots), 10000.0, "failed absorption must not drain charge");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void quantumArmorBonusRequiresCharge(GameTestHelper helper)
	{
		ItemStack uncharged = new ItemStack(Ic2Items.QUANTUM_LEGGINGS);
		Ic2GameTestAssertions.assertNear(helper, NanoSuitGameTests.getArmorBonus(uncharged, EquipmentSlotGroup.LEGS), 0.0, "uncharged leggings armor");

		ItemStack charged = ElectricItemManager.getCharged(Ic2Items.QUANTUM_LEGGINGS, Double.POSITIVE_INFINITY);
		Ic2GameTestAssertions.assertNear(helper, NanoSuitGameTests.getArmorBonus(charged, EquipmentSlotGroup.LEGS), ItemArmorQuantumSuit.CHARGED_PROTECTION[EquipmentSlot.LEGS.getIndex()], "charged leggings armor");

		helper.succeed();
	}
}
