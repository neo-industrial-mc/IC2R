package me.halfcooler.ic2r.api.upgrade;

import net.minecraft.world.item.ItemStack;

public interface IProcessingUpgrade extends IUpgradeItem
{
	int getExtraProcessTime(ItemStack var1, IUpgradableBlock var2);

	double getProcessTimeMultiplier(ItemStack var1, IUpgradableBlock var2);

	int getExtraEnergyDemand(ItemStack var1, IUpgradableBlock var2);

	double getEnergyDemandMultiplier(ItemStack var1, IUpgradableBlock var2);
}
