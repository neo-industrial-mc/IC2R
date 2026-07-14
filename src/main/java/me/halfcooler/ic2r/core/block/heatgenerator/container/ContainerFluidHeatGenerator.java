package me.halfcooler.ic2r.core.block.heatgenerator.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerFluidHeatGenerator extends ContainerFullInv<TileEntityFluidHeatGenerator>
{
	public ContainerFluidHeatGenerator(int syncId, Inventory playerInventory, TileEntityFluidHeatGenerator be)
	{
		super(Ic2rScreenHandlers.FLUID_HEAT_GENERATOR, syncId, playerInventory, be, 166);
		this.addSlot(new SlotInvSlot(be.fluidSlot, 0, 27, 21));
		this.addSlot(new SlotInvSlot(be.outputSlot, 0, 27, 54));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("fluidTank");
		ret.add("transmitHeat");
		ret.add("maxHeatEmitpeerTick");
		return ret;
	}
}
