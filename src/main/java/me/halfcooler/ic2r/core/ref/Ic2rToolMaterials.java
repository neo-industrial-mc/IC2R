package me.halfcooler.ic2r.core.ref;

import java.util.function.Supplier;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public enum Ic2rToolMaterials implements Tier
{
	BRONZE(2, 350, 6.0F, 2.0F, () -> Ingredient.of(Ic2rItems.BRONZE_INGOT)),
	CHAINSAW(3, 250, 12.0F, 9.0F, Ingredient::of);

	private final int miningLevel;
	private final int itemDurability;
	private final float miningSpeed;
	private final float attackDamage;
	private final int enchantAbility;
	private final Supplier<Ingredient> repairIngredient;

	Ic2rToolMaterials(int miningLevel, int itemDurability, float miningSpeed, float attackDamage, Supplier<Ingredient> repairIngredient)
	{
		this.miningLevel = miningLevel;
		this.itemDurability = itemDurability;
		this.miningSpeed = miningSpeed;
		this.attackDamage = attackDamage;
		this.enchantAbility = 14;
		this.repairIngredient = repairIngredient;
	}

	public int getUses()
	{
		return this.itemDurability;
	}

	public float getSpeed()
	{
		return this.miningSpeed;
	}

	public float getAttackDamageBonus()
	{
		return this.attackDamage;
	}

	/** Legacy mining level (0 wood … 4 netherite+). Kept for IC2R logic that still needs it. */
	public int getMiningLevel()
	{
		return this.miningLevel;
	}

	@Override
	public TagKey<Block> getIncorrectBlocksForDrops()
	{
		return switch (this.miningLevel)
		{
			case 0 -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
			case 1 -> BlockTags.INCORRECT_FOR_STONE_TOOL;
			case 2 -> BlockTags.INCORRECT_FOR_IRON_TOOL;
			case 3 -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
			default -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
		};
	}

	public int getEnchantmentValue()
	{
		return this.enchantAbility;
	}

	public @NotNull Ingredient getRepairIngredient()
	{
		return this.repairIngredient.get();
	}
}
