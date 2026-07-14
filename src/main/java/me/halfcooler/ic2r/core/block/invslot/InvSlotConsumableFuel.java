package me.halfcooler.ic2r.core.block.invslot;

import me.halfcooler.ic2r.api.info.Info;
import me.halfcooler.ic2r.core.block.IInventorySlotHolder;
import net.minecraft.world.item.ItemStack;

public class InvSlotConsumableFuel extends InvSlotConsumable
{
	public final boolean allowLava;

	public InvSlotConsumableFuel(IInventorySlotHolder<?> base1, String name1, int count, boolean allowLava1)
	{
		super(base1, name1, InvSlot.Access.I, count, InvSlot.InvSide.SIDE);
		this.allowLava = allowLava1;
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		return Info.getItemInfo().getFuelValue(stack, this.allowLava) > 0;
	}

	public int consumeFuel()
	{
		ItemStack fuel = this.consume(1);
		return fuel == null ? 0 : Info.getItemInfo().getFuelValue(fuel, this.allowLava);
	}
}
