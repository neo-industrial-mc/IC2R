package me.halfcooler.ic2r.api.info;

import net.minecraft.world.item.ItemStack;

public interface IInfoProvider
{
	double getEnergyValue(ItemStack var1);

	int getFuelValue(ItemStack var1, boolean var2);
}
