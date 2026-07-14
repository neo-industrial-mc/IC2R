package me.halfcooler.ic2r.core.block.invslot;

import me.halfcooler.ic2r.core.block.IInventorySlotHolder;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;

public class InvSlotConsumableLinked extends InvSlotConsumable
{
	public final InvSlot linkedSlot;

	public InvSlotConsumableLinked(IInventorySlotHolder<?> base1, String name1, int count, InvSlot linkedSlot1)
	{
		super(base1, name1, count);
		this.linkedSlot = linkedSlot1;
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		ItemStack required = this.linkedSlot.get();
		return StackUtil.isEmpty(required) ? false : StackUtil.checkItemEqualityStrict(required, stack);
	}

	public ItemStack consumeLinked(boolean simulate)
	{
		ItemStack required = this.linkedSlot.get();
		if (StackUtil.isEmpty(required))
		{
			return null;
		}

		int reqAmount = StackUtil.getSize(required);
		ItemStack available = this.consume(reqAmount, true, true);
		return !StackUtil.isEmpty(available) && StackUtil.getSize(available) == reqAmount ? this.consume(reqAmount, simulate, true) : null;
	}
}
