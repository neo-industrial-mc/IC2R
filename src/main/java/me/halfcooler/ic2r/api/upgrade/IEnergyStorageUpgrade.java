package me.halfcooler.ic2r.api.upgrade;

import net.minecraft.world.item.ItemStack;

public interface IEnergyStorageUpgrade extends IUpgradeItem
{
	int getExtraEnergyStorage(ItemStack var1, IUpgradableBlock var2);

	double getEnergyStorageMultiplier(ItemStack var1, IUpgradableBlock var2);
}
