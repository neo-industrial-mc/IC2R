package me.halfcooler.ic2r.core.block.generator.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntitySolarGenerator;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerSolarGenerator extends ContainerFullInv<TileEntitySolarGenerator>
{
	public ContainerSolarGenerator(int syncId, Inventory playerInventory, TileEntitySolarGenerator base)
	{
		super(Ic2rScreenHandlers.SOLAR_GENERATOR, syncId, playerInventory, base, 166);
		this.addSlot(new SlotInvSlot(base.chargeSlot, 0, 80, 26));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("sunlight");
		return ret;
	}
}
