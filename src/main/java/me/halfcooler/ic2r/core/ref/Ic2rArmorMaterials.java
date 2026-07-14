package me.halfcooler.ic2r.core.ref;

import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public enum Ic2rArmorMaterials implements ArmorMaterial
{
	BRONZE("ic2r:ic2_bronze", 15, new int[] { 2, 5, 6, 2 }, 9, 0.0F, () -> Ingredient.of(Ic2rItems.BRONZE_INGOT)),
	ALLOY("ic2r:ic2_alloy", 50, new int[] { 4, 7, 9, 4 }, 12, 2.0F, () -> Ingredient.of(Ic2rItems.ALLOY)),
	NANO_SUIT("ic2r:ic2_nano", 0, new int[] { 0, 0, 0, 0 }, 0, 2.0F, Ingredient::of),
	QUANTUM_SUIT("ic2r:ic2_quantum", 0, new int[] { 0, 0, 0, 0 }, 0, 2.0F, Ingredient::of),
	NIGHT_VISION_GOGGLES("ic2r:ic2_night_vision", 0, new int[] { 0, 0, 0, 3 }, 0, 2.0F, Ingredient::of),
	SOLAR_HELMET("ic2r:ic2_solar_helmet", 0, new int[] { 0, 0, 0, 3 }, 0, 0.0F, Ingredient::of),
	STATIC_BOOTS("ic2r:ic2_static_boots", SoundEvents.ARMOR_EQUIP_LEATHER, new int[] { 3, 0, 0, 0 }, 0.0F),
	HAZMAT("ic2r:ic2_hazmat", SoundEvents.ARMOR_EQUIP_LEATHER, new int[] { 3, 6, 8, 3 }, 2.0F),
	CF_PACK("ic2r:ic2_cf_pack", SoundEvents.ARMOR_EQUIP_IRON, new int[] { 0, 0, 8, 0 }, 2.0f),
	JET_PACK("ic2r:ic2_jet_pack", SoundEvents.ARMOR_EQUIP_IRON, new int[] { 0, 0, 8, 0 }, 2.0F),
	JET_PACK_ELECTRIC("ic2r:ic2_jet_pack_electric", SoundEvents.ARMOR_EQUIP_IRON, new int[] { 0, 0, 8, 0 }, 2.0F),
	BAT_PACK("ic2r:ic2_bat_pack", SoundEvents.ARMOR_EQUIP_IRON, new int[] { 0, 0, 8, 0 }, 2.0F),
	ADVANCED_BAT_PACK("ic2r:ic2_advanced_bat_pack", SoundEvents.ARMOR_EQUIP_IRON, new int[] { 0, 0, 8, 0 }, 2.0F),
	ENERGY_PACK("ic2r:ic2_energy_pack", SoundEvents.ARMOR_EQUIP_IRON, new int[] { 0, 0, 8, 0 }, 2.0F),
	LAP_PACK("ic2r:ic2_lap_pack", SoundEvents.ARMOR_EQUIP_IRON, new int[] { 0, 0, 8, 0 }, 2.0F);

	private static final int[] BASE_DURABILITY = new int[] { 13, 15, 16, 11 };
	private final String name;
	private final int durabilityMultiplier;
	private final int[] protectionAmounts;
	private final int enchantAbility;
	private final SoundEvent equipSound;
	private final float toughness;
	private final float knockbackResistance;
	private final Supplier<Ingredient> repairIngredientSupplier;

	Ic2rArmorMaterials(String name, int durabilityMultiplier, int[] protectionAmounts, int enchantAbility, float toughness, Supplier<Ingredient> repairIngredientSupplier)
	{
		this.name = name;
		this.durabilityMultiplier = durabilityMultiplier;
		this.protectionAmounts = protectionAmounts;
		this.enchantAbility = enchantAbility;
		this.equipSound = SoundEvents.ARMOR_EQUIP_IRON;
		this.toughness = toughness;
		this.knockbackResistance = (float) 0.0;
		this.repairIngredientSupplier = repairIngredientSupplier;
	}
	
	Ic2rArmorMaterials(String name, SoundEvent equipSound, int[] protectionAmounts, float toughness)
	{
		this.name = name;
		this.durabilityMultiplier = 0;
		this.enchantAbility = 0;
		this.equipSound = equipSound;
		this.knockbackResistance = 0.0F;
		this.protectionAmounts = protectionAmounts;
		this.toughness = toughness;
		this.repairIngredientSupplier = Ingredient::of;
	}

	public int getDurabilityForType(ArmorItem.Type type)
	{
		return BASE_DURABILITY[type.getSlot().getIndex()] * this.durabilityMultiplier;
	}

	public int getDefenseForType(ArmorItem.Type type)
	{
		return this.protectionAmounts[type.getSlot().getIndex()];
	}

	public int getEnchantmentValue()
	{
		return this.enchantAbility;
	}

	public @NotNull SoundEvent getEquipSound()
	{
		return this.equipSound;
	}

	public @NotNull Ingredient getRepairIngredient()
	{
		return this.repairIngredientSupplier.get();
	}

	public @NotNull String getName()
	{
		return this.name;
	}

	public float getToughness()
	{
		return this.toughness;
	}

	public float getKnockbackResistance()
	{
		return this.knockbackResistance;
	}
}
