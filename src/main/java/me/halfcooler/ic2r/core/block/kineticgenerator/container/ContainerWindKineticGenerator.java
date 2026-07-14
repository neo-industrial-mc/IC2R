package me.halfcooler.ic2r.core.block.kineticgenerator.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerWindKineticGenerator extends ContainerFullInv<TileEntityWindKineticGenerator>
{
	public ContainerWindKineticGenerator(int syncId, Inventory playerInventory, TileEntityWindKineticGenerator be)
	{
		super(Ic2rScreenHandlers.WIND_KINETIC_GENERATOR, syncId, playerInventory, be, 166);
		this.addSlot(new SlotInvSlot(be.rotorSlot, 0, 80, 26));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("windStrength");
		return ret;
	}
}
