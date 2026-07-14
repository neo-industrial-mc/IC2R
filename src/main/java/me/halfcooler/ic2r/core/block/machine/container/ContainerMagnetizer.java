package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMagnetizer;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotArmor;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerMagnetizer extends ContainerElectricMachine<TileEntityMagnetizer>
{
	public ContainerMagnetizer(int syncId, Inventory playerInventory, TileEntityMagnetizer be)
	{
		super(Ic2rScreenHandlers.MAGNETIZER, syncId, playerInventory, be, 166, 8, 44);

		for (int i = 0; i < 4; i++)
		{
			this.addSlot(new SlotInvSlot(be.upgradeSlot, i, 152, 8 + i * 18));
		}

		this.addSlot(new SlotArmor(playerInventory, EquipmentSlot.FEET, 45, 26));
	}
}
