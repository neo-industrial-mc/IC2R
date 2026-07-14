package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.IUpgradeItem;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.block.machine.container.ContainerItemBuffer;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityItemBuffer extends TileEntityInventory implements IHasGui, IUpgradableBlock
{
	public final InvSlot rightcontentSlot;
	public final InvSlot leftcontentSlot;
	public final InvSlotUpgrade upgradeSlot;
	private boolean tick = true;

	public TileEntityItemBuffer(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.ITEM_BUFFER, pos, state);
		this.rightcontentSlot = new InvSlot(this, "rightcontent", InvSlot.Access.IO, 24, InvSlot.InvSide.SIDE);
		this.leftcontentSlot = new InvSlot(this, "leftcontent", InvSlot.Access.IO, 24, InvSlot.InvSide.NOTSIDE);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);
		this.comparator.setUpdate(() -> calcRedstoneFromInvSlots(this.rightcontentSlot, this.leftcontentSlot));
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		ItemStack upgradeleft = this.upgradeSlot.get(0);
		ItemStack upgraderight = this.upgradeSlot.get(1);
		if (!StackUtil.isEmpty(upgradeleft) && !StackUtil.isEmpty(upgraderight))
		{
			if (this.tick)
			{
				if (((IUpgradeItem) upgradeleft.getItem()).onTick(upgradeleft, this))
				{
					super.setChanged();
				}
			} else if (((IUpgradeItem) upgraderight.getItem()).onTick(upgraderight, this))
			{
				super.setChanged();
			}

			this.tick = !this.tick;
		} else
		{
			if (!StackUtil.isEmpty(upgradeleft))
			{
				this.tick = true;
				if (((IUpgradeItem) upgradeleft.getItem()).onTick(upgradeleft, this))
				{
					super.setChanged();
				}
			}

			if (!StackUtil.isEmpty(upgraderight))
			{
				this.tick = false;
				if (((IUpgradeItem) upgraderight.getItem()).onTick(upgraderight, this))
				{
					super.setChanged();
				}
			}
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerItemBuffer(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerItemBuffer(syncId, inventory, this);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.ItemProducing);
	}

	@Override
	public double getEnergy()
	{
		return 40.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return true;
	}
}
