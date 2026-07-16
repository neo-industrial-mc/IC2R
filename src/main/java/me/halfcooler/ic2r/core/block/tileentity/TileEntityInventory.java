package me.halfcooler.ic2r.core.block.tileentity;

import me.halfcooler.ic2r.core.block.IInventorySlotHolder;
import me.halfcooler.ic2r.core.block.comp.ComparatorEmitter;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.HolderLookup;
public abstract class TileEntityInventory extends Ic2rTileEntity implements WorldlyContainer, IInventorySlotHolder<TileEntityInventory>
{
	protected final ComparatorEmitter comparator = this.addComponent(new ComparatorEmitter(this));
	private final List<InvSlot> invSlots = new ArrayList<>();
	/** Cached combined InvSlot → IItemHandler view (null side / full inventory). */
	private transient List<Object> cachedHandlerList;

	public TileEntityInventory(BlockEntityType<? extends TileEntityInventory> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.comparator.setUpdate(this::calcRedstoneFromInvSlots);
	}

	private static int getIndex(int loc)
	{
		return loc >>> 16;
	}

	private static int getOffset(int loc)
	{
		return loc & 65535;
	}

	protected static int calcRedstoneFromInvSlots(InvSlot... slots)
	{
		return calcRedstoneFromInvSlots(Arrays.asList(slots));
	}

	protected static int calcRedstoneFromInvSlots(Iterable<InvSlot> invSlots)
	{
		int space = 0;
		int used = 0;

		for (InvSlot slot : invSlots)
		{
			if (!(slot instanceof InvSlotUpgrade))
			{
				int size = slot.size();
				int limit = slot.getStackSizeLimit();
				space += size * limit;

				for (int i = 0; i < size; i++)
				{
					ItemStack stack = slot.get(i);
					if (!StackUtil.isEmpty(stack))
					{
						used += Math.min(limit, stack.getCount() * limit / stack.getMaxStackSize());
					}
				}
			}
		}

		return used != 0 && space != 0 ? 1 + used * 14 / space : 0;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		CompoundTag invSlotsTag = nbt.getCompound("InvSlots");

		for (InvSlot invSlot : this.invSlots)
		{
			invSlot.readFromNbt(invSlotsTag.getCompound(invSlot.name));
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.saveAdditional(nbt, registries);
		CompoundTag invSlotsTag = new CompoundTag();

		for (InvSlot invSlot : this.invSlots)
		{
			CompoundTag invSlotTag = new CompoundTag();
			invSlot.writeToNbt(invSlotTag);
			invSlotsTag.put(invSlot.name, invSlotTag);
		}

		nbt.put("InvSlots", invSlotsTag);
	}

	public int getContainerSize() {
		int ret = 0;

		for (InvSlot invSlot : this.invSlots)
		{
			ret += invSlot.size();
		}

		return ret;
	}

	public boolean isEmpty()
	{
		for (InvSlot invSlot : this.invSlots)
		{
			if (!invSlot.isEmpty())
			{
				return false;
			}
		}

		return true;
	}

	public @NotNull ItemStack getItem(int index)
	{
		int loc = this.locateInvSlot(index);
		return loc == -1 ? StackUtil.emptyStack : this.getStackAt(loc);
	}

	public @NotNull ItemStack removeItem(int index, int amount)
	{
		int loc = this.locateInvSlot(index);
		if (loc == -1)
		{
			return StackUtil.emptyStack;
		}

		ItemStack stack = this.getStackAt(loc);
		if (StackUtil.isEmpty(stack))
		{
			return StackUtil.emptyStack;
		}

		if (amount >= StackUtil.getSize(stack))
		{
			this.putStackAt(loc, StackUtil.emptyStack);
			return stack;
		}

		if (amount != 0)
		{
			if (amount < 0)
			{
				int space = Math.min(this.getAt(loc).getStackSizeLimit(), stack.getMaxStackSize()) - StackUtil.getSize(stack);
				amount = Math.max(amount, -space);
			}

			this.putStackAt(loc, StackUtil.decSize(stack, amount));
		}

		ItemStack ret = stack.copy();
		return StackUtil.setSize(ret, amount);
	}

	public @NotNull ItemStack removeItemNoUpdate(int index)
	{
		int loc = this.locateInvSlot(index);
		if (loc == -1)
		{
			return StackUtil.emptyStack;
		}

		ItemStack ret = this.getStackAt(loc);
		if (!StackUtil.isEmpty(ret))
		{
			this.putStackAt(loc, StackUtil.emptyStack);
		}

		return ret;
	}

	public void setItem(int index, @NotNull ItemStack stack)
	{
		int loc = this.locateInvSlot(index);
		if (loc == -1)
		{
			assert false;
		} else
		{
			if (StackUtil.isEmpty(stack))
			{
				stack = StackUtil.emptyStack;
			}

			this.putStackAt(loc, stack);
		}
	}

	public void setChanged()
	{
		super.setChanged();

		for (InvSlot invSlot : this.invSlots)
		{
			invSlot.onChanged();
		}
	}

	public int getMaxStackSize()
	{
		int max = 0;

		for (InvSlot slot : this.invSlots)
		{
			max = Math.max(max, slot.getStackSizeLimit());
		}

		return max;
	}

	public boolean stillValid(@NotNull Player player)
	{
		return !this.isRemoved() && player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
	}

	public void startOpen(@NotNull Player player)
	{
	}

	public void stopOpen(@NotNull Player player)
	{
	}

	public boolean canPlaceItem(int index, ItemStack stack)
	{
		if (stack.isEmpty())
		{
			return false;
		}

		InvSlot invSlot = this.getInventorySlot(index);
		return invSlot != null && invSlot.canInput() && invSlot.accepts(stack);
	}

	public int @NotNull [] getSlotsForFace(@NotNull Direction side)
	{
		int[] ret = new int[this.getContainerSize()];
		int i = 0;

		while (i < ret.length)
		{
			ret[i] = i++;
		}

		return ret;
	}

	public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, Direction side)
	{
		if (StackUtil.isEmpty(stack))
		{
			return false;
		}

		InvSlot targetSlot = this.getInventorySlot(index);
		if (targetSlot == null)
		{
			return false;
		}

		if (targetSlot.canInput() && targetSlot.accepts(stack))
		{
			if (targetSlot.preferredSide != InvSlot.InvSide.ANY && targetSlot.preferredSide.matches(side))
			{
				return true;
			}

			for (InvSlot invSlot : this.invSlots)
			{
				if (invSlot != targetSlot
					&& invSlot.preferredSide != InvSlot.InvSide.ANY
					&& invSlot.preferredSide.matches(side)
					&& invSlot.canInput()
					&& invSlot.accepts(stack))
				{
					return false;
				}
			}

			return true;
		} else
		{
			return false;
		}
	}

