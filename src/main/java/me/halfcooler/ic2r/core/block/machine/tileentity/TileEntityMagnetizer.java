package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Redstone;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.block.machine.container.ContainerMagnetizer;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityMagnetizer extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock
{
	public static final int defaultMaxEnergy = 100;
	public static final int defaultTier = 1;
	private static final double boostEnergy = 2.0;
	protected final Redstone redstone = this.addComponent(new Redstone(this));
	public InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);

	public TileEntityMagnetizer(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.MAGNETIZER, pos, state, 100, 1);
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		Level world = this.getLevel();
		if (world != null && !world.isClientSide)
		{
			this.setOverclockRates();
		}
	}

	public void setOverclockRates()
	{
		this.upgradeSlot.onChanged();
		int tier = this.upgradeSlot.getTier(1);
		this.energy.setSinkTier(tier);
		this.dischargeSlot.setTier(tier);
		this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(100, 0, 0));
		this.syncElectricalProfile(0);
	}

	private int distance()
	{
		return 20 + this.upgradeSlot.augmentation;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerMagnetizer(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerMagnetizer(syncId, inventory, this);
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

	public boolean canBoost()
	{
		return this.energy.getEnergy() >= 2.0;
	}

	public void boost(double multiplier)
	{
		this.energy.useEnergy(2.0 * multiplier);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Augmentable, UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage);
	}
}
