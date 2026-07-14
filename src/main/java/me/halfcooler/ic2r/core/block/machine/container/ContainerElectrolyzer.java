package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityElectrolyzer;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerElectrolyzer extends ContainerElectricMachine<TileEntityElectrolyzer>
{
	public ContainerElectrolyzer(int syncId, Inventory playerInventory, TileEntityElectrolyzer tileEntity)
	{
		super(Ic2rScreenHandlers.ELECTROLYZER, syncId, playerInventory, tileEntity, 166, 8, 62);

		for (int i = 0; i < 4; i++)
		{
			this.addSlot(new SlotInvSlot(tileEntity.upgradeSlot, i, 152, 8 + i * 18));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("progress");
		ret.add("recipe");
		ret.add("input");
		return ret;
	}
}
