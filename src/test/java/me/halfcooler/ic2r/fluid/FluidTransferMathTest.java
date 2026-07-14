package me.halfcooler.ic2r.fluid;

import me.halfcooler.ic2r.core.fluid.FluidTransferMath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure logic tests for tank fill/empty transfer rules (W2.2 / FL-001).
 * Does not construct FluidStacks / tanks (no Minecraft bootstrap).
 * <p>
 * Mirrors {@code Ic2rFluidTank} fillMb/drainMb residual math so domain tank
 * capacity semantics stay consistent with the platform fluid bridge.
 */
class FluidTransferMathTest
{
	// --- fillableMb ---

	@Test
	void fillable_into_empty_tank_is_min_offer_and_capacity()
	{
		assertEquals(1000, FluidTransferMath.fillableMb(1000, 0, 1000, true, true));
		assertEquals(500, FluidTransferMath.fillableMb(1000, 0, 500, true, true));
		assertEquals(1000, FluidTransferMath.fillableMb(1000, 0, 2000, true, true));
	}

	@Test
	void fillable_respects_remaining_space()
	{
		// 800 stored of 1000 capacity → 200 free
		assertEquals(200, FluidTransferMath.fillableMb(1000, 800, 500, true, true));
		assertEquals(200, FluidTransferMath.fillableMb(1000, 800, 200, true, true));
		assertEquals(0, FluidTransferMath.fillableMb(1000, 1000, 100, true, true));
		assertEquals(0, FluidTransferMath.fillableMb(1000, 1200, 100, true, true));
	}

	@Test
	void fillable_gated_by_compatibility_and_canFill()
	{
		assertEquals(0, FluidTransferMath.fillableMb(1000, 0, 500, false, true));
		assertEquals(0, FluidTransferMath.fillableMb(1000, 0, 500, true, false));
		assertEquals(0, FluidTransferMath.fillableMb(0, 0, 500, true, true));
		assertEquals(0, FluidTransferMath.fillableMb(1000, 0, 0, true, true));
		assertEquals(0, FluidTransferMath.fillableMb(1000, 0, -1, true, true));
	}

	@Test
	void remaining_offer_after_partial_fill()
	{
		int offer = 1000;
		int filled = FluidTransferMath.fillableMb(500, 0, offer, true, true);
		assertEquals(500, filled);
		assertEquals(500, FluidTransferMath.remainingOfferAfterFill(offer, filled));

		assertEquals(0, FluidTransferMath.remainingOfferAfterFill(200, 200));
		assertEquals(0, FluidTransferMath.remainingOfferAfterFill(0, 5));
		assertEquals(5, FluidTransferMath.remainingOfferAfterFill(5, 0));
	}

	// --- drainableMb ---

	@Test
	void drainable_is_min_stored_and_request()
	{
		assertEquals(500, FluidTransferMath.drainableMb(1000, 500, true));
		assertEquals(1000, FluidTransferMath.drainableMb(1000, 2000, true));
		assertEquals(1, FluidTransferMath.drainableMb(1, 1, true));
	}

	@Test
	void drainable_gated_by_empty_request_and_canDrain()
	{
		assertEquals(0, FluidTransferMath.drainableMb(0, 500, true));
		assertEquals(0, FluidTransferMath.drainableMb(1000, 0, true));
		assertEquals(0, FluidTransferMath.drainableMb(1000, 500, false));
		assertEquals(0, FluidTransferMath.drainableMb(-1, 500, true));
	}

	@Test
	void remaining_stored_after_partial_and_full_drain()
	{
		int stored = 1000;
		int drained = FluidTransferMath.drainableMb(stored, 300, true);
		assertEquals(300, drained);
		assertEquals(700, FluidTransferMath.remainingStoredAfterDrain(stored, drained));

		drained = FluidTransferMath.drainableMb(stored, 9999, true);
		assertEquals(1000, drained);
		assertEquals(0, FluidTransferMath.remainingStoredAfterDrain(stored, drained));

		assertEquals(0, FluidTransferMath.remainingStoredAfterDrain(0, 3));
		assertEquals(4, FluidTransferMath.remainingStoredAfterDrain(4, 0));
	}

	/**
	 * FL-001 style path: simulate fill into partial tank, then drain what was stored.
	 */
	@Test
	void fill_then_drain_path_preserves_capacity_math()
	{
		final int capacity = 16000;
		int stored = 15000;
		int offer = 2000;

		int filled = FluidTransferMath.fillableMb(capacity, stored, offer, true, true);
		assertEquals(1000, filled);
		assertEquals(1000, FluidTransferMath.remainingOfferAfterFill(offer, filled));

		stored += filled; // 16000 full
		assertEquals(0, FluidTransferMath.fillableMb(capacity, stored, 1, true, true));

		int drained = FluidTransferMath.drainableMb(stored, 500, true);
		assertEquals(500, drained);
		stored = FluidTransferMath.remainingStoredAfterDrain(stored, drained);
		assertEquals(15500, stored);

		// empty gate after full drain
		drained = FluidTransferMath.drainableMb(stored, Integer.MAX_VALUE, true);
		assertEquals(15500, drained);
		assertEquals(0, FluidTransferMath.remainingStoredAfterDrain(stored, drained));
	}

