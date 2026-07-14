package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityChunkLoader;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerChunkLoader extends ContainerFullInv<TileEntityChunkLoader>
{
	public ContainerChunkLoader(int syncId, Inventory playerInventory, TileEntityChunkLoader be)
	{
		super(Ic2rScreenHandlers.CHUNK_LOADER, syncId, playerInventory, be, 250);
		this.addSlot(new SlotInvSlot(be.dischargeSlot, 0, 8, 143));

		for (int i = 0; i < be.upgradeSlot.size(); i++)
		{
			this.addSlot(new SlotInvSlot(be.upgradeSlot, i, 8, 44 + 18 * i));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("loadedChunks");
		return ret;
	}
}
