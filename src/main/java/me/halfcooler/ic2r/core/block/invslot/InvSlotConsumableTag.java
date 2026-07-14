package me.halfcooler.ic2r.core.block.invslot;

import me.halfcooler.ic2r.core.block.IInventorySlotHolder;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InvSlotConsumableTag extends InvSlotConsumable
{
	protected final TagKey<Item> tag;

	public InvSlotConsumableTag(IInventorySlotHolder<?> base, String name, int count, TagKey<Item> tag)
	{
		super(base, name, count);
		this.tag = tag;
	}

	public InvSlotConsumableTag(IInventorySlotHolder<?> base, String name, InvSlot.Access access, int count, InvSlot.InvSide side, TagKey<Item> tag)
	{
		super(base, name, access, count, side);
		this.tag = tag;
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		return StackUtil.isEmpty(stack) ? false : stack.is(this.tag);
	}
}
