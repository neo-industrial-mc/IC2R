package me.halfcooler.ic2r.core.item.armor;

import me.halfcooler.ic2r.core.ref.Ic2rArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ItemArmorEnergypack extends ItemArmorElectric
{
	public ItemArmorEnergypack()
	{
		super(Ic2rArmorMaterials.ENERGY_PACK, EquipmentSlot.CHEST, new Properties(), 2000000.0, 1000.0, 3);
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
