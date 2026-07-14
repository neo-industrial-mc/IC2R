package me.halfcooler.ic2r.core.block.personal;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;
import me.halfcooler.ic2r.core.slot.SlotInvSlotReadOnly;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DataSlot;

public class ContainerEnergyOMatClosed extends ContainerFullInv<TileEntityEnergyOMat>
{
	public ContainerEnergyOMatClosed(int syncId, Inventory playerInventory, TileEntityEnergyOMat be)
	{
		super(Ic2rScreenHandlers.ENERGY_O_MAT_CLOSED, syncId, playerInventory, be, 166);
		this.addSlot(new SlotInvSlotReadOnly(be.demandSlot, 0, 50, 17));
		this.addSlot(new SlotInvSlot(be.inputSlot, 0, 143, 17));
		this.addSlot(new SlotInvSlot(be.chargeSlot, 0, 143, 53));
		this.addDataSlot(new DataSlot()
		{
			public int get()
			{
				return ContainerEnergyOMatClosed.this.base.chargeSlot.tier;
			}

			public void set(int value)
			{
				ContainerEnergyOMatClosed.this.base.chargeSlot.tier = value;
			}
		});
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("paidFor");
		ret.add("euOffer");
		return ret;
	}
}
