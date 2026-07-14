package me.halfcooler.ic2r.core.block.heatgenerator.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerElectricHeatGenerator extends ContainerFullInv<TileEntityElectricHeatGenerator>
{
	public ContainerElectricHeatGenerator(int syncId, Inventory playerInventory, TileEntityElectricHeatGenerator be)
	{
		super(Ic2rScreenHandlers.ELECTRIC_HEAT_GENERATOR, syncId, playerInventory, be, 166);

		for (int i = 0; i < 5; i++)
		{
			this.addSlot(new SlotInvSlot(be.coilSlot, i, 44 + i * 18, 27));
		}

		for (int i = 5; i < 10; i++)
		{
			this.addSlot(new SlotInvSlot(be.coilSlot, i, 44 + (i - 5) * 18, 45));
		}

		this.addSlot(new SlotInvSlot(be.dischargeSlot, 0, 8, 62));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("transmitHeat");
		ret.add("maxHeatEmitpeerTick");
		return ret;
	}
}
