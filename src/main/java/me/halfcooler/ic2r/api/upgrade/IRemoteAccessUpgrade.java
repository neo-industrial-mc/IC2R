package me.halfcooler.ic2r.api.upgrade;

import net.minecraft.world.item.ItemStack;

public interface IRemoteAccessUpgrade extends IUpgradeItem
{
	int getRangeAmplification(ItemStack var1, IUpgradableBlock var2, int var3);
}
