package me.halfcooler.ic2r.core.item.armor;

import net.minecraft.core.Holder;

import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.api.item.IElectricItem;
import me.halfcooler.ic2r.core.item.ElectricItemManager;
import me.halfcooler.ic2r.core.item.ElectricItemTooltipHandler;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;

public abstract class ItemArmorElectric extends ItemArmorIC2R implements IElectricItem
{
	protected final double maxCharge;
	protected final double transferLimit;
	protected final int tier;

	public ItemArmorElectric(Holder<ArmorMaterial> material, EquipmentSlot slot, Properties settings, double maxCharge, double transferLimit, int tier)
	{
		super(material, slot, settings);
		this.maxCharge = maxCharge;
		this.transferLimit = transferLimit;
		this.tier = tier;
	}

	public static float damageArmor(Player entity, DamageSource source, float amount)
	{
		// Void (/out_of_world) and /kill use BYPASSES_INVULNERABILITY; do not spend EU on them.
		if (source == null
			|| amount <= 0.0F
			|| source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)
			|| source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
		{
			return amount;
		}

		float remainingDamage = amount;

		for (EquipmentSlot slot : EquipmentSlot.values())
		{
			if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
			{
				continue;
			}

			ItemStack stack = entity.getItemBySlot(slot);
			if (stack.getItem() instanceof ItemArmorElectric electricArmor)
			{
				double absorptionRatio = electricArmor.getBaseAbsorptionRatio() * electricArmor.getDamageAbsorptionRatio();
				if (absorptionRatio <= 0.0)
				{
					continue;
				}

				int energyPerDamage = electricArmor.getEnergyPerDamage();
				if (energyPerDamage <= 0)
				{
					continue;
				}

				double availableEnergy = ElectricItem.manager.getCharge(stack);
				double maxAbsorbDamage = availableEnergy / energyPerDamage;
				double absorbedDamage = Math.min(remainingDamage * absorptionRatio, maxAbsorbDamage);

				if (absorbedDamage > 0.0)
				{
					electricArmor.damageArmor(entity, stack, source, absorbedDamage, slot);
					remainingDamage -= (float) absorbedDamage;
					if (remainingDamage <= 0.0F)
					{
						break;
					}
				}
			}
		}

		return remainingDamage;
	}

	public abstract int getEnergyPerDamage();

	public abstract double getDamageAbsorptionRatio();

	protected double getBaseAbsorptionRatio()
	{
		return switch (this.getEquipmentSlot())
		{
			case HEAD, FEET -> 0.15;
			case CHEST -> 0.4;
			case LEGS -> 0.3;
			default -> 0.0;
		};
	}

	public boolean isEnchantable(@NotNull ItemStack stack)
	{
		return false;
	}

	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems)
	{
		ElectricItemManager.addChargeVariants(this, subItems);
	}

	public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext world, @NotNull List<Component> tooltip, @NotNull TooltipFlag context)
	{
		ElectricItemTooltipHandler.addTooltip(stack, tooltip);
	}

	public void damageArmor(LivingEntity entity, ItemStack stack, DamageSource source, double damage, EquipmentSlot slot)
	{
		ElectricItem.manager.discharge(stack, damage * this.getEnergyPerDamage(), Integer.MAX_VALUE, true, false, false);
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return false;
	}

	@Override
	public double getMaxCharge(ItemStack stack)
	{
		return this.maxCharge;
	}

	@Override
	public int getTier(ItemStack stack)
	{
		return this.tier;
	}

	@Override
	public double getTransferLimit(ItemStack stack)
	{
		return this.transferLimit;
	}

	public boolean isBarVisible(@NotNull ItemStack stack)
	{
		return ElectricItem.manager.getChargeLevel(stack) < 1.0;
	}

	public int getBarWidth(@NotNull ItemStack stack)
	{
		return (int) Math.round(ElectricItem.manager.getChargeLevel(stack) * 13.0);
	}

	public int getBarColor(@NotNull ItemStack stack)
	{
		return Mth.hsvToRgb((float) (ElectricItem.manager.getChargeLevel(stack) / 3.0), 1.0F, 1.0F);
	}

	/**
	 * Charge-sensitive armor protection: replace default armor value when the piece has energy.
	 */
	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack)
	{
		ItemAttributeModifiers defaults = super.getDefaultAttributeModifiers(stack);
		boolean hasCharge = ElectricItem.manager.getCharge(stack) >= this.getEnergyPerDamage();
		if (!hasCharge)
		{
			return defaults;
		}

		Item armor = stack.getItem();
		EquipmentSlot slot = this.getEquipmentSlot();
		int protection;
		if (armor instanceof ItemArmorNanoSuit)
		{
			protection = ItemArmorNanoSuit.CHARGED_PROTECTION[slot.getIndex()];
		} else if (armor instanceof ItemArmorQuantumSuit)
		{
			protection = ItemArmorQuantumSuit.CHARGED_PROTECTION[slot.getIndex()];
		} else
		{
			return defaults;
		}

		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);
		ResourceLocation armorId = ResourceLocation.withDefaultNamespace("armor." + this.getType().getName());
		ResourceLocation armorModId = ResourceLocation.fromNamespaceAndPath("ic2r", "charged_armor." + this.getType().getName());

		defaults.forEach(group, (attribute, modifier) ->
		{
			if (!attribute.is(Attributes.ARMOR) || !modifier.is(armorId))
			{
				builder.add(attribute, modifier, group);
			}
		});
		builder.add(Attributes.ARMOR, new AttributeModifier(armorModId, protection, Operation.ADD_VALUE), group);
		return builder.build();
	}
}
