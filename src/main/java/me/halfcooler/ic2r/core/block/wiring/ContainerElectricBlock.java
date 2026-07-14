package me.halfcooler.ic2r.core.block.wiring;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityElectricBlock;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.ArmorSlot;
import me.halfcooler.ic2r.core.slot.SlotArmor;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerElectricBlock extends ContainerFullInv<TileEntityElectricBlock>
{
	public ContainerElectricBlock(int syncId, Inventory playerInventory, TileEntityElectricBlock tileEntity1)
	{
		super(Ic2rScreenHandlers.ENERGY_STORAGE, syncId, playerInventory, tileEntity1, 196);

		for (int col = 0; col < ArmorSlot.getCount(); col++)
		{
			this.addSlot(new SlotArmor(playerInventory, ArmorSlot.get(col), 8 + col * 18, 84));
		}

		this.addSlot(new SlotInvSlot(tileEntity1.chargeSlot, 0, 56, 17));
		this.addSlot(new SlotInvSlot(tileEntity1.dischargeSlot, 0, 56, 53));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("redstoneMode");
		return ret;
	}
}
