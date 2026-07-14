package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityMetalFormer;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerMetalFormer extends ContainerStandardMachine<TileEntityMetalFormer>
{
	public ContainerMetalFormer(int syncId, Inventory playerInventory, TileEntityMetalFormer be)
	{
		super(Ic2rScreenHandlers.METAL_FORMER, syncId, playerInventory, be, 166, 17, 53, 17, 17, 116, 35, 152, 8);
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("mode");
		return ret;
	}
}
