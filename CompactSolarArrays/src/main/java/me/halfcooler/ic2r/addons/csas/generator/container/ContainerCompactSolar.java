package me.halfcooler.ic2r.addons.csas.generator.container;

import ic2.core.ContainerFullInv;
import ic2.core.slot.SlotInvSlot;
import me.halfcooler.ic2r.addons.csas.blockentity.TileEntityCompactSolar;
import me.halfcooler.ic2r.addons.csas.init.CsasScreenHandlers;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ContainerCompactSolar extends ContainerFullInv<TileEntityCompactSolar>
{
	public ContainerCompactSolar(int syncId, Inventory playerInventory, TileEntityCompactSolar base)
	{
		super(CsasScreenHandlers.COMPACT_SOLAR.get(), syncId, playerInventory, base, 166);
		this.addSlot(new SlotInvSlot(base.chargeSlot, 0, 80, 26));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> fields = super.getNetworkedFields();
		fields.add("sunlight");
		return fields;
	}
}