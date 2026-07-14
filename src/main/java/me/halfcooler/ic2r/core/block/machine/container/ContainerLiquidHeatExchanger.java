package me.halfcooler.ic2r.core.block.machine.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerLiquidHeatExchanger extends ContainerFullInv<TileEntityLiquidHeatExchanger>
{
	public ContainerLiquidHeatExchanger(int syncId, Inventory playerInventory, TileEntityLiquidHeatExchanger be)
	{
		super(Ic2rScreenHandlers.LIQUID_HEAT_EXCHANGER, syncId, playerInventory, be, 204);
		this.addSlot(new SlotInvSlot(be.hotfluidinputSlot, 0, 8, 103));
		this.addSlot(new SlotInvSlot(be.cooloutputSlot, 0, 152, 103));
		this.addSlot(new SlotInvSlot(be.coolfluidinputSlot, 0, 134, 103));
		this.addSlot(new SlotInvSlot(be.hotoutputSlot, 0, 26, 103));

		for (int i = 0; i < 3; i++)
		{
			this.addSlot(new SlotInvSlot(be.upgradeSlot, i, 62 + i * 18, 103));
		}

		for (int i = 0; i < 5; i++)
		{
			this.addSlot(new SlotInvSlot(be.heatexchangerslots, i, 46 + i * 17, 50));
		}

		for (int i = 5; i < 10; i++)
		{
			this.addSlot(new SlotInvSlot(be.heatexchangerslots, i, 46 + (i - 5) * 17, 72));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("inputTank");
		ret.add("outputTank");
		ret.add("transmitHeat");
		ret.add("maxHeatEmitpeerTick");
		return ret;
	}
}
