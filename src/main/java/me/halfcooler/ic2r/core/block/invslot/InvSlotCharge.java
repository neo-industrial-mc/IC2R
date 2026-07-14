package me.halfcooler.ic2r.core.block.invslot;

import me.halfcooler.ic2r.api.energy.tile.IChargingSlot;
import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.core.block.IInventorySlotHolder;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;

public class InvSlotCharge extends InvSlot implements IChargingSlot
{
	public int tier;

	public InvSlotCharge(IInventorySlotHolder<?> base1, int tier)
	{
		super(base1, "charge", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP);
		this.tier = tier;
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		return ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, this.tier, true, true) > 0.0;
	}

	@Override
	public double charge(double amount)
	{
		if (amount <= 0.0)
		{
			throw new IllegalArgumentException("Amount must be > 0.");
		}

		ItemStack stack = this.get(0);
		return StackUtil.isEmpty(stack) ? 0.0 : ElectricItem.manager.charge(stack, amount, this.tier, false, false);
	}

	public void setTier(int tier1)
	{
		this.tier = tier1;
	}
}