	public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction side)
	{
		InvSlot targetSlot = this.getInventorySlot(index);
		if (targetSlot != null && targetSlot.canOutput())
		{
			boolean correctSide = targetSlot.preferredSide.matches(side);
			if (targetSlot.preferredSide != InvSlot.InvSide.ANY && correctSide)
			{
				return true;
			}

			for (InvSlot invSlot : this.invSlots)
			{
				if (invSlot != targetSlot
					&& (invSlot.preferredSide != InvSlot.InvSide.ANY || !correctSide)
					&& invSlot.preferredSide.matches(side)
					&& invSlot.canOutput()
					&& !invSlot.isEmpty())
				{
					return false;
				}
			}

			return true;
		} else
		{
			return false;
		}
	}

	public void clearContent()
	{
		for (InvSlot invSlot : this.invSlots)
		{
			invSlot.clear();
		}
	}

	@Override
	public int getBaseIndex(InvSlot invSlot)
	{
		int ret = 0;

		for (InvSlot slot : this.invSlots)
		{
			if (slot == invSlot)
			{
				return ret;
			}

			ret += slot.size();
		}

		return -1;
	}

	public TileEntityInventory getParent()
	{
		return this;
	}

	@Override
	public InvSlot getInventorySlot(String name)
	{
		for (InvSlot invSlot : this.invSlots)
		{
			if (invSlot.name.equals(name))
			{
				return invSlot;
			}
		}

		return null;
	}

	@Override
	public void addInventorySlot(InvSlot inventorySlot)
	{
		assert this.invSlots.stream().noneMatch(slot -> slot.name.equals(inventorySlot.name));
		this.invSlots.add(inventorySlot);
		// Slot list changed after construction is rare; drop combined-handler cache if present.
		this.cachedHandlerList = null;
	}

	/**
	 * Unmodifiable view of registered InvSlots (construction order = combined handler order).
	 */
	public List<InvSlot> getInvSlots()
	{
		return Collections.unmodifiableList(this.invSlots);
	}
	
	public List<Object> getInvSlotHandlerList()
	{
		if (this.cachedHandlerList == null || this.cachedHandlerList.size() != this.invSlots.size())
		{
			List<Object> handlers = new java.util.ArrayList<>(this.invSlots.size());
			for (InvSlot invSlot : this.invSlots)
			{
				handlers.add(invSlot.getItemHandler());
			}
			this.cachedHandlerList = java.util.Collections.unmodifiableList(handlers);
		}
		return this.cachedHandlerList;
	}
	
	/** NeoForge 1.21 removed invalidateCaps; clear local handler cache when inventory layout changes. */
	public void invalidateItemHandlerCache()
	{
		this.cachedHandlerList = null;
	}

	

	private int locateInvSlot(int extIndex)
	{
		if (extIndex < 0)
		{
			return -1;
		}

		for (int i = 0; i < this.invSlots.size(); i++)
		{
			int size = this.invSlots.get(i).size();
			if (extIndex < size)
			{
				return i << 16 | extIndex;
			}

			extIndex -= size;
		}

		return -1;
	}

	private InvSlot getAt(int loc)
	{
		assert loc != -1;
		return this.invSlots.get(getIndex(loc));
	}

	private ItemStack getStackAt(int loc)
	{
		return this.getAt(loc).get(getOffset(loc));
	}

	private void putStackAt(int loc, ItemStack stack)
	{
		this.getAt(loc).put(getOffset(loc), stack);
		super.setChanged();
	}

	private InvSlot getInventorySlot(int extIndex)
	{
		int loc = this.locateInvSlot(extIndex);
		return loc == -1 ? null : this.getAt(loc);
	}

	@Override
	protected List<ItemStack> getAuxDrops(int fortune)
	{
		List<ItemStack> ret = new ArrayList<>(super.getAuxDrops(fortune));

		for (InvSlot slot : this.invSlots)
		{
			for (ItemStack stack : slot)
			{
				if (!StackUtil.isEmpty(stack))
				{
					ret.add(stack);
				}
			}
		}

		return ret;
	}

	protected int calcRedstoneFromInvSlots()
	{
		return calcRedstoneFromInvSlots(this.invSlots);
	}
}
