package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityFluidBottler;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerFluidBottler extends ContainerStandardMachine<TileEntityFluidBottler>
{
	public ContainerFluidBottler(int syncId, Inventory playerInventory, TileEntityFluidBottler be)
	{
		super(Ic2rScreenHandlers.FLUID_BOTTLER, syncId, playerInventory, be, 184, 8, 53, 0, 0, 117, 53, 152, 26);
		this.addSlot(new SlotInvSlot(be.drainInputSlot, 0, 44, 35));
		this.addSlot(new SlotInvSlot(be.fillInputSlot, 0, 44, 72));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("fluidTank");
		return ret;
	}
}
