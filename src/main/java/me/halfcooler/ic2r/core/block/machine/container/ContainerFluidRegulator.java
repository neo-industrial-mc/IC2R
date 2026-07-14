package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityFluidRegulator;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerFluidRegulator extends ContainerElectricMachine<TileEntityFluidRegulator>
{
	public ContainerFluidRegulator(int syncId, Inventory playerInventory, TileEntityFluidRegulator be)
	{
		super(Ic2rScreenHandlers.FLUID_REGULATOR, syncId, playerInventory, be, 184, 8, 57);
		this.addSlot(new SlotInvSlot(be.wasserinputSlot, 0, 58, 53));
		this.addSlot(new SlotInvSlot(be.wasseroutputSlot, 0, 58, 71));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("fluidTank");
		ret.add("outputmb");
		ret.add("mode");
		return ret;
	}
}
