package me.halfcooler.ic2r.api.upgrade;

import net.minecraft.world.item.ItemStack;

public interface IAugmentationUpgrade extends IUpgradeItem
{
	int getAugmentation(ItemStack var1, IUpgradableBlock var2);
}
