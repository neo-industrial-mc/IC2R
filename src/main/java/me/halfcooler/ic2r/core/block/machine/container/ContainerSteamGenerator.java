package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntitySteamGenerator;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerSteamGenerator extends ContainerBase<TileEntitySteamGenerator>
{
	public ContainerSteamGenerator(int syncId, Inventory playerInventory, TileEntitySteamGenerator te)
	{
		super(Ic2rScreenHandlers.STEAM_GENERATOR, syncId, playerInventory, te);
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("waterTank");
		ret.add("heatInput");
		ret.add("inputMB");
		ret.add("outputMB");
		ret.add("pressure");
		ret.add("systemHeat");
		ret.add("outputFluid");
		ret.add("calcification");
		return ret;
	}
}
