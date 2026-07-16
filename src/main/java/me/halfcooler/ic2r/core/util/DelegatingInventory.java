package me.halfcooler.ic2r.core.util;

import java.util.Set;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DelegatingInventory implements Container
{
	private final Container parent;

	public DelegatingInventory(Container parent)
	{
		this.parent = parent;
	}

	public void clearContent()
	{
		this.parent.clearContent();
	}

	public int getContainerSize()
	{
		return this.parent.getContainerSize();
	}

	public boolean isEmpty()
	{
		return this.parent.isEmpty();
	}

	public @NotNull ItemStack getItem(int slot)
	{
		return this.parent.getItem(slot);
	}

	public @NotNull ItemStack removeItem(int slot, int amount)
	{
		return this.parent.removeItem(slot, amount);
	}

	public @NotNull ItemStack removeItemNoUpdate(int slot)
	{
		return this.parent.removeItemNoUpdate(slot);
	}

	public void setItem(int slot, @NotNull ItemStack stack)
	{
		this.parent.setItem(slot, stack);
	}

	public int getMaxStackSize()
	{
		return this.parent.getMaxStackSize();
	}

	public void setChanged()
	{
		this.parent.setChanged();
	}

	public boolean stillValid(@NotNull Player player)
	{
		return this.parent.stillValid(player);
	}

	public void startOpen(@NotNull Player player)
	{
		this.parent.startOpen(player);
	}

	public void stopOpen(@NotNull Player player)
	{
		this.parent.stopOpen(player);
	}

	public boolean canPlaceItem(int slot, @NotNull ItemStack stack)
	{
		return this.parent.canPlaceItem(slot, stack);
	}

	public int countItem(@NotNull Item item)
	{
		return this.parent.countItem(item);
	}

	public boolean hasAnyOf(@NotNull Set<Item> items)
	{
		return this.parent.hasAnyOf(items);
	}
}
