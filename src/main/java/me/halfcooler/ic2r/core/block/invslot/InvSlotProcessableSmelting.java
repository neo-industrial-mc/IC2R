package me.halfcooler.ic2r.core.block.invslot;

import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.core.block.IInventorySlotHolder;
import net.minecraft.world.item.ItemStack;

public class InvSlotProcessableSmelting extends InvSlotProcessable<ItemStack, ItemStack, ItemStack>
{
	public InvSlotProcessableSmelting(IInventorySlotHolder<?> base, String name, int count)
	{
		super(base, name, count, w -> Recipes.furnace);
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
