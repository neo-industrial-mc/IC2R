package me.halfcooler.ic2r.core.item.armor;

import me.halfcooler.ic2r.api.item.IMetalArmor;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemArmorIC2R extends ArmorItem implements IMetalArmor
{
	public ItemArmorIC2R(Holder<ArmorMaterial> material, EquipmentSlot slot, Properties settings)
	{
		super(material, fromSlot(slot), settings);
	}

	public ItemArmorIC2R(Holder<ArmorMaterial> material, EquipmentSlot slot, int durabilityMultiplier, Properties settings)
	{
		super(material, fromSlot(slot), settings.durability(durabilityForSlot(slot, durabilityMultiplier)));
	}

	static int durabilityForSlot(EquipmentSlot slot, int durabilityMultiplier)
	{
		return fromSlot(slot).getDurability(durabilityMultiplier);
	}

	private static ArmorItem.Type fromSlot(EquipmentSlot slot)
	{
		return switch (slot)
		{
			case HEAD -> ArmorItem.Type.HELMET;
			case CHEST -> ArmorItem.Type.CHESTPLATE;
			case LEGS -> ArmorItem.Type.LEGGINGS;
			case FEET -> ArmorItem.Type.BOOTS;
			default -> throw new IllegalArgumentException("Invalid slot: " + slot);
		};
	}

	@Override
	public boolean isMetalArmor(ItemStack itemstack, Player player)
	{
		return true;
	}
}
