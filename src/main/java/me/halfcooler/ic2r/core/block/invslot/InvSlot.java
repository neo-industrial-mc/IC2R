package me.halfcooler.ic2r.core.block.invslot;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.IInventorySlotHolder;
import me.halfcooler.ic2r.core.util.LegacyItemStackNbt;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.core.util.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InvSlot implements Iterable<ItemStack>
{
	public final IInventorySlotHolder<?> base;
	public final String name;
	public final InvSlot.InvSide preferredSide;
	protected final InvSlot.Access access;
	private final ItemStack[] contents;
	private int stackSizeLimit;
	/** Lazy Forge IItemHandler adapter (W2.1); storage stays in {@link #contents}. */
	private Object itemHandler;
	private static java.util.function.Function<InvSlot, Object> handlerFactory;

	public InvSlot(IInventorySlotHolder<?> base, String name, InvSlot.Access access, int count)
	{
		this(base, name, access, count, InvSlot.InvSide.ANY);
	}

	public InvSlot(IInventorySlotHolder<?> base, String name, InvSlot.Access access, int count, InvSlot.InvSide preferredSide)
	{
		if (count <= 0)
		{
			throw new IllegalArgumentException("invalid slot count: " + count);
		}

		this.contents = new ItemStack[count];
		this.clear();
		this.base = base;
		this.name = name;
		this.access = access;
		this.preferredSide = preferredSide;
		this.stackSizeLimit = 64;
		base.addInventorySlot(this);
	}

	public InvSlot(int count)
	{
		this.contents = new ItemStack[count];
		this.clear();
		this.base = null;
		this.name = null;
		this.access = InvSlot.Access.NONE;
		this.preferredSide = InvSlot.InvSide.ANY;
	}

	public void readFromNbt(CompoundTag nbt)
	{
		this.readFromNbt(nbt, RegistryAccess.EMPTY);
	}

	public void readFromNbt(CompoundTag nbt, @Nullable HolderLookup.Provider registries)
	{
		this.clear();
		ListTag contentsTag = nbt.getList("Contents", 10);

		for (int i = 0; i < contentsTag.size(); i++)
		{
			CompoundTag contentTag = contentsTag.getCompound(i);
			int index = contentTag.getByte("Index") & 255;
			if (index >= this.size())
			{
				IC2R.log
					.error(
						LogCategory.Block,
						"Can't load item stack for %s, slot %s, index %d is out of bounds.",
						Util.toString(this.base.getParent()),
						this.name,
						index
					);
			} else
			{
				// Normalize 1.20.1 Count/tag/Damage → 1.21 count/components (DFU never walks InvSlots).
				ItemStack stack = LegacyItemStackNbt.parseOptional(registries, contentTag);
				if (StackUtil.isEmpty(stack))
				{
					IC2R.log
						.warn(
							LogCategory.Block,
							"Can't load item stack %s for %s, slot %s, index %d, no matching item for %s.",
							StackUtil.toStringSafe(stack),
							Util.toString(this.base.getParent()),
							this.name,
							index,
							contentTag.getString("id")
						);
				} else
				{
					if (!this.isEmpty(index))
					{
						IC2R.log
							.error(
								LogCategory.Block,
								"Loading content to non-empty slot for %s, slot %s, index %d, replacing %s with %s.",
								Util.toString(this.base.getParent()),
								this.name,
								index,
								this.get(index),
								stack
							);
					}

					this.putFromNBT(index, stack);
				}
			}
		}

		this.onChanged();
	}

	public void writeToNbt(CompoundTag nbt)
	{
		this.writeToNbt(nbt, RegistryAccess.EMPTY);
	}

	public void writeToNbt(CompoundTag nbt, @Nullable HolderLookup.Provider registries)
	{
		ListTag contentsTag = new ListTag();

		for (int i = 0; i < this.contents.length; i++)
		{
			ItemStack content = this.contents[i];
			if (!StackUtil.isEmpty(content))
			{
				CompoundTag contentTag = new CompoundTag();
				contentTag.putByte("Index", (byte) i);
				LegacyItemStackNbt.saveInto(registries, content, contentTag);
				contentsTag.add(contentTag);
			}
		}

		nbt.put("Contents", contentsTag);
	}

	public int size()
	{
		return this.contents.length;
	}

	public boolean isEmpty()
	{
		for (ItemStack stack : this.contents)
		{
			if (!StackUtil.isEmpty(stack))
			{
				return false;
			}
		}

		return true;
	}

	public boolean isEmpty(int index)
	{
		return StackUtil.isEmpty(this.contents[index]);
	}

	public ItemStack get()
	{
		return this.get(0);
	}

	public ItemStack get(int index)
	{
		return this.contents[index];
	}

	public void put(ItemStack content)
	{
		this.put(0, content);
	}

	protected void putFromNBT(int index, ItemStack content)
	{
		this.contents[index] = content;
	}

	public void put(int index, ItemStack content)
	{
		if (StackUtil.isEmpty(content))
		{
			content = StackUtil.emptyStack;
		}

		this.contents[index] = content;
		this.onChanged();
	}

	public void clear()
	{
		Arrays.fill(this.contents, StackUtil.emptyStack);
	}

	public void clear(int index)
	{
		this.put(index, StackUtil.emptyStack);
	}

	public void onChanged()
	{
	}

	public boolean accepts(ItemStack stack)
	{
		return InvSlotTransferMath.defaultAccepts();
	}

	public boolean canInput()
	{
		return this.access == InvSlot.Access.I || this.access == InvSlot.Access.IO;
	}

	public boolean canOutput()
	{
		return this.access == InvSlot.Access.O || this.access == InvSlot.Access.IO;
	}

	public void organize()
	{
		for (int dstIndex = 0; dstIndex < this.contents.length - 1; dstIndex++)
		{
			ItemStack dst = this.contents[dstIndex];
			if (StackUtil.isEmpty(dst) || StackUtil.getSize(dst) < dst.getMaxStackSize())
			{
				for (int srcIndex = dstIndex + 1; srcIndex < this.contents.length; srcIndex++)
				{
					ItemStack src = this.contents[srcIndex];
					if (!StackUtil.isEmpty(src))
					{
						if (StackUtil.isEmpty(dst))
						{
							this.contents[srcIndex] = StackUtil.emptyStack;
							dst = src;
							this.contents[dstIndex] = src;
						} else if (StackUtil.checkItemEqualityStrict(dst, src))
						{
							int space = Math.min(this.getStackSizeLimit(), dst.getMaxStackSize() - StackUtil.getSize(dst));
							int srcSize = StackUtil.getSize(src);
							if (srcSize > space)
							{
								this.contents[srcIndex] = StackUtil.decSize(src, space);
								this.contents[dstIndex] = StackUtil.incSize(dst, space);
								break;
							}

							this.contents[srcIndex] = StackUtil.emptyStack;
							this.contents[dstIndex] = dst = StackUtil.incSize(dst, srcSize);
							if (srcSize == space)
							{
								break;
							}
						}
					}
				}
			}
		}
	}

	public int getStackSizeLimit()
	{
		return this.stackSizeLimit;
	}

	public void setStackSizeLimit(int stackSizeLimit)
	{
		this.stackSizeLimit = stackSizeLimit;
	}

	/**
	 * Forge item-handler view of this slot group (W2.1). Domain code may keep using get/put;
	 * automation should prefer this adapter (access + accepts enforced on insert/extract).
	 */
	public static void setHandlerFactory(java.util.function.Function<InvSlot, Object> factory)
	{
		handlerFactory = factory;
	}

	public Object getItemHandler()
	{
		if (this.itemHandler == null && handlerFactory != null)
		{
			this.itemHandler = handlerFactory.apply(this);
		}

		return this.itemHandler;
	}

	@Override
	public @NotNull Iterator<ItemStack> iterator()
	{
		return new Iterator<>()
		{
			private int idx = 0;

			@Override
			public boolean hasNext()
			{
				return this.idx < InvSlot.this.contents.length;
			}

			public ItemStack next()
			{
				if (this.idx >= InvSlot.this.contents.length)
				{
					throw new NoSuchElementException();
				} else
				{
					return InvSlot.this.contents[this.idx++];
				}
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public String toString()
	{
		StringBuilder ret = new StringBuilder(this.name + "[" + this.contents.length + "]: ");

		for (int i = 0; i < this.contents.length; i++)
		{
			ret.append(this.contents[i]);
			if (i < this.contents.length - 1)
			{
				ret.append(", ");
			}
		}

		return ret.toString();
	}

	protected ItemStack[] backup()
	{
		ItemStack[] ret = new ItemStack[this.contents.length];

		for (int i = 0; i < this.contents.length; i++)
		{
			ItemStack content = this.contents[i];
			ret[i] = StackUtil.isEmpty(content) ? StackUtil.emptyStack : content.copy();
		}

		return ret;
	}

	protected void restore(ItemStack[] backup)
	{
		if (backup.length != this.contents.length)
		{
			throw new IllegalArgumentException("invalid array size");
		}

		System.arraycopy(backup, 0, this.contents, 0, this.contents.length);
	}

	public void onPickupFromSlot()
	{
	}

	public enum Access
	{
		NONE,
		I,
		O,
		IO;

		public boolean isInput()
		{
			return (this.ordinal() & 1) != 0;
		}

		public boolean isOutput()
		{
			return (this.ordinal() & 2) != 0;
		}
	}

	public enum InvSide
	{
		ANY(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST),
		TOP(Direction.UP),
		BOTTOM(Direction.DOWN),
		SIDE(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST),
		NOTSIDE();

		private final Set<Direction> acceptedSides;

		InvSide(Direction... sides)
		{
			if (sides.length == 0)
			{
				this.acceptedSides = Collections.emptySet();
			} else
			{
				Set<Direction> acceptedSides = EnumSet.noneOf(Direction.class);
				acceptedSides.addAll(Arrays.asList(sides));
				this.acceptedSides = Collections.unmodifiableSet(acceptedSides);
			}
		}

		public boolean matches(Direction side)
		{
			return this.acceptedSides.contains(side);
		}

		public Set<Direction> getAcceptedSides()
		{
			return this.acceptedSides;
		}
	}
}
