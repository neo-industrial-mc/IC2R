package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityFluidDistributor;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerFluidDistributor extends ContainerFullInv<TileEntityFluidDistributor>
{
	public ContainerFluidDistributor(int syncId, Inventory playerInventory, TileEntityFluidDistributor be)
	{
		super(Ic2rScreenHandlers.FLUID_DISTRIBUTOR, syncId, playerInventory, be, 184);
		this.addSlot(new SlotInvSlot(be.inputSlot, 0, 9, 54));
		this.addSlot(new SlotInvSlot(be.OutputSlot, 0, 9, 72));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("fluidTank");
		return ret;
	}
}
