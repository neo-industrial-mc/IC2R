package me.halfcooler.ic2r.core.block.personal;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerTradeOMatOpen extends ContainerFullInv<TileEntityTradeOMat>
{
	final boolean canToggleInfinite;

	public ContainerTradeOMatOpen(int syncId, Inventory playerInventory, TileEntityTradeOMat be, boolean canToggleInfinite)
	{
		super(Ic2rScreenHandlers.TRADE_O_MAT_OPEN, syncId, playerInventory, be, 166);
		this.canToggleInfinite = canToggleInfinite;
		this.addSlot(new SlotInvSlot(be.demandSlot, 0, 50, 19));
		this.addSlot(new SlotInvSlot(be.offerSlot, 0, 50, 53));
		this.addSlot(new SlotInvSlot(be.inputSlot, 0, 80, 19));
		this.addSlot(new SlotInvSlot(be.outputSlot, 0, 80, 53));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("stock");
		ret.add("totalTradeCount");
		return ret;
	}
}
