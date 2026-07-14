package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityCondenser;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerCondenser extends ContainerElectricMachine<TileEntityCondenser>
{
	public ContainerCondenser(int syncId, Inventory playerInventory, TileEntityCondenser te)
	{
		super(Ic2rScreenHandlers.CONDENSER, syncId, playerInventory, te, 184, 8, 44);
		this.addSlot(new SlotInvSlot(te.waterInputSlot, 0, 26, 73));
		this.addSlot(new SlotInvSlot(te.waterOutputSlot, 0, 134, 73));
		this.addSlot(new SlotInvSlot(te.upgradeSlot, 0, 152, 73));

		for (int i = 0; i < 2; i++)
		{
			this.addSlot(new SlotInvSlot(te.ventSlots, i, 26 + i * 108, 26));
			this.addSlot(new SlotInvSlot(te.ventSlots, i + 2, 26 + i * 108, 44));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("inputTank");
		ret.add("outputTank");
		ret.add("progress");
		return ret;
	}
}
