package me.halfcooler.ic2r.core.block.invslot;

/**
 * Pure insert/extract arithmetic for {@link InvSlotItemHandler}.
 * No Minecraft/Forge types — unit-testable without client or registry bootstrap (W2.1 / G2.1 / G2.4).
 * Combined-index helpers mirror {@code TileEntityInventory} multi-InvSlot layout for the
 * null-facing combined {@code ITEM_HANDLER} view. See {@code docs/spec/item_handler_contract.md}.
 * <p>
 * Accept / consumable-output helpers mirror {@link InvSlot#accepts}, {@link InvSlotOutput},
 * and {@link InvSlotConsumable#canOutput()} leftover-eject rules.
 */
public final class InvSlotTransferMath
{
	private InvSlotTransferMath()
	{
	}

	/**
	 * Whether an insert may proceed given access / accepts / emptiness of the incoming stack.
	 */
	public static boolean allowsInsert(boolean canInput, boolean accepts, boolean incomingEmpty)
	{
		return canInput && accepts && !incomingEmpty;
	}

	/**
	 * Whether an extract may proceed given access and whether the slot is empty.
	 */
	public static boolean allowsExtract(boolean canOutput, boolean slotEmpty)
	{
		return canOutput && !slotEmpty;
	}

	/**
	 * Default {@link InvSlot#accepts} policy — accepts any offered stack
	 * (emptiness gated separately by {@link #allowsInsert}).
	 */
	public static boolean defaultAccepts()
	{
		return true;
	}

	/**
	 * {@link InvSlotOutput#accepts} policy — always rejects automation insert.
	 */
	public static boolean outputAccepts()
	{
		return false;
	}

	/**
	 * Linked / filter accept: only when linked has content and stacks are equal.
	 * Mirrors {@code InvSlotConsumableLinked} spirit without ItemStack types.
	 */
	public static boolean linkedAccepts(boolean linkedEmpty, boolean sameAsLinked)
	{
		return !linkedEmpty && sameAsLinked;
	}

	/**
	 * {@link InvSlotConsumable#canOutput()} — allow extract of invalid leftover items.
	 * {@code accessAllowsOutput || (!accessIsNone && !slotEmpty && !acceptsCurrent)}.
	 */
	public static boolean consumableCanOutput(
		boolean accessAllowsOutput,
		boolean accessIsNone,
		boolean slotEmpty,
		boolean acceptsCurrent
	)
	{
		if (accessAllowsOutput)
		{
			return true;
		}

		return !accessIsNone && !slotEmpty && !acceptsCurrent;
	}

	/**
	 * How many items can be inserted into a slot.
	 *
	 * @param existingCount     current count in the slot (0 if empty)
	 * @param incomingCount     size of the stack being inserted
	 * @param slotLimit         InvSlot stack-size limit
	 * @param maxStackSize      item max stack size
	 * @param stacksCompatible  true if slot empty or items may merge
	 * @return amount that would be accepted (0..incomingCount)
	 */
	public static int insertableCount(int existingCount, int incomingCount, int slotLimit, int maxStackSize, boolean stacksCompatible)
	{
		if (incomingCount <= 0 || existingCount < 0 || !stacksCompatible)
		{
			return 0;
		}

		int space = Math.min(slotLimit, maxStackSize) - existingCount;
		if (space <= 0)
		{
			return 0;
		}

		return Math.min(space, incomingCount);
	}

	/**
	 * How many items can be extracted from a slot.
	 *
	 * @param existingCount current count in the slot
	 * @param request       requested extract amount
	 * @param maxStackSize  item max stack size (cap on single extract)
	 * @return amount that would be extracted
	 */
	public static int extractableCount(int existingCount, int request, int maxStackSize)
	{
		if (existingCount <= 0 || request <= 0 || maxStackSize <= 0)
		{
			return 0;
		}

		return Math.min(existingCount, Math.min(request, maxStackSize));
	}

	/**
	 * Remaining count after a successful insert of {@code inserted} from {@code incomingCount}.
	 */
	public static int remainingAfterInsert(int incomingCount, int inserted)
	{
		if (incomingCount <= 0 || inserted <= 0)
		{
			return Math.max(0, incomingCount);
		}

		return Math.max(0, incomingCount - inserted);
	}

	/**
	 * Slot count after extracting {@code extracted} from {@code existingCount}.
	 */
	public static int remainingAfterExtract(int existingCount, int extracted)
	{
		if (existingCount <= 0 || extracted <= 0)
		{
			return Math.max(0, existingCount);
		}

		return Math.max(0, existingCount - extracted);
	}

	/**
	 * Flatten multi-InvSlot layouts into a single handler index space (combined handler).
	 *
	 * @param slotSizes sizes of each InvSlot in order
	 * @return total slot count
	 */
	public static int totalSlots(int... slotSizes)
	{
		int total = 0;
		for (int size : slotSizes)
		{
			if (size < 0)
			{
				throw new IllegalArgumentException("negative slot size: " + size);
			}

			total += size;
		}

		return total;
	}

	/**
	 * Map a combined-handler index to (slotGroupIndex, indexWithinGroup).
	 *
	 * @return packed {@code (group << 16) | local}, or {@code -1} if out of range
	 */
	public static int locateCombinedIndex(int combinedIndex, int... slotSizes)
	{
		if (combinedIndex < 0)
		{
			return -1;
		}

		int remaining = combinedIndex;
		for (int group = 0; group < slotSizes.length; group++)
		{
			int size = slotSizes[group];
			if (remaining < size)
			{
				return group << 16 | remaining;
			}

			remaining -= size;
		}

		return -1;
	}

	public static int unpackGroup(int packed)
	{
		return packed >>> 16;
	}

	public static int unpackLocal(int packed)
	{
		return packed & 0xFFFF;
	}
}
