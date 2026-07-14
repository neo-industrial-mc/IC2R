package me.halfcooler.ic2r.api.item;

import net.minecraft.world.item.ItemStack;

public interface IBackupElectricItemManager extends IElectricItemManager
{
	boolean handles(ItemStack var1);
}
