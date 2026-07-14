package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityPatternStorage;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerPatternStorage extends ContainerFullInv<TileEntityPatternStorage>
{
	public ContainerPatternStorage(int syncId, Inventory playerInventory, TileEntityPatternStorage be)
	{
		super(Ic2rScreenHandlers.PATTERN_STORAGE, syncId, playerInventory, be, 166);
		this.addSlot(new SlotInvSlot(be.diskSlot, 0, 18, 20));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("index");
		ret.add("maxIndex");
		ret.add("pattern");
		ret.add("patternUu");
		ret.add("patternEu");
		return ret;
	}
}
