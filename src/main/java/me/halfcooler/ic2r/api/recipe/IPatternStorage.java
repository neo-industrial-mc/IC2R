package me.halfcooler.ic2r.api.recipe;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IPatternStorage
{
	boolean addPattern(ItemStack var1);

	List<ItemStack> getPatterns();
}
