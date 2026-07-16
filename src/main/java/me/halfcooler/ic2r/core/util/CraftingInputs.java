package me.halfcooler.ic2r.core.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

/**
 * Helpers for converting legacy {@link CraftingContainer} views into 1.21 {@link CraftingInput}.
 */
public final class CraftingInputs
{
	private CraftingInputs()
	{
	}

	public static CraftingInput of(CraftingContainer container)
	{
		int size = container.getContainerSize();
		int width = container.getWidth();
		int height = container.getHeight();
		List<ItemStack> items = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
		{
			items.add(container.getItem(i));
		}
		return CraftingInput.of(width, height, items);
	}

	public static CraftingInput of(int width, int height, ItemStack[] grid)
	{
		List<ItemStack> items = new ArrayList<>(width * height);
		for (int i = 0; i < width * height; i++)
		{
			items.add(i < grid.length && grid[i] != null ? grid[i] : ItemStack.EMPTY);
		}
		return CraftingInput.of(width, height, items);
	}
}
