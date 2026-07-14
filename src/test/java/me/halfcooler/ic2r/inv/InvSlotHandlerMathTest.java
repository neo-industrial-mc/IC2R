package me.halfcooler.ic2r.inv;

import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotTransferMath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure logic tests for InvSlot → IItemHandler transfer rules (W2.1).
 * Does not construct ItemStacks / handlers (no Minecraft bootstrap).
 * <p>
 * Mirrors {@code InvSlotItemHandler} insert/extract gating and residual math so
 * domain InvSlot access stays consistent with the Forge adapter.
 */
class InvSlotHandlerMathTest
{
	// --- Access / gating (InvSlot.Access ↔ handler insert/extract) ---

	@Test
	void access_flags_match_inv_slot_enum()
	{
		assertTrue(InvSlot.Access.I.isInput());
		assertFalse(InvSlot.Access.I.isOutput());

		assertFalse(InvSlot.Access.O.isInput());
		assertTrue(InvSlot.Access.O.isOutput());

		assertTrue(InvSlot.Access.IO.isInput());
		assertTrue(InvSlot.Access.IO.isOutput());

		assertFalse(InvSlot.Access.NONE.isInput());
		assertFalse(InvSlot.Access.NONE.isOutput());
	}

	@Test
	void allowsInsert_requires_input_access_accepts_and_nonempty()
	{
		assertTrue(InvSlotTransferMath.allowsInsert(true, true, false));
		assertFalse(InvSlotTransferMath.allowsInsert(false, true, false));
		assertFalse(InvSlotTransferMath.allowsInsert(true, false, false));
		assertFalse(InvSlotTransferMath.allowsInsert(true, true, true));
	}

	@Test
	void allowsExtract_requires_output_access_and_nonempty_slot()
	{
		assertTrue(InvSlotTransferMath.allowsExtract(true, false));
		assertFalse(InvSlotTransferMath.allowsExtract(false, false));
		assertFalse(InvSlotTransferMath.allowsExtract(true, true));
	}

	@Test
	void input_only_slot_blocks_extract_like_macerator_input()
	{
		// InvSlotConsumable default Access.I — hoppers must not pull valid inputs
		boolean canInput = InvSlot.Access.I.isInput();
		boolean canOutput = InvSlot.Access.I.isOutput();
		assertTrue(InvSlotTransferMath.allowsInsert(canInput, true, false));
		assertFalse(InvSlotTransferMath.allowsExtract(canOutput, false));
	}

	@Test
	void output_only_slot_blocks_insert_like_macerator_output()
	{
		// InvSlotOutput Access.O + accepts()==false
		boolean canInput = InvSlot.Access.O.isInput();
		boolean canOutput = InvSlot.Access.O.isOutput();
		assertFalse(InvSlotTransferMath.allowsInsert(canInput, false, false));
		assertTrue(InvSlotTransferMath.allowsExtract(canOutput, false));
	}

	@Test
	void upgrade_access_none_blocks_both_directions()
	{
		boolean canInput = InvSlot.Access.NONE.isInput();
		boolean canOutput = InvSlot.Access.NONE.isOutput();
		assertFalse(InvSlotTransferMath.allowsInsert(canInput, true, false));
		assertFalse(InvSlotTransferMath.allowsExtract(canOutput, false));
	}

	// --- insert / extract residual consistency with slot counts ---

	@Test
	void insertable_into_empty_slot_respects_limits()
	{
		assertEquals(16, InvSlotTransferMath.insertableCount(0, 16, 64, 64, true));
		assertEquals(32, InvSlotTransferMath.insertableCount(0, 64, 32, 64, true));
		assertEquals(16, InvSlotTransferMath.insertableCount(0, 64, 64, 16, true));
		assertEquals(0, InvSlotTransferMath.insertableCount(0, 10, 64, 64, false));
	}

	@Test
	void insertable_merges_with_existing_and_computes_remaining()
	{
		int existing = 50;
		int incoming = 20;
		int inserted = InvSlotTransferMath.insertableCount(existing, incoming, 64, 64, true);
		assertEquals(14, inserted); // space 14
		assertEquals(6, InvSlotTransferMath.remainingAfterInsert(incoming, inserted));
		assertEquals(64, existing + inserted);
	}

	@Test
	void insertable_full_slot_returns_zero_inserted_full_remaining()
	{
		int inserted = InvSlotTransferMath.insertableCount(64, 8, 64, 64, true);
		assertEquals(0, inserted);
		assertEquals(8, InvSlotTransferMath.remainingAfterInsert(8, inserted));
	}

	@Test
	void extractable_partial_and_full_matches_slot_leftover()
	{
		int existing = 10;
		int extracted = InvSlotTransferMath.extractableCount(existing, 3, 64);
		assertEquals(3, extracted);
		assertEquals(7, InvSlotTransferMath.remainingAfterExtract(existing, extracted));

		extracted = InvSlotTransferMath.extractableCount(existing, 99, 64);
		assertEquals(10, extracted);
		assertEquals(0, InvSlotTransferMath.remainingAfterExtract(existing, extracted));
	}

	@Test
	void extractable_capped_by_max_stack_size()
	{
		assertEquals(16, InvSlotTransferMath.extractableCount(40, 30, 16));
	}

	// --- combined multi-slot index layout (standard machine: discharge + input + output + upgrade) ---

	@Test
	void combined_index_layout_matches_standard_machine_slot_sizes()
	{
		// TileEntityElectricMachine.discharge(1) + input(1) + output(1) + upgrade(4) typical macerator
		int[] sizes = {1, 1, 1, 4};
		assertEquals(7, InvSlotTransferMath.totalSlots(sizes));

		assertEquals(0, InvSlotTransferMath.unpackGroup(InvSlotTransferMath.locateCombinedIndex(0, sizes)));
		assertEquals(0, InvSlotTransferMath.unpackLocal(InvSlotTransferMath.locateCombinedIndex(0, sizes)));

		assertEquals(1, InvSlotTransferMath.unpackGroup(InvSlotTransferMath.locateCombinedIndex(1, sizes)));
		assertEquals(0, InvSlotTransferMath.unpackLocal(InvSlotTransferMath.locateCombinedIndex(1, sizes)));

		assertEquals(2, InvSlotTransferMath.unpackGroup(InvSlotTransferMath.locateCombinedIndex(2, sizes)));
		assertEquals(3, InvSlotTransferMath.unpackGroup(InvSlotTransferMath.locateCombinedIndex(3, sizes)));
		assertEquals(0, InvSlotTransferMath.unpackLocal(InvSlotTransferMath.locateCombinedIndex(3, sizes)));
		assertEquals(3, InvSlotTransferMath.unpackLocal(InvSlotTransferMath.locateCombinedIndex(6, sizes)));

		assertEquals(-1, InvSlotTransferMath.locateCombinedIndex(7, sizes));
		assertEquals(-1, InvSlotTransferMath.locateCombinedIndex(-1, sizes));
	}

	@Test
	void totalSlots_rejects_negative()
	{
		assertThrows(IllegalArgumentException.class, () -> InvSlotTransferMath.totalSlots(1, -1));
	}

	@Test
	void remaining_helpers_are_clamped()
	{
		assertEquals(0, InvSlotTransferMath.remainingAfterInsert(0, 5));
		assertEquals(5, InvSlotTransferMath.remainingAfterInsert(5, 0));
		assertEquals(0, InvSlotTransferMath.remainingAfterExtract(0, 3));
		assertEquals(4, InvSlotTransferMath.remainingAfterExtract(4, 0));
	}
}
