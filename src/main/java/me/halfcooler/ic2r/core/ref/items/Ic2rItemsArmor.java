package me.halfcooler.ic2r.core.ref.items;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;
import me.halfcooler.ic2r.core.item.armor.ItemArmorAdvBatpack;
import me.halfcooler.ic2r.core.item.armor.ItemArmorBatpack;
import me.halfcooler.ic2r.core.item.armor.ItemArmorCFPack;
import me.halfcooler.ic2r.core.item.armor.ItemArmorEnergypack;
import me.halfcooler.ic2r.core.item.armor.ItemArmorHazmat;
import me.halfcooler.ic2r.core.item.armor.ItemArmorIC2R;
import me.halfcooler.ic2r.core.item.armor.ItemArmorJetpack;
import me.halfcooler.ic2r.core.item.armor.ItemArmorJetpackElectric;
import me.halfcooler.ic2r.core.item.armor.ItemArmorLappack;
import me.halfcooler.ic2r.core.item.armor.ItemArmorNanoSuit;
import me.halfcooler.ic2r.core.item.armor.ItemArmorNightVisionGoggles;
import me.halfcooler.ic2r.core.item.armor.ItemArmorQuantumSuit;
import me.halfcooler.ic2r.core.item.armor.ItemArmorSolarHelmet;
import me.halfcooler.ic2r.core.item.armor.ItemArmorStaticBoots;
import me.halfcooler.ic2r.core.item.tool.ItemNanoSaber;
import me.halfcooler.ic2r.core.item.tool.ItemSprayer;
import me.halfcooler.ic2r.core.ref.Ic2rArmorMaterials;
import me.halfcooler.ic2r.core.ref.Ic2rItems;

/** Domain item registrations: Armor and wearable equipment */
public final class Ic2rItemsArmor
{
	private Ic2rItemsArmor()
	{
	}

	public static final Item NANO_SABER = Ic2rItems.register("nano_saber", new ItemNanoSaber(new Properties().stacksTo(1)));
	public static final Item ALLOY_CHESTPLATE = Ic2rItems.register("alloy_chestplate", new ItemArmorIC2R(Ic2rArmorMaterials.ALLOY, EquipmentSlot.CHEST, new Properties()));
	public static final Item BRONZE_BOOTS = Ic2rItems.register("bronze_boots", new ItemArmorIC2R(Ic2rArmorMaterials.BRONZE, EquipmentSlot.FEET, new Properties()));
	public static final Item BRONZE_CHESTPLATE = Ic2rItems.register("bronze_chestplate", new ItemArmorIC2R(Ic2rArmorMaterials.BRONZE, EquipmentSlot.CHEST, new Properties()));
	public static final Item BRONZE_HELMET = Ic2rItems.register("bronze_helmet", new ItemArmorIC2R(Ic2rArmorMaterials.BRONZE, EquipmentSlot.HEAD, new Properties()));
	public static final Item BRONZE_LEGGINGS = Ic2rItems.register("bronze_leggings", new ItemArmorIC2R(Ic2rArmorMaterials.BRONZE, EquipmentSlot.LEGS, new Properties()));
	public static final Item CF_PACK = Ic2rItems.register("cf_pack", new ItemArmorCFPack(new Properties()));
	public static final Item FOAM_SPRAYER = Ic2rItems.register("foam_sprayer", new ItemSprayer(new Properties().stacksTo(1)));
	public static final Item HAZMAT_CHESTPLATE = Ic2rItems.register("hazmat_chestplate", new ItemArmorHazmat(EquipmentSlot.CHEST, new Properties()));
	public static final Item HAZMAT_HELMET = Ic2rItems.register("hazmat_helmet", new ItemArmorHazmat(EquipmentSlot.HEAD, new Properties()));
	public static final Item HAZMAT_LEGGINGS = Ic2rItems.register("hazmat_leggings", new ItemArmorHazmat(EquipmentSlot.LEGS, new Properties()));
	public static final Item JETPACK = Ic2rItems.register("jetpack", new ItemArmorJetpack(new Properties()));
	public static final Item JETPACK_ELECTRIC = Ic2rItems.register("jetpack_electric", new ItemArmorJetpackElectric());
	public static final Item BATPACK = Ic2rItems.register("batpack", new ItemArmorBatpack());
	public static final Item ADVANCED_BATPACK = Ic2rItems.register("advanced_batpack", new ItemArmorAdvBatpack());
	public static final Item ENERGY_PACK = Ic2rItems.register("energy_pack", new ItemArmorEnergypack());
	public static final Item LAPPACK = Ic2rItems.register("lappack", new ItemArmorLappack());
	public static final Item NANO_BOOTS = Ic2rItems.register("nano_boots", new ItemArmorNanoSuit(Ic2rArmorMaterials.NANO_SUIT, EquipmentSlot.FEET, new Properties().rarity(Rarity.UNCOMMON)));
	public static final Item NANO_CHESTPLATE = Ic2rItems.register("nano_chestplate", new ItemArmorNanoSuit(Ic2rArmorMaterials.NANO_SUIT, EquipmentSlot.CHEST, new Properties().rarity(Rarity.UNCOMMON)));
	public static final Item NANO_HELMET = Ic2rItems.register("nano_helmet", new ItemArmorNanoSuit(Ic2rArmorMaterials.NANO_SUIT, EquipmentSlot.HEAD, new Properties().rarity(Rarity.UNCOMMON)));
	public static final Item NANO_LEGGINGS = Ic2rItems.register("nano_leggings", new ItemArmorNanoSuit(Ic2rArmorMaterials.NANO_SUIT, EquipmentSlot.LEGS, new Properties().rarity(Rarity.UNCOMMON)));
	public static final Item QUANTUM_BOOTS = Ic2rItems.register("quantum_boots", new ItemArmorQuantumSuit(Ic2rArmorMaterials.QUANTUM_SUIT, EquipmentSlot.FEET, new Properties().rarity(Rarity.UNCOMMON)));
	public static final Item QUANTUM_CHESTPLATE = Ic2rItems.register("quantum_chestplate", new ItemArmorQuantumSuit(Ic2rArmorMaterials.QUANTUM_SUIT, EquipmentSlot.CHEST, new Properties().rarity(Rarity.UNCOMMON)));
	public static final Item QUANTUM_HELMET = Ic2rItems.register("quantum_helmet", new ItemArmorQuantumSuit(Ic2rArmorMaterials.QUANTUM_SUIT, EquipmentSlot.HEAD, new Properties().rarity(Rarity.UNCOMMON)));
	public static final Item QUANTUM_LEGGINGS = Ic2rItems.register("quantum_leggings", new ItemArmorQuantumSuit(Ic2rArmorMaterials.QUANTUM_SUIT, EquipmentSlot.LEGS, new Properties().rarity(Rarity.UNCOMMON)));
	public static final Item RUBBER_BOOTS = Ic2rItems.register("rubber_boots", new ItemArmorHazmat(EquipmentSlot.FEET, new Properties()));
	public static final Item NIGHT_VISION_GOGGLES = Ic2rItems.register("night_vision_goggles", new ItemArmorNightVisionGoggles(new Properties().durability(27)));
	public static final Item SOLAR_HELMET = Ic2rItems.register("solar_helmet", new ItemArmorSolarHelmet(new Properties()));
	public static final Item STATIC_BOOTS = Ic2rItems.register("static_boots", new ItemArmorStaticBoots(new Properties()));
}
