package me.halfcooler.ic2r.core.ref;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoForge 1.21 armor materials are registry entries ({@link Holder}{@code <ArmorMaterial>}), not enums.
 */
public final class Ic2rArmorMaterials
{
	public static final DeferredRegister<ArmorMaterial> REGISTRY =
		DeferredRegister.create(Registries.ARMOR_MATERIAL, "ic2r");

	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BRONZE =
		register("bronze", 15, new int[] { 2, 5, 6, 2 }, 9, 0.0F, () -> Ingredient.of(Ic2rItems.BRONZE_INGOT), SoundEvents.ARMOR_EQUIP_IRON);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ALLOY =
		register("alloy", 50, new int[] { 4, 7, 9, 4 }, 12, 2.0F, () -> Ingredient.of(Ic2rItems.ALLOY), SoundEvents.ARMOR_EQUIP_IRON);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> NANO_SUIT =
		register("nano", 0, new int[] { 0, 0, 0, 0 }, 0, 2.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_IRON);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> QUANTUM_SUIT =
		register("quantum", 0, new int[] { 0, 0, 0, 0 }, 0, 2.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_IRON);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> NIGHT_VISION_GOGGLES =
		register("night_vision", 0, new int[] { 0, 0, 0, 3 }, 0, 2.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_IRON);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SOLAR_HELMET =
		register("solar_helmet", 0, new int[] { 0, 0, 0, 3 }, 0, 0.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_IRON);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> STATIC_BOOTS =
		register("static_boots", 0, new int[] { 3, 0, 0, 0 }, 0, 0.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_LEATHER);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HAZMAT =
		register("hazmat", 0, new int[] { 3, 6, 8, 3 }, 0, 2.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_LEATHER);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CF_PACK =
		register("cf_pack", 0, new int[] { 0, 0, 8, 0 }, 0, 2.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_IRON);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> JET_PACK =
		register("jet_pack", 0, new int[] { 0, 0, 8, 0 }, 0, 2.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_IRON);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> JET_PACK_ELECTRIC =
		register("jet_pack_electric", 0, new int[] { 0, 0, 8, 0 }, 0, 2.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_IRON);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BAT_PACK =
		register("bat_pack", 0, new int[] { 0, 0, 8, 0 }, 0, 2.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_IRON);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ADVANCED_BAT_PACK =
		register("advanced_bat_pack", 0, new int[] { 0, 0, 8, 0 }, 0, 2.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_IRON);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ENERGY_PACK =
		register("energy_pack", 0, new int[] { 0, 0, 8, 0 }, 0, 2.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_IRON);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LAP_PACK =
		register("lap_pack", 0, new int[] { 0, 0, 8, 0 }, 0, 2.0F, Ingredient::of, SoundEvents.ARMOR_EQUIP_IRON);

	private Ic2rArmorMaterials()
	{
	}

	/**
	 * protectionAmounts index order matches legacy IC2: boots, leggings, chest, helmet
	 * (same as {@link EquipmentSlot} FEET/LEGS/CHEST/HEAD index).
	 */
	private static DeferredHolder<ArmorMaterial, ArmorMaterial> register(
		String name,
		int durabilityMultiplierUnused,
		int[] protectionAmounts,
		int enchantAbility,
		float toughness,
		Supplier<Ingredient> repair,
		Holder<SoundEvent> equipSound
	)
	{
		return REGISTRY.register(name, () -> {
			Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
			// ArmorItem.Type order: HELMET, CHESTPLATE, LEGGINGS, BOOTS, BODY
			// Legacy array: [boots, legs, chest, helmet] by EquipmentSlot index
			defense.put(ArmorItem.Type.BOOTS, protectionAmounts[0]);
			defense.put(ArmorItem.Type.LEGGINGS, protectionAmounts[1]);
			defense.put(ArmorItem.Type.CHESTPLATE, protectionAmounts[2]);
			defense.put(ArmorItem.Type.HELMET, protectionAmounts[3]);
			// Texture path: assets/ic2r/textures/models/armor/ic2_<name>_layer_{1|2}.png
			// (legacy IC2 asset names keep the "ic2_" prefix on disk)
			return new ArmorMaterial(
				defense,
				enchantAbility,
				equipSound,
				repair,
				List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("ic2r", "ic2_" + name))),
				toughness,
				0.0F
			);
		});
	}
}
