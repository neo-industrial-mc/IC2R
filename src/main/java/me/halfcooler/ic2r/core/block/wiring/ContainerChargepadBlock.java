package me.halfcooler.ic2r.core.block.wiring;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityChargePadBlock;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerChargepadBlock extends ContainerFullInv<TileEntityChargePadBlock>
{
	public ContainerChargepadBlock(int syncId, Inventory playerInventory, TileEntityChargePadBlock be)
	{
		super(Ic2rScreenHandlers.CHARGEPAD, syncId, playerInventory, be, 161);
		this.addSlot(new SlotInvSlot(be.chargeSlot, 0, 56, 17));
		this.addSlot(new SlotInvSlot(be.dischargeSlot, 0, 56, 53));
	}

	/**
	 * Network field list still uses legacy camelCase name {@code redstoneMode} for TeUpdate packet
	 * compatibility. Value R/W on the TE goes through Sync (G1.5 / parent ElectricBlock).
	 */
	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add(TileEntityChargePadBlock.LEGACY_REDSTONE_MODE_FIELD);
		return ret;
	}
}
