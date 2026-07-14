package me.halfcooler.ic2r.core.block.kineticgenerator.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerWaterKineticGenerator extends ContainerFullInv<TileEntityWaterKineticGenerator>
{
	public ContainerWaterKineticGenerator(int syncId, Inventory playerInventory, TileEntityWaterKineticGenerator be)
	{
		super(Ic2rScreenHandlers.WATER_KINETIC_GENERATOR, syncId, playerInventory, be, 166);
		this.addSlot(new SlotInvSlot(be.rotorSlot, 0, 80, 26));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("waterFlow");
		ret.add("type");
		return ret;
	}
}
