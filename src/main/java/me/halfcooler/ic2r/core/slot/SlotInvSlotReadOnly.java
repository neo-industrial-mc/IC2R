package me.halfcooler.ic2r.core.slot;

import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotInvSlotReadOnly extends SlotInvSlot
{
	public SlotInvSlotReadOnly(InvSlot invSlot, int index, int xDisplayPosition, int yDisplayPosition)
	{
		super(invSlot, index, xDisplayPosition, yDisplayPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack)
	{
		return false;
	}

	@Override
	public void onTake(Player player, ItemStack stack)
	{
	}

	public boolean mayPickup(@NotNull Player player)
	{
		return false;
	}

	@Override
	public @NotNull ItemStack remove(int par1)
	{
		return StackUtil.emptyStack;
	}
}
