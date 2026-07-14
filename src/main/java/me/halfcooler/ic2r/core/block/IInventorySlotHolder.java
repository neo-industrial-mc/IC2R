package me.halfcooler.ic2r.core.block;

import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import net.minecraft.world.Container;

public interface IInventorySlotHolder<P extends Ic2rTileEntity & Container>
{
	P getParent();

	InvSlot getInventorySlot(String var1);

	void addInventorySlot(InvSlot var1);

	int getBaseIndex(InvSlot var1);
}
