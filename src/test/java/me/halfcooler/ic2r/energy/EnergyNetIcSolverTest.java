package me.halfcooler.ic2r.energy;

import me.halfcooler.ic2r.api.energy.profile.VoltageTier;
import me.halfcooler.ic2r.core.block.wiring.CableType;
import me.halfcooler.ic2r.core.energy.grid.EnergyTransferMath;
import me.halfcooler.ic2r.core.energy.profile.CableSpec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Host-boundary tests for the IC energy-net solver contract.
 * <p>
 * Uses fake topology data / pure math — no {@code Level} or {@code BlockEntity} dependency.
 * Intended to complement {@link EnergyTransferMathTest} (pure predicates) with
 * solver-level scenarios: multi-path topology selection, heterogeneous sink distribution,
 * and IC-vs-GT cross-mode comparison (EN-GT-010).
 *
 * <h3>Spec coverage</h3>
 * <ul>
 *   <li>EN-IC-002 — multi-path topology selection (deepened from pure predicate)</li>
 *   <li>EN-IC-004/005 — multi-sink distribution with realistic loss patterns (deepened)</li>
 *   <li>EN-GT-010 — same topology, IC vs GT math diverge per invariants</li>
 * </ul>
 */
class EnergyNetIcSolverTest
{
	// ---- EN-IC-002: Multi-path topology selection (deepened beyond icPreferNewPath) ----

	/**
	 * @Spec EN-IC-002
	 * <b>Given</b> a sink reachable via 3 candidate paths with losses [5, 2, 7],
	 * <b>When</b> BFS discovery evaluates each candidate against the current best,
	 * <b>Then</b> the path with strictly lowest loss (2) is retained.
	 */
	@Test
	void pathSelection_lowestLossWins_acrossMultipleCandidates()
	{
		double[] candidateLosses = {5.0, 2.0, 7.0};
		double bestLoss = Double.POSITIVE_INFINITY;
		for (double loss : candidateLosses)
		{
			if (EnergyTransferMath.icPreferNewPath(bestLoss, loss))
			{
				bestLoss = loss;
			}
		}
		assertEquals(2.0, bestLoss);

		// Equal loss: first-discovered is kept (no replacement)
		double kept = 3.0;
		assertFalse(EnergyTransferMath.icPreferNewPath(kept, 3.0));
		assertFalse(EnergyTransferMath.icPreferNewPath(kept, 4.0));
	}

	/**
	 * @Spec EN-IC-002
	 * <b>Given</b> a source→sink topology with two parallel conductor chains:
	 * chain-A total loss 3, chain-B total loss 1,
	 * <b>When</b> both paths are discovered and compared,
	 * <b>Then</b> chain-B (lower cumulative loss) is preferred and delivers more EU.
	 */
	@Test
	void pathSelection_parallelChains_lowerCumulativeLossPreferred()
	{
		double pathALoss = 1.0 + 2.0;    // source→condA(1)→sink(2)
		double pathBLoss = 0.5 + 0.5;    // source→condB(0.5)→sink(0.5)

		double bestLoss = Double.POSITIVE_INFINITY;
		if (EnergyTransferMath.icPreferNewPath(bestLoss, pathALoss)) bestLoss = pathALoss;
		if (EnergyTransferMath.icPreferNewPath(bestLoss, pathBLoss)) bestLoss = pathBLoss;

		assertEquals(1.0, bestLoss);
		double offer = 100.0;
		// Path A: 100-3=97; Path B: 100-1=99
		assertEquals(97.0, EnergyTransferMath.icInjectAmount(offer, pathALoss));
		assertEquals(99.0, EnergyTransferMath.icInjectAmount(offer, pathBLoss));
		assertTrue(EnergyTransferMath.icInjectAmount(offer, pathBLoss)
			> EnergyTransferMath.icInjectAmount(offer, pathALoss));
	}

	/**
	 * @Spec EN-IC-002
	 * <b>Given</b> three candidate paths all with loss ≥ source offer (all dead),
	 * <b>When</b> path selection runs and then inject is computed with the best (least-dead) path,
	 * <b>Then</b> inject amount is 0 because the best loss still exhausts the offer.
	 */
	@Test
	void pathSelection_allPathsDead_noDelivery()
	{
		double[] deadLosses = {200.0, 150.0, 300.0};
		double offer = 100.0;

		double bestLoss = Double.POSITIVE_INFINITY;
		for (double loss : deadLosses)
		{
			if (EnergyTransferMath.icPreferNewPath(bestLoss, loss))
			{
				bestLoss = loss;
			}
		}
		assertEquals(150.0, bestLoss);
		assertEquals(0.0, EnergyTransferMath.icInjectAmount(offer, bestLoss));
	}

