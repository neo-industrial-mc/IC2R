package me.halfcooler.ic2r.core.block.wiring;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityTransformer;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerTransformer extends ContainerFullInv<TileEntityTransformer>
{
	public ContainerTransformer(int syncId, Inventory playerInventory, TileEntityTransformer tileEntity1)
	{
		super(Ic2rScreenHandlers.TRANSFORMER, syncId, playerInventory, tileEntity1, 219);
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("configuredMode");
		ret.add("transformMode");
		ret.add("inputFlow");
		ret.add("outputFlow");
		return ret;
	}
}
