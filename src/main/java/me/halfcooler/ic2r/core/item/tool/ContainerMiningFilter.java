package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.network.ClientModifiable;
import me.halfcooler.ic2r.core.item.ContainerHandHeldInventory;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotHologramSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ContainerMiningFilter extends ContainerHandHeldInventory<HandHeldMiningFilter>
{
	@ClientModifiable
	protected boolean blacklist;

	public ContainerMiningFilter(int syncId, Inventory playerInventory, HandHeldMiningFilter base)
	{
		super(Ic2rScreenHandlers.MINING_FILTER, syncId, base);
		this.blacklist = base.blacklist;

		for (int row = 0; row < 5; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				int idx = row * 9 + col;
				this.addSlot(new SlotHologramSlot(
					base.inventory, idx,
					8 + col * 18, 32 + row * 18,
					1, base.makeSaveCallback()
				));
			}
		}

		this.addPlayerInventorySlots(playerInventory, 215);
	}

	@Override
	public void removed(Player player)
	{
		this.base.blacklist = this.blacklist;
		super.removed(player);
	}
}
