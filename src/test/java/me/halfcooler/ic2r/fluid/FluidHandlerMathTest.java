package me.halfcooler.ic2r.fluid;

import me.halfcooler.ic2r.core.fluid.FluidTransferMath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure logic tests for {@code Ic2rFluidTank} → {@code IFluidHandler} transfer rules (G2.5 / FL-*).
 * Does not construct FluidStacks / handlers (no Minecraft bootstrap).
 * <p>
 * Mirrors {@code Ic2rFluidTankHandler} fill/drain gating and residual math so domain tank
 * access stays consistent with the Forge adapter. Cap attachment and the handler class body
 * remain runtime-only — see {@code docs/spec/fluid_handler_contract.md}.
 */
class FluidHandlerMathTest
{
	// --- FL-001 / FL-002: empty resource early-exit (handler surface) ---

	/**
	 * Handler fill/drain with empty or non-positive offer/request returns 0 without state change.
	 */
	@Test
	void empty_resource_fill_and_drain_are_zero()
	{
		VirtualTank tank = VirtualTank.empty(1000);

		assertEquals(0, tank.fill(0, false));
		assertEquals(0, tank.fill(-1, false));
		assertEquals(0, tank.stored);

		assertEquals(0, tank.drainAmount(0, false));
		assertEquals(0, tank.drainAmount(-5, false));
		assertEquals(0, tank.drainByStack(0, true, false));
	}

	// --- FL-002: isFluidValid / canFill gate ---

	/**
	 * isFluidValid mirrors external canFill only (not free space).
	 * Full tank can still report fluid valid while fill returns 0.
	 */
	@Test
	void isFluidValid_mirrors_canFill_not_free_space()
	{
		VirtualTank open = VirtualTank.empty(1000);
		open.canFill = true;
		assertTrue(open.isFluidValid(false));

		open.canFill = false;
		assertFalse(open.isFluidValid(false));
		assertFalse(open.isFluidValid(true)); // empty offer always invalid

		VirtualTank full = VirtualTank.withStored(1000, 1000);
		full.canFill = true;
		assertTrue(full.isFluidValid(false));
		assertEquals(0, full.fill(100, false));
		assertEquals(1000, full.stored);
	}

	// --- fill residual + simulate ---

	/**
	 * Partial fill leaves residual offer; SIMULATE must not commit stored.
	 */
	@Test
	void fill_partial_residual_and_simulate_no_commit()
	{
		VirtualTank tank = VirtualTank.withStored(1000, 700);

		int sim = tank.fill(500, true);
		assertEquals(300, sim);
		assertEquals(700, tank.stored);
		assertEquals(200, FluidTransferMath.remainingOfferAfterFill(500, sim));

		int exec = tank.fill(500, false);
		assertEquals(300, exec);
		assertEquals(1000, tank.stored);
		assertEquals(200, FluidTransferMath.remainingOfferAfterFill(500, exec));
		assertEquals(0, tank.fill(1, false));
	}

	// --- drain gates + stack match ---

	/**
	 * Amount drain gated by canDrain; stack drain requires fluid match.
	 */
	@Test
	void drain_amount_and_stack_match_gates()
	{
		VirtualTank tank = VirtualTank.withStored(1000, 800);

		tank.canDrain = false;
		assertEquals(0, tank.drainAmount(200, false));
		assertEquals(800, tank.stored);

		tank.canDrain = true;
		assertEquals(200, tank.drainAmount(200, true));
		assertEquals(800, tank.stored);
		assertEquals(200, tank.drainAmount(200, false));
		assertEquals(600, tank.stored);

		// wrong fluid on stack drain
		assertEquals(0, tank.drainByStack(100, false, false));
		assertEquals(600, tank.stored);
		// match
		assertEquals(100, tank.drainByStack(100, true, false));
		assertEquals(500, tank.stored);
	}

	// --- FL-003 style pipe sequence ---

