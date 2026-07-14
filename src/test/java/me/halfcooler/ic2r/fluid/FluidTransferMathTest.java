package me.halfcooler.ic2r.fluid;

import me.halfcooler.ic2r.core.fluid.FluidTransferMath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
