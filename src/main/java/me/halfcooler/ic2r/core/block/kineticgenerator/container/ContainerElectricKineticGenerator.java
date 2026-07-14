package me.halfcooler.ic2r.core.block.kineticgenerator.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerElectricKineticGenerator extends ContainerFullInv<TileEntityElectricKineticGenerator>
{
	public ContainerElectricKineticGenerator(int syncId, Inventory playerInventory, TileEntityElectricKineticGenerator be)
	{
		super(Ic2rScreenHandlers.ELECTRIC_KINETIC_GENERATOR, syncId, playerInventory, be, 166);

		for (int i = 0; i < 5; i++)
		{
			this.addSlot(new SlotInvSlot(be.slotMotor, i, 44 + i * 18, 27));
		}

		for (int i = 5; i < 10; i++)
		{
			this.addSlot(new SlotInvSlot(be.slotMotor, i, 44 + (i - 5) * 18, 45));
		}

		this.addSlot(new SlotInvSlot(be.dischargeSlot, 0, 8, 62));
	}
}