	/**
	 * @Spec EN-IC-002
	 * <b>Given</b> 20 random candidate paths with losses in [0,50],
	 * <b>When</b> path selection iterates the full set,
	 * <b>Then</b> the retained path has the global minimum loss.
	 */
	@Test
	void pathSelection_manyCandidates_findsGlobalMinimum()
	{
		java.util.Random rng = new java.util.Random(42);
		double[] losses = new double[20];
		for (int i = 0; i < losses.length; i++)
		{
			losses[i] = rng.nextDouble() * 50.0;
		}

		double bestLoss = Double.POSITIVE_INFINITY;
		for (double loss : losses)
		{
			if (EnergyTransferMath.icPreferNewPath(bestLoss, loss))
			{
				bestLoss = loss;
			}
		}

		double expectedMin = Double.POSITIVE_INFINITY;
		for (double loss : losses)
		{
			if (loss < expectedMin) expectedMin = loss;
		}
		assertEquals(expectedMin, bestLoss);
	}

	// ---- EN-IC-004 / EN-IC-005: Multi-sink distribution deepened ----

	/**
	 * @Spec EN-IC-004
	 * <b>Given</b> one source (200 EU) feeding 4 sinks with heterogeneous path losses
	 * [0, 5, 20, 50] and demands [60, 80, 100, 200],
	 * <b>When</b> {@code icDistributeSequential} runs in fixed order,
	 * <b>Then</b> total delivered (175) ≤ offer (200), and each sink receives
	 * min(injectAmount, demand) with earlier-priority fill.
	 */
	@Test
	void multiSink_heterogeneousLosses_totalDeliveredBounded()
	{
		double offer = 200.0;
		double[] losses = {0.0, 5.0, 20.0, 50.0};
		double[] demands = {60.0, 80.0, 100.0, 200.0};
		double[] delivered = EnergyTransferMath.icDistributeSequential(offer, losses, demands);

		// Step-by-step:
		// Sink[0]: inject 200, demand 60 → 60, source consumed 60 (0 loss)
		// Sink[1]: remaining 140, inject 135, demand 80 → 80, source consumed 85
		// Sink[2]: remaining 55, inject 35, demand 100 → 35, source consumed 55
		// Sink[3]: remaining 0 → 0
		assertArrayEquals(new double[]{60.0, 80.0, 35.0, 0.0}, delivered, 1e-9);

		double sum = 0.0;
		for (double d : delivered) sum += d;
		assertTrue(sum <= offer);
		assertEquals(175.0, sum, 1e-9);
	}

	/**
	 * @Spec EN-IC-005
	 * <b>Given</b> two sinks with identical (zero) path loss but demands [100, 100],
	 * <b>When</b> source offers only 50 EU,
	 * <b>Then</b> first sink consumes all 50; second gets 0 (earlier-path priority).
	 */
	@Test
	void multiSink_earlierPriority_starvesLaterWhenOfferInsufficient()
	{
		double[] delivered = EnergyTransferMath.icDistributeSequential(
			50.0,
			new double[]{0.0, 0.0},
			new double[]{100.0, 100.0});
		assertArrayEquals(new double[]{50.0, 0.0}, delivered, 1e-9);

		// Reorder: first sink only needs 30, rest goes to second
		double[] delivered2 = EnergyTransferMath.icDistributeSequential(
			50.0,
			new double[]{0.0, 0.0},
			new double[]{30.0, 100.0});
		assertArrayEquals(new double[]{30.0, 20.0}, delivered2, 1e-9);
	}

	/**
	 * @Spec EN-IC-004 / EN-IC-005
	 * <b>Given</b> 5 sinks with identical demands but increasing path losses,
	 * <b>When</b> a large source offer flows through sequential distribution,
	 * <b>Then</b> early sinks with low loss fill completely; later high-loss sinks
	 * may be starved despite sufficient total offer (earlier sinks drain source faster
	 * due to lower source-consumed cost).
	 */
	@Test
	void multiSink_lossGradient_earlySinksDrainSourceFasterPerDeliveredEu()
	{
		double offer = 500.0;
		double[] losses = {0.0, 5.0, 10.0, 20.0, 50.0};
		double[] demands = {100.0, 100.0, 100.0, 100.0, 100.0};
		double[] delivered = EnergyTransferMath.icDistributeSequential(offer, losses, demands);

		// Sink[0]: inject 500, want 100 → takes 100, source pays 100 → remaining 400
		// Sink[1]: inject 395, want 100 → takes 100, source pays 105 → remaining 295
		// Sink[2]: inject 285, want 100 → takes 100, source pays 110 → remaining 185
		// Sink[3]: inject 165, want 100 → takes 100, source pays 120 → remaining 65
		// Sink[4]: inject 15, want 100 → takes 15, source pays 65 → remaining 0
		assertEquals(100.0, delivered[0], 1e-9);
		assertEquals(100.0, delivered[1], 1e-9);
		assertEquals(100.0, delivered[2], 1e-9);
		assertEquals(100.0, delivered[3], 1e-9);
		assertEquals(15.0, delivered[4], 1e-9);

		double sum = 0.0;
		for (double d : delivered) sum += d;
		assertTrue(sum <= offer);
	}

