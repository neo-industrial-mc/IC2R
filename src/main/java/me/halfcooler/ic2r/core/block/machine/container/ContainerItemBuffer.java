package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityItemBuffer;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerItemBuffer extends ContainerFullInv<TileEntityItemBuffer>
{
	public ContainerItemBuffer(int syncId, Inventory playerInventory, TileEntityItemBuffer te)
	{
		super(Ic2rScreenHandlers.ITEM_BUFFER, syncId, playerInventory, te, 232);

		for (int y = 0; y < te.leftcontentSlot.size() / 4; y++)
		{
			for (int x = 0; x < 4; x++)
			{
				this.addSlot(new SlotInvSlot(te.leftcontentSlot, x + y * 4, 8 + x * 18, 18 + y * 18));
			}
		}

		for (int y = 0; y < te.rightcontentSlot.size() / 4; y++)
		{
			for (int x = 0; x < 4; x++)
			{
				this.addSlot(new SlotInvSlot(te.rightcontentSlot, x + y * 4, 98 + x * 18, 18 + y * 18));
			}
		}

		for (int i = 0; i < 2; i++)
		{
			this.addSlot(new SlotInvSlot(te.upgradeSlot, i, 35 + i * 90, 128));
		}
	}
}
