package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.network.INetworkClientTileEntityEventListener;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquid;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquidByTank;
import me.halfcooler.ic2r.core.block.invslot.InvSlotOutput;
import me.halfcooler.ic2r.core.block.machine.container.ContainerFluidDistributor;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.util.LiquidUtil;
import me.halfcooler.ic2r.core.util.Util;

import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityFluidDistributor extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener, ServerTicker
{
	public final InvSlotConsumableLiquidByTank inputSlot;
	public final InvSlotOutput OutputSlot;
	@GuiSynced
	public final Fluids.InternalFluidTank fluidTank;
	protected final Fluids fluids = this.addComponent(new Fluids(this));

	public TileEntityFluidDistributor(BlockPos pos, BlockState state)
	{
		this(Ic2rBlockEntities.FLUID_DISTRIBUTOR, pos, state);
	}

	protected TileEntityFluidDistributor(BlockEntityType<? extends TileEntityFluidDistributor> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.fluidTank = this.fluids.addTank("fluidTank", 1000);
		this.inputSlot = new InvSlotConsumableLiquidByTank(
			this, "inputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, this.fluidTank
		);
		this.OutputSlot = new InvSlotOutput(this, "OutputSlot", 1);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateConnectivity();
	}

	@Override
	public void setActive(boolean val)
	{
		super.setActive(val);
		this.updateConnectivity();
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		this.updateConnectivity();
	}

	protected void updateConnectivity()
	{
		EnumSet<Direction> acceptingSides = EnumSet.of(this.getFacing());
		if (this.getActive())
		{
			acceptingSides = EnumSet.complementOf(acceptingSides);
		}

		this.fluids.changeConnectivity(this.fluidTank, acceptingSides, Collections.emptySet());
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.inputSlot.processFromTank(this.fluidTank, this.OutputSlot);
		if (this.fluidTank.getFluidAmount() > 0)
		{
			this.moveFluid();
		}
	}

	protected void moveFluid()
	{
		Level world = this.getLevel();
		if (this.getActive())
		{
			BlockEntity target = world.getBlockEntity(this.worldPosition.relative(this.getFacing()));
			Direction side = this.getFacing().getOpposite();
			if (LiquidUtil.isFluidTile(target, side))
			{
				int amount = LiquidUtil.fillTile(target, side, this.fluidTank.getFluidStack(), false);
				if (amount > 0)
				{
					this.fluidTank.drainMbUnchecked(amount, false);
				}
			}
		} else
		{
			Map<Direction, BlockEntity> acceptingNeighbors = new EnumMap<>(Direction.class);
			int acceptedVolume = 0;

			for (Direction dir : Util.ALL_DIRS)
			{
				if (dir != this.getFacing())
				{
					BlockEntity target = world.getBlockEntity(this.worldPosition.relative(dir));
					Direction side = dir.getOpposite();
					if (LiquidUtil.isFluidTile(target, side))
					{
						int amount = LiquidUtil.fillTile(target, side, this.fluidTank.getFluidStack(), true);
						if (amount > 0)
						{
							acceptingNeighbors.put(dir, target);
							acceptedVolume += amount;
						}
					}
				}
			}

			while (!acceptingNeighbors.isEmpty())
			{
				int amount = Math.min(acceptedVolume, this.fluidTank.getFluidAmount());
				if (amount <= 0)
				{
					break;
				}

				amount /= acceptingNeighbors.size();
				if (amount == 0)
				{
					for (Entry<Direction, BlockEntity> entry : acceptingNeighbors.entrySet())
					{
						BlockEntity target = entry.getValue();
						Direction side = entry.getKey().getOpposite();
						Ic2rFluidStack fs = this.fluidTank.getFluidStack();
						if (fs == null)
						{
							return;
						}

						fs = fs.copy();
						fs.setAmountMb(Math.min(acceptedVolume, fs.getAmountMb()));
						if (fs.isEmpty())
						{
							return;
						}

						int cAmount = LiquidUtil.fillTile(target, side, fs, false);
						this.fluidTank.drainMbUnchecked(cAmount, false);
						acceptedVolume -= cAmount;
					}
					break;
				}

				Iterator<Entry<Direction, BlockEntity>> it = acceptingNeighbors.entrySet().iterator();

				while (it.hasNext())
				{
					Entry<Direction, BlockEntity> entry = it.next();
					BlockEntity target = entry.getValue();
					Direction side = entry.getKey().getOpposite();
					Ic2rFluidStack fs = this.fluidTank.getFluidStack();
					if (fs == null)
					{
						break;
					}

					fs = fs.copy();
					if (fs.isEmpty())
					{
						break;
					}

					fs.setAmountMb(Math.min(amount, fs.getAmountMb()));
					int cAmount = LiquidUtil.fillTile(target, side, fs, false);
					this.fluidTank.drainMbUnchecked(cAmount, false);
					acceptedVolume -= cAmount;
					if (cAmount < fs.getAmountMb())
					{
						it.remove();
					}
				}
			}
		}
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		this.setActive(!this.getActive());
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerFluidDistributor(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerFluidDistributor(syncId, inventory, this);
	}
}
