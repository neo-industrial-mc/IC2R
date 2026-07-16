package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.block.machine.container.ContainerCropHarvester;
import me.halfcooler.ic2r.core.crop.TileEntityCrop;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityCropHarvester extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock
{
	public final InvSlot contentSlot;
	public final InvSlotUpgrade upgradeSlot;
	public int scanX = -4;
	public int scanY = -1;
	public int scanZ = -4;

	public TileEntityCropHarvester(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.CROP_HARVESTER, pos, state, 10000, 1, false);
		this.syncElectricalProfile(21);
		this.contentSlot = new InvSlot(this, "content", InvSlot.Access.IO, 15);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.upgradeSlot.tick();
		if (this.level.getGameTime() % 10L == 0L && this.energy.getEnergy() >= 21.0)
		{
			this.scan();
		}
	}

	public void scan()
	{
		this.scanX++;
		if (this.scanX > 4)
		{
			this.scanX = -4;
			this.scanZ++;
			if (this.scanZ > 4)
			{
				this.scanZ = -4;
				this.scanY++;
				if (this.scanY > 1)
				{
					this.scanY = -1;
				}
			}
		}

		this.energy.useEnergy(1.0);
		Level world = this.getLevel();
		BlockEntity tileEntity = world.getBlockEntity(this.worldPosition.offset(this.scanX, this.scanY, this.scanZ));
		if (tileEntity instanceof TileEntityCrop crop && !this.isInvFull())
		{
			if (crop.getCrop() != null)
			{
				List<ItemStack> drops = null;
				if (crop.getCurrentAge() == crop.getCrop().getOptimalHarvestAge(crop))
				{
					drops = crop.performHarvest();
				} else if (crop.getCurrentAge() == crop.getCrop().getMaxAge())
				{
					drops = crop.performHarvest();
				}

				if (drops != null)
				{
					drops.forEach(drop ->
					{
						if (StackUtil.putInInventory(this, Direction.WEST, drop, true) == 0)
						{
							StackUtil.dropAsEntity(world, this.worldPosition, drop);
						} else
						{
							StackUtil.putInInventory(this, Direction.WEST, drop, false);
						}

						this.energy.useEnergy(20.0);
					});
				}
			}
		}
	}

	private boolean isInvFull()
	{
		for (int i = 0; i < this.contentSlot.size(); i++)
		{
			ItemStack stack = this.contentSlot.get(i);
			if (StackUtil.isEmpty(stack) || StackUtil.getSize(stack) < Math.min(stack.getMaxStackSize(), this.contentSlot.getStackSizeLimit()))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemProducing);
	}

	@Override
	public ContainerBase<TileEntityCropHarvester> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerCropHarvester(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerCropHarvester(syncId, inventory, this);
	}
}
