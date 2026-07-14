package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityWeightedFluidDistributor;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerWeightedFluidDistributor extends ContainerFullInv<TileEntityWeightedFluidDistributor>
{
	public static final short HEIGHT = 211;

	public ContainerWeightedFluidDistributor(int syncId, Inventory playerInventory, TileEntityWeightedFluidDistributor te)
	{
		super(Ic2rScreenHandlers.WEIGHTED_FLUID_DISTRIBUTOR, syncId, playerInventory, te, 211);
		this.addSlot(new SlotInvSlot(te.inputSlot, 0, 8, 108));
		this.addSlot(new SlotInvSlot(te.OutputSlot, 0, 152, 108));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("fluidTank");
		return ret;
	}
}
