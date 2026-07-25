package me.halfcooler.ic2r.api.upgrade;

import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Set;

public interface IUpgradeItem
{
	boolean isSuitableFor(ItemStack var1, Set<UpgradableProperty> var2);

	boolean onTick(ItemStack var1, IUpgradableBlock var2);

	Collection<ItemStack> onProcessEnd(ItemStack var1, IUpgradableBlock var2, Collection<ItemStack> var3);
}
