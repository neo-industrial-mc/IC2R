package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityWeightedItemDistributor;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerWeightedItemDistributor extends ContainerFullInv<TileEntityWeightedItemDistributor>
{
	public static final short HEIGHT = 211;

	public ContainerWeightedItemDistributor(int syncId, Inventory playerInventory, TileEntityWeightedItemDistributor te)
	{
		super(Ic2rScreenHandlers.WEIGHTED_ITEM_DISTRIBUTOR, syncId, playerInventory, te, 211);

		for (int i = 0; i < te.buffer.size(); i++)
		{
			this.addSlot(new SlotInvSlot(te.buffer, i, 8 + i * 18, 108));
		}
	}
}
