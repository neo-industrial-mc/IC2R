package me.halfcooler.ic2r.forge.block.invslot;

import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotTransferMath;
import me.halfcooler.ic2r.core.util.StackUtil;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

/**
 * Forge {@link IItemHandlerModifiable} adapter over a single {@link InvSlot} (W2.1 / G2.1).
 * <p>
 * Machine domain logic continues to use InvSlot get/put/accepts; automation and capability
 * callers use this handler. Insert/extract respect {@link InvSlot#canInput()},
 * {@link InvSlot#canOutput()}, {@link InvSlot#accepts(ItemStack)}, and stack-size limits
 * via {@link InvSlotTransferMath}. Storage remains inside the InvSlot (no full InvSlot rewrite).
 * <p>
 * Contract and pure-logic mirrors: {@code docs/spec/item_handler_contract.md}. This class itself
 * needs ItemStack/Forge types — unit suite mirrors rules in {@code InvSlotHandlerMathTest}
 * without loading this adapter (no MC bootstrap in CI).
 */
public final class InvSlotItemHandler implements IItemHandlerModifiable
{
	private final InvSlot slot;

	public InvSlotItemHandler(InvSlot slot)
	{
		if (slot == null)
		{
			throw new NullPointerException("slot");
		}

		this.slot = slot;
	}

	public InvSlot getSlot()
	{
		return this.slot;
	}

	@Override
	public int getSlots()
	{
		return this.slot.size();
	}

	@Override
	public @NotNull ItemStack getStackInSlot(int index)
	{
		this.validateIndex(index);
		ItemStack stack = this.slot.get(index);
		return StackUtil.isEmpty(stack) ? ItemStack.EMPTY : stack;
	}

	@Override
	public @NotNull ItemStack insertItem(int index, @NotNull ItemStack stack, boolean simulate)
	{
		this.validateIndex(index);
		if (!InvSlotTransferMath.allowsInsert(this.slot.canInput(), this.slot.accepts(stack), stack.isEmpty()))
		{
			return stack;
		}

		ItemStack existing = this.slot.get(index);
		int existingCount = StackUtil.isEmpty(existing) ? 0 : StackUtil.getSize(existing);
		boolean compatible = existingCount == 0 || (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() < existing.getMaxStackSize());
		int insertable = InvSlotTransferMath.insertableCount(
			existingCount,
			stack.getCount(),
			this.slot.getStackSizeLimit(),
			stack.getMaxStackSize(),
			compatible
		);
		if (insertable <= 0)
		{
			return stack;
		}

		if (!simulate)
		{
			if (existingCount == 0)
			{
				ItemStack copy1 = stack.copy(); copy1.setCount(insertable); this.slot.put(index, copy1);
			} else
			{
				this.slot.put(index, StackUtil.incSize(existing, insertable));
			}
		}

		int remaining = InvSlotTransferMath.remainingAfterInsert(stack.getCount(), insertable);
		if (remaining == 0) return ItemStack.EMPTY; ItemStack result1 = stack.copy(); result1.setCount(remaining); return result1;
	}

	@Override
	public @NotNull ItemStack extractItem(int index, int amount, boolean simulate)
	{
		this.validateIndex(index);
		ItemStack existing = this.slot.get(index);
		boolean slotEmpty = StackUtil.isEmpty(existing);
		if (!InvSlotTransferMath.allowsExtract(this.slot.canOutput(), slotEmpty))
		{
			return ItemStack.EMPTY;
		}

		int existingCount = StackUtil.getSize(existing);
		int extractable = InvSlotTransferMath.extractableCount(existingCount, amount, existing.getMaxStackSize());
		if (extractable <= 0)
		{
			return ItemStack.EMPTY;
		}

		ItemStack result = existing.copy(); result.setCount(extractable);
		if (!simulate)
		{
			int left = InvSlotTransferMath.remainingAfterExtract(existingCount, extractable);
			if (left <= 0)
			{
				this.slot.put(index, StackUtil.emptyStack);
			} else
			{
				this.slot.put(index, StackUtil.setSize(existing, left));
			}
		}

		return result;
	}

	@Override
	public int getSlotLimit(int index)
	{
		this.validateIndex(index);
		return this.slot.getStackSizeLimit();
	}

	@Override
	public boolean isItemValid(int index, @NotNull ItemStack stack)
	{
		this.validateIndex(index);
		return InvSlotTransferMath.allowsInsert(this.slot.canInput(), this.slot.accepts(stack), stack.isEmpty());
	}

	/**
	 * Direct set (GUI / internal). Does not re-check {@link InvSlot#accepts(ItemStack)};
	 * use {@link #insertItem} for automation-safe placement.
	 */
	@Override
	public void setStackInSlot(int index, @NotNull ItemStack stack)
	{
		this.validateIndex(index);
		this.slot.put(index, stack.isEmpty() ? StackUtil.emptyStack : stack);
	}

	private void validateIndex(int index)
	{
		if (index < 0 || index >= this.slot.size())
		{
			throw new IndexOutOfBoundsException("slot index " + index + " / " + this.slot.size());
		}
	}
}
