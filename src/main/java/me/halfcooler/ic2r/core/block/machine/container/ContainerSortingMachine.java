package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntitySortingMachine;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotHologramSlot;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;
import me.halfcooler.ic2r.core.util.Util;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ContainerSortingMachine extends ContainerElectricMachine<TileEntitySortingMachine>
{
	public ContainerSortingMachine(int syncId, Inventory playerInventory, TileEntitySortingMachine be)
	{
		super(Ic2rScreenHandlers.SORTING_MACHINE, syncId, playerInventory, be, 243, 188, 219);

		for (int i = 0; i < 3; i++)
		{
			this.addSlot(new SlotInvSlot(be.upgradeSlot, i, 188, 161 + i * 18));
		}

		for (int i = 0; i < 11; i++)
		{
			this.addSlot(new SlotInvSlot(be.buffer, i, 8 + i * 18, 141));
		}

		for (int i = 0; i < Util.ALL_DIRS.length; i++)
		{
			Direction dir = Util.ALL_DIRS[i];
			ItemStack[] filterSlots = be.getFilterSlots(dir);

			for (int j = 0; j < filterSlots.length; j++)
			{
				this.addSlot(new SlotHologramSlot(filterSlots, j, 80 + j * 18, 19 + i * 20, 64, null));
			}
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("defaultRoute");
		return ret;
	}
}
