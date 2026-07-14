package me.halfcooler.ic2r.core.slot;

import me.halfcooler.ic2r.api.item.ItemWrapper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotBoxable extends Slot
{
	public SlotBoxable(Container iinventory, int i, int j, int k)
	{
		super(iinventory, i, j, k);
	}

	public boolean mayPlace(@NotNull ItemStack itemstack)
	{
		return itemstack == null ? false : ItemWrapper.canBeStoredInToolbox(itemstack);
	}
}
