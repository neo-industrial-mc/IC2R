package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.core.item.ContainerHandHeldInventory;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotRadioactive;

public class ContainerContainmentbox extends ContainerHandHeldInventory<HandHeldContainmentbox>
{
	protected static final int height = 166;

	public ContainerContainmentbox(int syncId, HandHeldContainmentbox box)
	{
		super(Ic2rScreenHandlers.CONTAINMENT_BOX, syncId, box);

		for (int i = 0; i < 4; i++)
		{
			this.addSlot(new SlotRadioactive(box, i, 53 + i * 18, 19));
		}

		for (int i = 4; i < 8; i++)
		{
			this.addSlot(new SlotRadioactive(box, i, 53 + (i - 4) * 18, 37));
		}

		for (int i = 8; i < 12; i++)
		{
			this.addSlot(new SlotRadioactive(box, i, 53 + (i - 8) * 18, 55));
		}

		this.addPlayerInventorySlots(box.player.getInventory(), height);
	}
}
