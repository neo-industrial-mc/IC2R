package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityCanner;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerCanner extends ContainerStandardMachine<TileEntityCanner>
{
	public ContainerCanner(int syncId, Inventory playerInventory, TileEntityCanner canner)
	{
		super(Ic2rScreenHandlers.CANNER, syncId, playerInventory, canner, 184, 8, 80, 80, 44, 119, 17, 152, 26);
		this.addSlot(new SlotInvSlot(canner.canInputSlot, 0, 41, 17));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("mode");
		ret.add("inputTank");
		ret.add("outputTank");
		return ret;
	}
}
