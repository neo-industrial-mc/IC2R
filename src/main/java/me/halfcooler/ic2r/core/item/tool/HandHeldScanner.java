package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HandHeldScanner extends HandHeldInventory
{
	ItemStack itemScanner;
	Player player;

	public HandHeldScanner(Player player, InteractionHand hand, ItemStack itemScanner)
	{
		super(player, hand, itemScanner, 0);
		this.itemScanner = itemScanner;
		this.player = player;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerToolScanner(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerToolScanner(syncId, inventory, this);
	}
}
