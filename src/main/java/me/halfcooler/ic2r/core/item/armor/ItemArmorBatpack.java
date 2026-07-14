package me.halfcooler.ic2r.core.item.armor;

import me.halfcooler.ic2r.core.ref.Ic2rArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ItemArmorBatpack extends ItemArmorElectric
{
	public ItemArmorBatpack()
	{
		super(Ic2rArmorMaterials.BAT_PACK, EquipmentSlot.CHEST, new Properties(), 60000.0, 100.0, 1);
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