	// --- G2.4: Ic2rFluidTank fill/drain delegate gates (compat / external / simulate) ---

	/** Empty tank accepts any fluid; non-empty requires same fluid. */
	@Test
	void tank_fluids_compatible_empty_or_same()
	{
		assertTrue(FluidTransferMath.tankFluidsCompatible(true, false));
		assertTrue(FluidTransferMath.tankFluidsCompatible(true, true));
		assertTrue(FluidTransferMath.tankFluidsCompatible(false, true));
		assertFalse(FluidTransferMath.tankFluidsCompatible(false, false));
	}

	/**
	 * External fill respects canFill; unchecked (external=false) bypasses canFill;
	 * empty offer always rejected.
	 */
	@Test
	void fill_access_external_vs_unchecked_and_empty_offer()
	{
		assertFalse(FluidTransferMath.fillAccessAllowed(true, true, true));
		assertFalse(FluidTransferMath.fillAccessAllowed(true, false, true));

		// external + canFill
		assertTrue(FluidTransferMath.fillAccessAllowed(false, true, true));
		assertFalse(FluidTransferMath.fillAccessAllowed(false, true, false));

		// unchecked bypasses canFill=false
		assertTrue(FluidTransferMath.fillAccessAllowed(false, false, false));
	}

	/**
	 * Full tank / incompatible / external-deny → 0; empty tank fill; partial into free space.
	 * Mirrors Ic2rFluidTank.fillMbDelegated path.
	 */
	@Test
	void fillMbDelegated_full_incompatible_deny_and_partial()
	{
		// full tank
		assertEquals(0, FluidTransferMath.fillMbDelegated(
			1000, 1000, 500, false, true, false, true, true
		));
		// incompatible fluids
		assertEquals(0, FluidTransferMath.fillMbDelegated(
			1000, 200, 500, false, false, false, true, true
		));
		// external canFill deny
		assertEquals(0, FluidTransferMath.fillMbDelegated(
			1000, 0, 500, true, false, false, true, false
		));
		// empty tank accepts full offer up to capacity
		assertEquals(500, FluidTransferMath.fillMbDelegated(
			1000, 0, 500, true, false, false, true, true
		));
		// partial free space 300
		assertEquals(300, FluidTransferMath.fillMbDelegated(
			1000, 700, 500, false, true, false, true, true
		));
		// unchecked fill with canFill=false still works
		assertEquals(100, FluidTransferMath.fillMbDelegated(
			1000, 0, 100, true, false, false, false, false
		));
	}

	/**
	 * Amount drain: external canDrain gate; empty tank; unchecked bypass.
	 * Stack drain: wrong fluid / empty request rejected; match allows.
	 */
	@Test
	void drain_delegated_amount_and_stack_match_gates()
	{
		// amount drain external deny
		assertEquals(0, FluidTransferMath.drainMbDelegated(1000, 200, true, false));
		// amount drain empty tank
		assertEquals(0, FluidTransferMath.drainMbDelegated(0, 200, true, true));
		// amount drain ok / unchecked bypass
		assertEquals(200, FluidTransferMath.drainMbDelegated(1000, 200, true, true));
		assertEquals(200, FluidTransferMath.drainMbDelegated(1000, 200, false, false));

		// stack drain wrong fluid
		assertEquals(0, FluidTransferMath.drainMbByStackDelegated(
			1000, 200, false, false, false, true, true
		));
		// stack drain empty request
		assertEquals(0, FluidTransferMath.drainMbByStackDelegated(
			1000, 200, false, true, true, true, true
		));
		// stack drain empty tank
		assertEquals(0, FluidTransferMath.drainMbByStackDelegated(
			0, 200, true, false, true, true, true
		));
		// stack match + partial / full
		assertEquals(200, FluidTransferMath.drainMbByStackDelegated(
			1000, 200, false, false, true, true, true
		));
		assertEquals(1000, FluidTransferMath.drainMbByStackDelegated(
			1000, 9999, false, false, true, true, true
		));
	}

	/**
	 * Simulate must not change stored; commit fill/drain updates; full drain → 0.
	 */
	@Test
	void simulate_vs_commit_stored_after_fill_and_drain()
	{
		int stored = 400;
		assertEquals(400, FluidTransferMath.storedAfterFill(stored, 100, true));
		assertEquals(500, FluidTransferMath.storedAfterFill(stored, 100, false));
		assertEquals(400, FluidTransferMath.storedAfterFill(stored, 0, false));

		assertEquals(400, FluidTransferMath.storedAfterDrain(stored, 150, true));
		assertEquals(250, FluidTransferMath.storedAfterDrain(stored, 150, false));
		assertEquals(0, FluidTransferMath.storedAfterDrain(stored, 400, false));
		assertEquals(0, FluidTransferMath.storedAfterDrain(stored, 999, false));
	}
}
