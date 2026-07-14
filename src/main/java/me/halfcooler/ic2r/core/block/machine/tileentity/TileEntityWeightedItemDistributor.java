package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.network.ClientModifiable;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.machine.container.ContainerWeightedItemDistributor;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.item.EnvItemHandler;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.util.StackUtil;

import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityWeightedItemDistributor extends TileEntityInventory implements IHasGui, IWeightedDistributor, ServerTicker
{
	public final InvSlot buffer = new InvSlot(this, "buffer", InvSlot.Access.I, 9);
	@ClientModifiable
	protected List<Direction> priority = new ArrayList<>(5);

	public TileEntityWeightedItemDistributor(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.WEIGHTED_ITEM_DISTRIBUTOR, pos, state);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		int[] indexes = nbt.getIntArray("priority");
		for (int index : indexes)
		{
			this.priority.add(Direction.from3DDataValue(index));
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		if (!this.priority.isEmpty())
		{
			int[] indexes = new int[this.priority.size()];

			for (int i = 0; i < indexes.length; i++)
			{
				indexes[i] = this.priority.get(i).get3DDataValue();
			}

			nbt.putIntArray("priority", indexes);
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("priority");
		return ret;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
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
		if (!this.getLevel().isClientSide && !this.priority.isEmpty() && this.priority.remove(this.getFacing()))
		{
			this.updatePriority(true);
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (!this.priority.isEmpty() && !this.buffer.isEmpty())
		{
			Level world = this.getLevel();
			boolean hasChanged = false;

			for (Direction facing : this.priority)
			{
				EnvItemHandler.AdjacentInventory inv = StackUtil.ENV.getAdjacentInventory(this, facing);
				if (inv != null)
				{
					boolean empty = true;

					for (int index = 0; index < this.buffer.size(); index++)
					{
						if (!this.buffer.isEmpty(index))
						{
							ItemStack stack = this.buffer.get(index);
							ItemStack transferStack = StackUtil.copy(stack);
							int amount = StackUtil.ENV.deposit(inv, transferStack, true);
							if (amount > 0)
							{
								amount = StackUtil.ENV.deposit(inv, transferStack, false);
								stack = StackUtil.decSize(stack, amount);
								this.buffer.put(index, stack);
								hasChanged = true;
								empty &= StackUtil.isEmpty(stack);
							}
						}
					}

					if (hasChanged && empty)
					{
						break;
					}
				}
			}

			if (hasChanged)
			{
				this.setChanged();
			}
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerWeightedItemDistributor(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerWeightedItemDistributor(syncId, inventory, this);
	}

	@Override
	public List<Direction> getPriority()
	{
		return this.priority;
	}

	@Override
	public void updatePriority(boolean server)
	{
		IC2R.network.get(server).updateTileEntityField(this, "priority");
	}
}
