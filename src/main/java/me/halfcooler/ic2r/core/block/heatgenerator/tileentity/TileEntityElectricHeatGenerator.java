package me.halfcooler.ic2r.core.block.heatgenerator.tileentity;

import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.heatgenerator.container.ContainerElectricHeatGenerator;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumable;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableItemStack;
import me.halfcooler.ic2r.core.block.invslot.InvSlotDischarge;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityHeatSourceInventory;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityElectricHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui
{
	public static final double outputMultiplier = IC2RConfig.balance.energy.heatGenerator.electric.get();
	public final InvSlotDischarge dischargeSlot;
	public final InvSlotConsumable coilSlot = new InvSlotConsumableItemStack(this, "CoilSlot", 10, new ItemStack(Ic2rItems.COIL));
	protected final Energy energy;
	private boolean newActive;

	public TileEntityElectricHeatGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.ELECTRIC_HEAT_GENERATOR, pos, state);
		this.coilSlot.setStackSizeLimit(1);
		this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, 4);
		this.energy = this.addComponent(Energy.asBasicSink(this, 10000.0, 4).addManagedSlot(this.dischargeSlot));
		this.newActive = false;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.getActive() != this.newActive)
		{
			this.setActive(this.newActive);
		}
	}

	@Override
	public ContainerBase<TileEntityElectricHeatGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerElectricHeatGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerElectricHeatGenerator(syncId, inventory, this);
	}

	@Override
	protected int fillHeatBuffer(int maxAmount)
	{
		int amount = Math.min(maxAmount, (int) (this.energy.getEnergy() / outputMultiplier));
		this.energy.syncConsumerProfile(amount > 0 ? (int) Math.ceil(amount / outputMultiplier) : 0);
		if (amount > 0)
		{
			this.energy.useEnergy(amount / outputMultiplier);
			this.newActive = true;
		} else
		{
			this.newActive = false;
		}

		return amount;
	}

	@Override
	public int getMaxHeatEmittedPerTick()
	{
		int counter = 0;

		for (int i = 0; i < this.coilSlot.size(); i++)
		{
			if (!this.coilSlot.isEmpty(i))
			{
				counter++;
			}
		}

		return counter * 10;
	}

	public final float getChargeLevel()
	{
		return (float) Math.min(1.0, this.energy.getFillRatio());
	}
}
