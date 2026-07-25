package me.halfcooler.ic2r.api.recipe;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IListRecipeManager extends Iterable<IRecipeInput>
{
	void add(IRecipeInput var1);

	boolean contains(ItemStack var1);

	boolean isEmpty();

	List<IRecipeInput> getInputs();
}