	/**
	 * Pipe-style sequence: fill input → (machine keeps fluid) → drain extract.
	 * Mirrors automation using IFluidHandler fill then drain on same tank (IO access).
	 */
	@Test
	void pipe_sequence_fill_then_drain_on_io_tank()
	{
		VirtualTank tank = VirtualTank.empty(8000);
		tank.canFill = true;
		tank.canDrain = true;

		int filled = tank.fill(1000, false);
		assertEquals(1000, filled);
		assertEquals(1000, tank.stored);
		assertFalse(tank.empty);

		// second fill same fluid partial into free space
		filled = tank.fill(7500, false);
		assertEquals(7000, filled);
		assertEquals(8000, tank.stored);

		int drained = tank.drainAmount(500, false);
		assertEquals(500, drained);
		assertEquals(7500, tank.stored);

		drained = tank.drainByStack(9000, true, false);
		assertEquals(7500, drained);
		assertEquals(0, tank.stored);
		assertTrue(tank.empty);
	}

	/**
	 * Input-only tank (insert ok, extract blocked) like Fluids.addTankInsert external path.
	 */
	@Test
	void input_only_tank_blocks_external_drain()
	{
		VirtualTank input = VirtualTank.withStored(1000, 500);
		input.canFill = true;
		input.canDrain = false; // Access.I / extract sides empty at component layer

		assertEquals(200, input.fill(200, false));
		assertEquals(700, input.stored);
		assertEquals(0, input.drainAmount(100, false));
		assertEquals(700, input.stored);
	}

	/**
	 * Output-only tank (drain ok, fill blocked) like Fluids.addTankExtract.
	 */
	@Test
	void output_only_tank_blocks_external_fill()
	{
		VirtualTank output = VirtualTank.withStored(1000, 400);
		output.canFill = false;
		output.canDrain = true;

		assertEquals(0, output.fill(100, false));
		assertEquals(400, output.stored);
		assertEquals(150, output.drainAmount(150, false));
		assertEquals(250, output.stored);
	}

	// -------------------------------------------------------------------------

	/**
	 * Count-only mirror of {@code Ic2rFluidTankHandler} + {@code Ic2rFluidTank} external path.
	 * No Fluid / FluidStack types.
	 */
	static final class VirtualTank
	{
		final int capacity;
		int stored;
		boolean empty;
		boolean sameFluid = true;
		boolean canFill = true;
		boolean canDrain = true;

		private VirtualTank(int capacity, int stored)
		{
			this.capacity = capacity;
			this.stored = stored;
			this.empty = stored <= 0;
		}

		static VirtualTank empty(int capacity)
		{
			return new VirtualTank(capacity, 0);
		}

		static VirtualTank withStored(int capacity, int stored)
		{
			return new VirtualTank(capacity, stored);
		}

		/** Mirrors {@code isFluidValid}: offerEmpty + canFill only. */
		boolean isFluidValid(boolean offerEmpty)
		{
			return FluidTransferMath.fillAccessAllowed(offerEmpty, true, this.canFill);
		}

		int fill(int offerMb, boolean simulate)
		{
			int filled = FluidTransferMath.fillMbDelegated(
				this.capacity,
				this.stored,
				offerMb,
				this.empty,
				this.sameFluid,
				offerMb <= 0,
				true,
				this.canFill
			);
			if (!simulate && filled > 0)
			{
				this.stored = FluidTransferMath.storedAfterFill(this.stored, filled, false);
				this.empty = this.stored <= 0;
				this.sameFluid = true;
			}
			return filled;
		}

		int drainAmount(int requestMb, boolean simulate)
		{
			int drained = FluidTransferMath.drainMbDelegated(
				this.stored,
				requestMb,
				true,
				this.canDrain
			);
			if (!simulate && drained > 0)
			{
				this.stored = FluidTransferMath.storedAfterDrain(this.stored, drained, false);
				this.empty = this.stored <= 0;
			}
			return drained;
		}

		int drainByStack(int requestMb, boolean sameFluid, boolean simulate)
		{
			int drained = FluidTransferMath.drainMbByStackDelegated(
				this.stored,
				requestMb,
				this.empty,
				requestMb <= 0,
				sameFluid,
				true,
				this.canDrain
			);
			if (!simulate && drained > 0)
			{
				this.stored = FluidTransferMath.storedAfterDrain(this.stored, drained, false);
				this.empty = this.stored <= 0;
			}
			return drained;
		}
	}
}
