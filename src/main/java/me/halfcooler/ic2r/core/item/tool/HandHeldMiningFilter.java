package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HandHeldMiningFilter extends HandHeldInventory
{
	public boolean blacklist = true;

	public HandHeldMiningFilter(Player player, InteractionHand hand, ItemStack containerStack)
	{
		super(player, hand, containerStack, 45);
		CompoundTag nbt = StackUtil.getOrCreateNbtData(containerStack);
		if (nbt.contains("blacklist"))
		{
			this.blacklist = nbt.getBoolean("blacklist");
		}
	}

	@Override
	protected void save()
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(this.containerStack);
		nbt.putBoolean("blacklist", this.blacklist);
		super.save();
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerMiningFilter(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerMiningFilter(syncId, inventory, this);
	}
}
