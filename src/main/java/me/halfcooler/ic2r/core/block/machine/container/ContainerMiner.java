package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMiner;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerMiner extends ContainerElectricMachine<TileEntityMiner>
{
	public ContainerMiner(int syncId, Inventory playerInventory, TileEntityMiner be)
	{
		super(Ic2rScreenHandlers.MINER, syncId, playerInventory, be, 166, 152, 58);
		this.addSlot(new SlotInvSlot(be.scannerSlot, 0, 8, 58));
		this.addSlot(new SlotInvSlot(be.pipeSlot, 0, 8, 40));
		this.addSlot(new SlotInvSlot(be.drillSlot, 0, 8, 22));
		this.addSlot(new SlotInvSlot(be.upgradeSlot, 0, 152, 22));

		for (int y = 0; y < be.buffer.size() / 5; y++)
		{
			for (int x = 0; x < 5; x++)
			{
				this.addSlot(new SlotInvSlot(be.buffer, x + y * 5, 44 + x * 18, 22 + y * 18));
			}
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("progress");
		ret.add("pumpMode");
		return ret;
	}
}
