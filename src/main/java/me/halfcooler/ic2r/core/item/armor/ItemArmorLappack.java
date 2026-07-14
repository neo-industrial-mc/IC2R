package me.halfcooler.ic2r.core.item.armor;

import me.halfcooler.ic2r.core.ref.Ic2rArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ItemArmorLappack extends ItemArmorElectric
{
	public ItemArmorLappack()
	{
		super(Ic2rArmorMaterials.LAP_PACK, EquipmentSlot.CHEST, new Properties().rarity(Rarity.UNCOMMON), 2.0E7, 2500.0, 4);
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return true;
	}

	@Override
	public int getEnergyPerDamage()
	{
		return 0;
	}

	@Override
	public double getDamageAbsorptionRatio()
	{
		return 0.0;
	}
}
