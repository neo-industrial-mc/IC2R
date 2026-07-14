package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.item.ItemWrapper;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HandHeldToolbox extends HandHeldInventory
{
	public HandHeldToolbox(Player player, InteractionHand hand, ItemStack containerStack, int inventorySize)
	{
		super(player, hand, containerStack, inventorySize);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack1)
	{
		return StackUtil.isEmpty(stack1) ? false : ItemWrapper.canBeStoredInToolbox(stack1);
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerToolbox(syncId, this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerToolbox(syncId, this);
	}
}
