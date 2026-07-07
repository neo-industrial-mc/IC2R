package me.halfcooler.ic2r.addons.csas.init;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public enum CsasArmorMaterials implements ArmorMaterial
{
	SOLAR_HAT("ic2r_csas:solar_hat", 0, new int[] { 0, 0, 0, 0 }, 0, 0.0F, Ingredient::of);

	private static final int[] BASE_DURABILITY = new int[] { 13, 15, 16, 11 };
	private final String name;
	private final int durabilityMultiplier;
	private final int[] protectionAmounts;
	private final int enchantAbility;
	private final SoundEvent equipSound;
	private final float toughness;
	private final float knockbackResistance;
	private final java.util.function.Supplier<Ingredient> repairIngredientSupplier;

	CsasArmorMaterials(String name, int durabilityMultiplier, int[] protectionAmounts, int enchantAbility, float toughness, java.util.function.Supplier<Ingredient> repairIngredientSupplier)
	{
		this.name = name;
		this.durabilityMultiplier = durabilityMultiplier;
		this.protectionAmounts = protectionAmounts;
		this.enchantAbility = enchantAbility;
		this.equipSound = SoundEvents.ARMOR_EQUIP_IRON;
		this.toughness = toughness;
		this.knockbackResistance = 0.0F;
		this.repairIngredientSupplier = repairIngredientSupplier;
	}

	@Override
	public int getDurabilityForType(ArmorItem.Type type)
	{
		return BASE_DURABILITY[type.getSlot().getIndex()] * this.durabilityMultiplier;
	}

	@Override
	public int getDefenseForType(ArmorItem.Type type)
	{
		return this.protectionAmounts[type.getSlot().getIndex()];
	}

	@Override
	public int getEnchantmentValue()
	{
		return this.enchantAbility;
	}

	@Override
	public @NotNull SoundEvent getEquipSound()
	{
		return this.equipSound;
	}

	@Override
	public @NotNull Ingredient getRepairIngredient()
	{
		return this.repairIngredientSupplier.get();
	}

	@Override
	public @NotNull String getName()
	{
		return this.name;
	}

	@Override
	public float getToughness()
	{
		return this.toughness;
	}

	@Override
	public float getKnockbackResistance()
	{
		return this.knockbackResistance;
	}
}