package me.halfcooler.ic2r.core.block.kineticgenerator.container;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerStirlingKineticGenerator extends ContainerFullInv<TileEntityStirlingKineticGenerator>
{
	public ContainerStirlingKineticGenerator(int syncId, Inventory playerInventory, TileEntityStirlingKineticGenerator te)
	{
		super(Ic2rScreenHandlers.STIRLING_KINETIC_GENERATOR, syncId, playerInventory, te, 204);
		this.addSlot(new SlotInvSlot(te.coolFluidInputSlot, 0, 8, 103));
		this.addSlot(new SlotInvSlot(te.coolOutputSlot, 0, 26, 103));
		this.addSlot(new SlotInvSlot(te.hotFluidInputSlot, 0, 134, 103));
		this.addSlot(new SlotInvSlot(te.hotOutputSlot, 0, 152, 103));

		for (int i = 0; i < 3; i++)
		{
			this.addSlot(new SlotInvSlot(te.upgradeSlot, i, 62 + i * 18, 103));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("inputTank");
		ret.add("outputTank");
		return ret;
	}
}