	// ---- EN-GT-010: IC vs GT cross-mode comparison ----

	/**
	 * @Spec EN-GT-010
	 * <b>Given</b> the same 100 EU source buffer at LV tier (32 V),
	 * <b>When</b> processed through IC math (continuous EU) vs GT math (discrete 1A packets),
	 * <b>Then</b> IC offers all 100 EU continuously; GT offers floor(100/32)=3 amps (96 EU).
	 * After 10 EU path loss: IC delivers 90 EU; GT delivers 3×22=66 EU
	 * (loss applied per-amp magnifies effect). Both are internally correct.
	 */
	@Test
	void icVsGt_sameBuffer_differentOfferGranularity()
	{
		double bufferEu = 100.0;
		int voltage = VoltageTier.LV.getVoltage(); // 32
		double pathLoss = 10.0;
		double sinkDemand = 200.0;

		// --- IC path ---
		double icDelivered = Math.min(
			EnergyTransferMath.icInjectAmount(bufferEu, pathLoss),
			sinkDemand);
		assertEquals(90.0, icDelivered);

		// --- GT path ---
		int gtOfferAmps = EnergyTransferMath.gtOfferAmps(bufferEu, voltage);
		assertEquals(3, gtOfferAmps);           // floor(100/32)
		double gtAvailable = (double) gtOfferAmps * voltage;
		assertEquals(96.0, gtAvailable);

		int tinLoss = CableSpec.forType(CableType.tin).getLossPerMeterPerAmp(); // =1
		int[] tenTinBlocks = {tinLoss, tinLoss, tinLoss, tinLoss, tinLoss,
			tinLoss, tinLoss, tinLoss, tinLoss, tinLoss};
		int survivingEu = EnergyTransferMath.gtPacketEuAfterPathLoss(voltage, tenTinBlocks);
		assertEquals(22, survivingEu);          // 32 - 10

		int deliverableAmps = EnergyTransferMath.gtDeliverableAmps(gtOfferAmps, survivingEu);
		assertEquals(3, deliverableAmps);
		double gtDelivered = (double) deliverableAmps * survivingEu;
		assertEquals(66.0, gtDelivered);

		// Both are correct per their respective invariants: divergence is expected
		assertNotEquals(icDelivered, gtDelivered, 1e-9);
		assertTrue(icDelivered > gtDelivered,
			"IC continuous-EU should deliver more than GT per-amp-loss for same topology");
	}

	/**
	 * @Spec EN-GT-010 / EN-GT-009
	 * <b>Given</b> a source buffer of 20 EU at LV tier (32 V) — less than 1 full amp,
	 * <b>When</b> IC and GT both evaluate the offer,
	 * <b>Then</b> IC still delivers 20 EU (continuous); GT correctly offers 0 amps
	 * (EN-GT-009: partial buffer below one amp → no output).
	 */
	@Test
	void icVsGt_bufferBelowOneAmp_gtOffersZero_icStillDelivers()
	{
		double bufferEu = 20.0;
		int voltage = VoltageTier.LV.getVoltage();
		double pathLoss = 0.0;

		// IC: delivers whatever is in buffer
		double icDelivered = EnergyTransferMath.icInjectAmount(bufferEu, pathLoss);
		assertEquals(20.0, icDelivered);

		// GT: partial buffer → no amp output (EN-GT-009)
		int gtOfferAmps = EnergyTransferMath.gtOfferAmps(bufferEu, voltage);
		assertEquals(0, gtOfferAmps);
		assertEquals(0, EnergyTransferMath.gtOfferAmps(31.9, voltage));
	}

