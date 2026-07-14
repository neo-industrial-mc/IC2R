package me.halfcooler.ic2r.api.upgrade;

import net.minecraft.world.item.ItemStack;

public interface IRedstoneSensitiveUpgrade extends IUpgradeItem
{
	boolean modifiesRedstoneInput(ItemStack var1, IUpgradableBlock var2);

	int getRedstoneInput(ItemStack var1, IUpgradableBlock var2, int var3);
}
