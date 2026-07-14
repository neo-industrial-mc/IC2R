package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumable;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableId;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityNuke extends TileEntityBridgeNuke implements IHasGui
{
	public final InvSlotConsumable outsideSlot;
	public final InvSlotConsumable insideSlot = new InvSlotConsumableId(
		this,
		"insideSlot",
		1,
		Ic2rItems.URANIUM_BLOCK,
		Ic2rItems.URANIUM_238,
		Ic2rItems.URANIUM_235,
		Ic2rItems.SMALL_URANIUM_235,
		Ic2rItems.PLUTONIUM,
		Ic2rItems.SMALL_PLUTONIUM
	);
	public int RadiationRange;

	public TileEntityNuke(BlockPos pos, BlockState state)
	{
		super(pos, state);
		this.outsideSlot = new InvSlotConsumableId(this, "outsideSlot", 1, Ic2rItems.ITNT);
	}

	@Override
	public int getRadiationRange()
	{
		return this.RadiationRange;
	}

	public void setRadiationRange(int range)
	{
		if (range != this.RadiationRange)
		{
			this.RadiationRange = range;
		}
	}

	@Override
	public float getNukeExplosivePower()
	{
		if (this.outsideSlot.isEmpty())
		{
			return -1.0F;
		}

		int itntCount = StackUtil.getSize(this.outsideSlot.get());
		double ret = 5.0 * Math.pow(itntCount, 0.3333333333333333);
		if (this.insideSlot.isEmpty())
		{
			this.setRadiationRange(0);
		} else
		{
			ItemStack insideStack = this.insideSlot.get();
			Item inside = insideStack.getItem();
			int insideCount = StackUtil.getSize(insideStack);
			if (inside == Ic2rItems.URANIUM_238)
			{
				this.setRadiationRange(itntCount);
			} else if (inside == Ic2rItems.URANIUM_BLOCK)
			{
				this.setRadiationRange(itntCount * 6);
			} else if (inside == Ic2rItems.SMALL_URANIUM_235)
			{
				this.setRadiationRange(itntCount * 2);
				if (itntCount >= 64)
				{
					ret += 0.05555555555555555 * Math.pow(insideCount, 1.6);
				}
			} else if (inside == Ic2rItems.URANIUM_235)
			{
				this.setRadiationRange(itntCount * 2);
				if (itntCount >= 32)
				{
					ret += 0.5 * Math.pow(insideCount, 1.4);
				}
			} else if (inside == Ic2rItems.SMALL_PLUTONIUM)
			{
				this.setRadiationRange(itntCount * 3);
				if (itntCount >= 32)
				{
					ret += 0.05555555555555555 * Math.pow(insideCount, 2.0);
				}
			} else if (inside == Ic2rItems.PLUTONIUM)
			{
				this.setRadiationRange(itntCount * 4);
				if (itntCount >= 16)
				{
					ret += 0.5 * Math.pow(insideCount, 1.8);
				}
			}
		}

		ret = Math.min(ret, IC2RConfig.protection.nukeExplosionPowerLimit.get().floatValue());
		return (float) ret;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	@Override
	protected void onIgnite(LivingEntity igniter)
	{
		super.onIgnite(igniter);
		this.outsideSlot.clear();
		this.insideSlot.clear();
	}
}
