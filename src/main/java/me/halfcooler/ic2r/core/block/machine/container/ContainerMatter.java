package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMatter;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerMatter extends ContainerFullInv<TileEntityMatter>
{
	public ContainerMatter(int syncId, Inventory playerInventory, TileEntityMatter be)
	{
		super(Ic2rScreenHandlers.MATTER_GENERATOR, syncId, playerInventory, be, 166);
		this.addSlot(new SlotInvSlot(be.amplifierSlot, 0, 72, 40));
		this.addSlot(new SlotInvSlot(be.outputSlot, 0, 125, 59));
		this.addSlot(new SlotInvSlot(be.containerSlot, 0, 125, 23));

		for (int i = 0; i < 4; i++)
		{
			this.addSlot(new SlotInvSlot(be.upgradeSlot, i, 152, 8 + i * 18));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("energy");
		ret.add("scrap");
		ret.add("fluidTank");
		return ret;
	}
}
