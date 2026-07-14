package me.halfcooler.ic2r.core.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotRadioactive extends Slot
{
	public SlotRadioactive(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}

	public boolean mayPlace(@NotNull ItemStack stack)
	{
		return this.container.canPlaceItem(this.getContainerSlot(), stack);
	}
}