	/**
	 * @Spec EN-GT-010
	 * <b>Given</b> a topology with heavy path loss (30) that nearly kills GT amps,
	 * <b>When</b> 200 EU @ LV feeds through both calculators,
	 * <b>Then</b> IC delivers 170 EU (loss subtracted once from total);
	 * GT delivers 12 EU (each of 6 amps loses 30 from its 32 EU → 2 EU survives per amp).
	 * The dramatic divergence is correct: GT loss is per-amp, IC loss is per-packet.
	 */
	@Test
	void icVsGt_heavyLoss_ampsNearlyDie_dramaticDivergence()
	{
		double bufferEu = 200.0;
		int voltage = VoltageTier.LV.getVoltage();
		double pathLoss = 30.0;
		double demand = 500.0;

		// IC: one subtraction
		double icDelivered = Math.min(
			EnergyTransferMath.icInjectAmount(bufferEu, pathLoss), demand);
		assertEquals(170.0, icDelivered);

		// GT: 6 amps × 30 loss each → 2 EU/amp survives
		int gtOfferAmps = EnergyTransferMath.gtOfferAmps(bufferEu, voltage);
		assertEquals(6, gtOfferAmps);

		int[] thirtyLoss = new int[30];
		for (int i = 0; i < 30; i++) thirtyLoss[i] = 1; // tin: 1 EU/m/A
		int survivingEu = EnergyTransferMath.gtPacketEuAfterPathLoss(voltage, thirtyLoss);
		assertEquals(2, survivingEu);

		int deliverableAmps = EnergyTransferMath.gtDeliverableAmps(gtOfferAmps, survivingEu);
		assertEquals(6, deliverableAmps);
		double gtDelivered = (double) deliverableAmps * survivingEu;
		assertEquals(12.0, gtDelivered);

		// IC delivers ~14× more than GT — correct given loss semantics
		assertTrue(icDelivered > gtDelivered * 10,
			"IC loss-once vs GT loss-per-amp should produce dramatic divergence");
	}

	/**
	 * @Spec EN-GT-010
	 * <b>Given</b> 0-loss (glass fibre) topology at LV,
	 * <b>When</b> both calculators process the same buffer,
	 * <b>Then</b> IC delivers all buffer EU; GT delivers all available amps × voltage.
	 * With 0 loss they converge — both deliver the same total EU modulo amp quantization.
	 */
	@Test
	void icVsGt_zeroLoss_convergesModuloQuantization()
	{
		double bufferEu = 128.0;
		int voltage = VoltageTier.LV.getVoltage(); // 32

		// IC: 128 EU, 0 loss → 128 EU
		double icDelivered = EnergyTransferMath.icInjectAmount(bufferEu, 0.0);
		assertEquals(128.0, icDelivered);

		// GT: floor(128/32) = 4 amps × 32 = 128 EU
		int gtOfferAmps = EnergyTransferMath.gtOfferAmps(bufferEu, voltage);
		assertEquals(4, gtOfferAmps);

		int glassLoss = CableSpec.forType(CableType.glass).getLossPerMeterPerAmp();
		assertEquals(0, glassLoss);

		int survivingEu = EnergyTransferMath.gtPacketEuAfterPathLoss(voltage,
			glassLoss, glassLoss, glassLoss, glassLoss, glassLoss);
		assertEquals(32, survivingEu); // unchanged

		int deliverableAmps = EnergyTransferMath.gtDeliverableAmps(gtOfferAmps, survivingEu);
		assertEquals(4, deliverableAmps);

		double gtDelivered = (double) deliverableAmps * survivingEu;
		assertEquals(128.0, gtDelivered);
		assertEquals(icDelivered, gtDelivered, 1e-9);
	}

	/**
	 * @Spec EN-GT-010
	 * <b>Given</b> a buffer that is an exact multiple of LV voltage plus a fractional remainder,
	 * <b>When</b> IC and GT both compute deliverable EU after 0 loss,
	 * <b>Then</b> IC delivers the full buffer (including fractional EU);
	 * GT delivers only the whole-amp portion (floor), discarding the remainder.
	 */
	@Test
	void icVsGt_fractionalBuffer_gtTruncates_icPreserves()
	{
		double bufferEu = 100.0; // 3.125 amps @ 32V; GT floors to 3
		int voltage = VoltageTier.LV.getVoltage();

		double icDelivered = EnergyTransferMath.icInjectAmount(bufferEu, 0.0);
		assertEquals(100.0, icDelivered);

		int gtAmps = EnergyTransferMath.gtOfferAmps(bufferEu, voltage);
		assertEquals(3, gtAmps);
		double gtDelivered = (double) gtAmps * voltage;
		assertEquals(96.0, gtDelivered);

		assertTrue(icDelivered > gtDelivered);
		assertEquals(4.0, icDelivered - gtDelivered, 1e-9); // IC preserves 4 EU fractional remainder
	}
}
