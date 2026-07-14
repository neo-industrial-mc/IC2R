package me.halfcooler.ic2r.inv;

import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotTransferMath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure logic tests for InvSlot → IItemHandler transfer rules (W2.1 / G2.1).
 * Does not construct ItemStacks / handlers (no Minecraft bootstrap).
 * <p>
 * Mirrors {@code InvSlotItemHandler} insert/extract gating and residual math so
 * domain InvSlot access stays consistent with the Forge adapter. Cap attachment
 * and the handler class body remain runtime-only — see
 * {@code docs/spec/item_handler_contract.md}.
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

	// --- combined multi-slot index layout (standard machine construction order) ---

	@Test
	void combined_index_layout_matches_macerator_construction_order()
	{
		// TileEntityElectricMachine.discharge(1) → StandardMachine.output(1) → upgrade(4)
		// → Macerator.input(1). Registration order = combined ITEM_HANDLER null-facing order.
		int[] sizes = maceratorSlotSizes();
		assertEquals(7, InvSlotTransferMath.totalSlots(sizes));

		// discharge @ 0
		assertEquals(0, InvSlotTransferMath.unpackGroup(InvSlotTransferMath.locateCombinedIndex(0, sizes)));
		assertEquals(0, InvSlotTransferMath.unpackLocal(InvSlotTransferMath.locateCombinedIndex(0, sizes)));

		// output @ 1
		assertEquals(1, InvSlotTransferMath.unpackGroup(InvSlotTransferMath.locateCombinedIndex(1, sizes)));
		assertEquals(0, InvSlotTransferMath.unpackLocal(InvSlotTransferMath.locateCombinedIndex(1, sizes)));

		// upgrade @ 2..5
		assertEquals(2, InvSlotTransferMath.unpackGroup(InvSlotTransferMath.locateCombinedIndex(2, sizes)));
		assertEquals(0, InvSlotTransferMath.unpackLocal(InvSlotTransferMath.locateCombinedIndex(2, sizes)));
		assertEquals(2, InvSlotTransferMath.unpackGroup(InvSlotTransferMath.locateCombinedIndex(5, sizes)));
		assertEquals(3, InvSlotTransferMath.unpackLocal(InvSlotTransferMath.locateCombinedIndex(5, sizes)));

		// input @ 6
		assertEquals(3, InvSlotTransferMath.unpackGroup(InvSlotTransferMath.locateCombinedIndex(6, sizes)));
		assertEquals(0, InvSlotTransferMath.unpackLocal(InvSlotTransferMath.locateCombinedIndex(6, sizes)));

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

	// --- G2.1: pipeline sequences / access matrix / simulate / virtual handler mirror ---

	/**
	 * Macerator-style Access per combined index (null-facing view).
	 * discharge NONE, output O, upgrade×4 NONE, input I.
	 */
	@Test
	void macerator_combined_access_matrix_gates_insert_and_extract()
	{
		InvSlot.Access[] accessByCombined = maceratorAccessByCombinedIndex();
		assertEquals(7, accessByCombined.length);

		for (int i = 0; i < accessByCombined.length; i++)
		{
			InvSlot.Access access = accessByCombined[i];
			boolean accepts = access == InvSlot.Access.I; // output accepts=false; others N/A for insert gate
			boolean canIn = access.isInput();
			boolean canOut = access.isOutput();

			if (i == 6)
			{
				// input
				assertTrue(InvSlotTransferMath.allowsInsert(canIn, accepts, false));
				assertFalse(InvSlotTransferMath.allowsExtract(canOut, false));
			} else if (i == 1)
			{
				// output: Access.O + accepts false
				assertFalse(InvSlotTransferMath.allowsInsert(canIn, false, false));
				assertTrue(InvSlotTransferMath.allowsExtract(canOut, false));
			} else
			{
				// discharge + upgrade: NONE
				assertFalse(InvSlotTransferMath.allowsInsert(canIn, true, false));
				assertFalse(InvSlotTransferMath.allowsExtract(canOut, false));
			}
		}
	}

	/**
	 * Pipe-style sequence: insert into input-only virtual slot, reject extract from input,
	 * machine "process" moves count to output-only slot, then extract from output; re-insert
	 * into output rejected.
	 */
	@Test
	void pipeline_insert_process_extract_sequence_respects_access()
	{
		VirtualSlot input = VirtualSlot.inputOnly(64, 64);
		VirtualSlot output = VirtualSlot.outputOnly(64, 64);

		// hopper/pipe inserts ore into input
		assertEquals(0, input.insert(16, true, false));
		assertEquals(16, input.count);

		// cannot pull feedstock from input
		assertEquals(0, input.extract(8, false));
		assertEquals(16, input.count);

		// cannot push into output via automation
		assertEquals(10, output.insert(10, true, false));
		assertEquals(0, output.count);

		// machine internal process (domain path, not automation insert)
		int consumed = 16;
		input.count = InvSlotTransferMath.remainingAfterExtract(input.count, consumed);
		output.count = 16; // product placed by InvSlotOutput#add
		assertEquals(0, input.count);
		assertEquals(16, output.count);

		// pipe extracts product
		assertEquals(16, output.extract(64, false));
		assertEquals(0, output.count);
	}

	/**
	 * {@code simulate=true} must not mutate slot counts (mirrors InvSlotItemHandler branches).
	 */
	@Test
	void simulate_insert_and_extract_leave_virtual_state_unchanged()
	{
		VirtualSlot io = VirtualSlot.bidirectional(64, 64);
		assertEquals(0, io.insert(20, true, false));
		assertEquals(20, io.count);

		int remainingSim = io.insert(50, true, true);
		// space 44 → remaining 6; count stays 20
		assertEquals(6, remainingSim);
		assertEquals(20, io.count);

		int extractedSim = io.extract(7, true);
		assertEquals(7, extractedSim);
		assertEquals(20, io.count);

		// commit extract
		assertEquals(7, io.extract(7, false));
		assertEquals(13, io.count);

		// commit partial insert into remaining space
		assertEquals(0, io.insert(5, true, false));
		assertEquals(18, io.count);
	}

	/**
	 * Combined-layout pipeline: only input index accepts insert; only output index yields extract;
	 * upgrade/discharge indices stay inert across a multi-step sequence.
	 */
	@Test
	void combined_layout_pipeline_only_input_and_output_move_items()
	{
		int[] sizes = maceratorSlotSizes();
		VirtualSlot[] slots = new VirtualSlot[InvSlotTransferMath.totalSlots(sizes)];
		InvSlot.Access[] access = maceratorAccessByCombinedIndex();
		for (int i = 0; i < slots.length; i++)
		{
			slots[i] = VirtualSlot.fromAccess(access[i], 64, 64);
		}

		// try insert 8 into every combined index; only input (6) accepts
		for (int i = 0; i < slots.length; i++)
		{
			boolean accepts = i == 6;
			int remaining = slots[i].insert(8, accepts, false);
			if (i == 6)
			{
				assertEquals(0, remaining);
				assertEquals(8, slots[i].count);
			} else
			{
				assertEquals(8, remaining);
				assertEquals(0, slots[i].count);
			}
		}

		// process: clear input, fill output
		slots[6].count = 0;
		slots[1].count = 8;

		// extract 8 from every index; only output yields
		for (int i = 0; i < slots.length; i++)
		{
			int got = slots[i].extract(8, false);
			if (i == 1)
			{
				assertEquals(8, got);
				assertEquals(0, slots[i].count);
			} else
			{
				assertEquals(0, got);
			}
		}

		// upgrade band still empty
		for (int i = 2; i <= 5; i++)
		{
			assertEquals(0, slots[i].count);
		}
		assertEquals(0, slots[0].count);
	}

	/**
	 * Partial insert remainder + subsequent extract of produced goods (round-trip residuals).
	 */
	@Test
	void partial_insert_remainder_then_extract_round_trip()
	{
		VirtualSlot input = VirtualSlot.inputOnly(32, 64); // slotLimit 32
		VirtualSlot output = VirtualSlot.outputOnly(64, 64);

		// offer 50 into limit-32 input → insert 32, remainder 18
		assertEquals(18, input.insert(50, true, false));
		assertEquals(32, input.count);

		// simulate full extract from input would be blocked by Access.I
		assertEquals(0, input.extract(32, true));
		assertEquals(32, input.count);

		// process one batch of 16
		input.count = InvSlotTransferMath.remainingAfterExtract(input.count, 16);
		output.count += 16;
		assertEquals(16, input.count);

		assertEquals(10, output.extract(10, false));
		assertEquals(6, output.count);
		assertEquals(6, output.extract(99, false));
		assertEquals(0, output.count);
	}

	// --- helpers ---

	/** discharge(1) + output(1) + upgrade(4) + input(1). */
	private static int[] maceratorSlotSizes()
	{
		return new int[] {1, 1, 4, 1};
	}

	private static InvSlot.Access[] maceratorAccessByCombinedIndex()
	{
		return new InvSlot.Access[] {
			InvSlot.Access.NONE, // discharge
			InvSlot.Access.O, // output
			InvSlot.Access.NONE,
			InvSlot.Access.NONE,
			InvSlot.Access.NONE,
			InvSlot.Access.NONE, // upgrade ×4
			InvSlot.Access.I // input
		};
	}

	/**
	 * Count-only mirror of {@code InvSlotItemHandler} insert/extract residual rules.
	 * Assumes a single item identity (stacks always compatible when non-empty).
	 */
	static final class VirtualSlot
	{
		int count;
		final boolean canInput;
		final boolean canOutput;
		final int slotLimit;
		final int maxStack;

		VirtualSlot(boolean canInput, boolean canOutput, int slotLimit, int maxStack)
		{
			this.canInput = canInput;
			this.canOutput = canOutput;
			this.slotLimit = slotLimit;
			this.maxStack = maxStack;
		}

		static VirtualSlot inputOnly(int slotLimit, int maxStack)
		{
			return new VirtualSlot(true, false, slotLimit, maxStack);
		}

		static VirtualSlot outputOnly(int slotLimit, int maxStack)
		{
			return new VirtualSlot(false, true, slotLimit, maxStack);
		}

		static VirtualSlot bidirectional(int slotLimit, int maxStack)
		{
			return new VirtualSlot(true, true, slotLimit, maxStack);
		}

		static VirtualSlot fromAccess(InvSlot.Access access, int slotLimit, int maxStack)
		{
			return new VirtualSlot(access.isInput(), access.isOutput(), slotLimit, maxStack);
		}

		/** @return remaining incoming count (like insertItem return stack size). */
		int insert(int incoming, boolean accepts, boolean simulate)
		{
			if (!InvSlotTransferMath.allowsInsert(this.canInput, accepts, incoming <= 0))
			{
				return Math.max(0, incoming);
			}

			int insertable = InvSlotTransferMath.insertableCount(
				this.count,
				incoming,
				this.slotLimit,
				this.maxStack,
				true
			);
			if (!simulate)
			{
				this.count += insertable;
			}

			return InvSlotTransferMath.remainingAfterInsert(incoming, insertable);
		}

		/** @return extracted amount (like extractItem stack size). */
		int extract(int request, boolean simulate)
		{
			if (!InvSlotTransferMath.allowsExtract(this.canOutput, this.count <= 0))
			{
				return 0;
			}

			int extractable = InvSlotTransferMath.extractableCount(this.count, request, this.maxStack);
			if (!simulate)
			{
				this.count = InvSlotTransferMath.remainingAfterExtract(this.count, extractable);
			}

			return extractable;
		}
	}
}
