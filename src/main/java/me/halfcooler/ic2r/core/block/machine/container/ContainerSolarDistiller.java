package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntitySolarDistiller;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerSolarDistiller extends ContainerFullInv<TileEntitySolarDistiller>
{
	public ContainerSolarDistiller(int syncId, Inventory playerInventory, TileEntitySolarDistiller be)
	{
		super(Ic2rScreenHandlers.SOLAR_DISTILLER, syncId, playerInventory, be, 184);
		this.addSlot(new SlotInvSlot(be.waterinputSlot, 0, 17, 27));
		this.addSlot(new SlotInvSlot(be.destiwaterinputSlot, 0, 136, 64));
		this.addSlot(new SlotInvSlot(be.waterOutputSlot, 0, 17, 45));
		this.addSlot(new SlotInvSlot(be.destiwateroutputSlott, 0, 136, 82));

		for (int i = 0; i < 2; i++)
		{
			this.addSlot(new SlotInvSlot(be.upgradeSlot, i, 152, 8 + i * 18));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("skyLight");
		ret.add("inputTank");
		ret.add("outputTank");
		return ret;
	}
}
