package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityCropmatron;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerCropmatron extends ContainerElectricMachine<TileEntityCropmatron>
{
	public ContainerCropmatron(int syncId, Inventory playerInventory, TileEntityCropmatron base)
	{
		super(Ic2rScreenHandlers.CROPMATRON, syncId, playerInventory, base, 192, 134, 80);

		for (int i = 0; i < base.fertilizerSlot.size(); i++)
		{
			this.addSlot(new SlotInvSlot(base.fertilizerSlot, i, 8 + i * 18, 80));
		}

		this.addSlot(new SlotInvSlot(base.exInputSlot, 0, 49, 27));
		this.addSlot(new SlotInvSlot(base.exOutputSlot, 0, 67, 27));
		this.addSlot(new SlotInvSlot(base.wasserinputSlot, 0, 57, 56));
		this.addSlot(new SlotInvSlot(base.wasseroutputSlot, 0, 75, 56));

		for (int i = 0; i < 4; i++)
		{
			this.addSlot(new SlotInvSlot(base.upgradeSlot, i, 152, 26 + i * 18));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("waterTank");
		ret.add("exTank");
		return ret;
	}
}
