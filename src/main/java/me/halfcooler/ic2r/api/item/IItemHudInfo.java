package me.halfcooler.ic2r.api.item;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IItemHudInfo
{
	List<String> getHudInfo(ItemStack var1, boolean var2);
}
