package me.halfcooler.ic2r.core.block.kineticgenerator.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerSteamKineticGenerator extends ContainerFullInv<TileEntitySteamKineticGenerator>
{
	public ContainerSteamKineticGenerator(int syncId, Inventory playerInventory, TileEntitySteamKineticGenerator te)
	{
		super(Ic2rScreenHandlers.STEAM_KINETIC_GENERATOR, syncId, playerInventory, te, 166);
		this.addSlot(new SlotInvSlot(te.upgradeSlot, 0, 152, 26));
		this.addSlot(new SlotInvSlot(te.turbineSlot, 0, 80, 26));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("distilledWaterTank");
		ret.add("kuOutput");
		ret.add("ventingSteam");
		ret.add("throttled");
		ret.add("isTurbineFilledWithWater");
		return ret;
	}
}
