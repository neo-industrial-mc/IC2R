package me.halfcooler.ic2r.core.block.heatgenerator.tileentity;

import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.heatgenerator.container.ContainerRTHeatGenerator;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumable;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableItemStack;
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
public class TileEntityRTHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui
{
	public static final float outputMultiplier = 2.0F * IC2RConfig.balance.energy.heatGenerator.radioisotope.get().floatValue();
	public final InvSlotConsumable fuelSlot = new InvSlotConsumableItemStack(this, "fuelSlot", 6, new ItemStack(Ic2rItems.RTG_PELLET));
	private boolean newActive;

	public TileEntityRTHeatGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.RT_HEAT_GENERATOR, pos, state);
		this.fuelSlot.setStackSizeLimit(1);
		this.newActive = false;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.HeatBuffer > 0)
		{
			this.newActive = true;
		} else
		{
			this.newActive = false;
		}

		if (this.getActive() != this.newActive)
		{
			this.setActive(this.newActive);
		}
	}

	@Override
	protected int fillHeatBuffer(int maxAmount)
	{
		return maxAmount >= this.getMaxHeatEmittedPerTick() ? this.getMaxHeatEmittedPerTick() : maxAmount;
	}

	@Override
	public int getMaxHeatEmittedPerTick()
	{
		int counter = 0;

		for (int i = 0; i < this.fuelSlot.size(); i++)
		{
			if (!this.fuelSlot.isEmpty(i))
			{
				counter++;
			}
		}

		return counter == 0 ? 0 : (int) (Math.pow(2.0, counter - 1) * outputMultiplier);
	}

	@Override
	public ContainerBase<TileEntityRTHeatGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerRTHeatGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerRTHeatGenerator(syncId, inventory, this);
	}
}
