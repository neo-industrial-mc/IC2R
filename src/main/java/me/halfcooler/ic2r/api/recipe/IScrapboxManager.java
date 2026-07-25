package me.halfcooler.ic2r.api.recipe;

import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface IScrapboxManager extends IBasicMachineRecipeManager
{
	ItemStack getDrop(ItemStack var1, boolean var2);

	Map<ItemStack, Float> getDrops();
}
