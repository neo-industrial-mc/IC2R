package me.halfcooler.ic2r.core.block.invslot;

import me.halfcooler.ic2r.api.recipe.IMachineRecipeManager;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.core.block.IInventorySlotHolder;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;

public class InvSlotProcessableGeneric extends InvSlotProcessable<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public InvSlotProcessableGeneric(
		IInventorySlotHolder<?> base,
		String name,
		int count,
		Recipes.IGetter<? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>> recipeManager
	)
	{
		super(base, name, count, recipeManager);
	}

	protected ItemStack getInput(ItemStack stack)
	{
		return stack;
	}

	protected void setInput(ItemStack input)
	{
		this.put(input);
	}
}
