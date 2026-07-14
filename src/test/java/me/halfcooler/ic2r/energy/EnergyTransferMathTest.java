package me.halfcooler.ic2r.energy;

import me.halfcooler.ic2r.api.energy.profile.VoltageTier;
import me.halfcooler.ic2r.core.block.wiring.CableType;
import me.halfcooler.ic2r.core.energy.grid.EnergyTransferMath;
import me.halfcooler.ic2r.core.energy.profile.CableSpec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-logic EnergyNet transfer formulas (no Level / client).
 * Spec IDs: EN-IC-001…010 (selected), EN-GT-001/002/006/007/009.
 */
class EnergyTransferMathTest
{
	// --- EN-IC-001 / EN-IC-003: IC path loss → sink inject amount ---

	/** @Spec EN-IC-001 单源单汇：到达量 = 发出量 − 路径线损 */
	@Test
	void icInjectAmount_offerMinusLoss_equalsDelivered()
	{
		assertEquals(90.0, EnergyTransferMath.icInjectAmount(100.0, 10.0));
		assertEquals(32.0, EnergyTransferMath.icInjectAmount(32.0, 0.0));
	}

	/** @Spec EN-IC-003 线损耗尽 / 不足时到达量 ≥ 0，不得负 EU 入汇 */
	@Test
	void icInjectAmount_lossExceedsOrEqualsOffer_isZeroNotNegative()
	{
		assertEquals(0.0, EnergyTransferMath.icInjectAmount(5.0, 10.0));
		assertEquals(0.0, EnergyTransferMath.icInjectAmount(10.0, 10.0));
		assertEquals(0.0, EnergyTransferMath.icInjectAmount(0.0, 1.0));
		assertTrue(EnergyTransferMath.icInjectAmount(1.0, 100.0) >= 0.0);
	}

	// --- EN-IC-002: path preference by loss ---

	/** @Spec EN-IC-002 路径缓存仅在候选 loss 严格更优时替换 */
	@Test
	void icPreferNewPath_onlyWhenStrictlyLowerLoss()
	{
		assertTrue(EnergyTransferMath.icPreferNewPath(4.0, 2.0));
		assertFalse(EnergyTransferMath.icPreferNewPath(2.0, 2.0));
		assertFalse(EnergyTransferMath.icPreferNewPath(1.0, 3.0));
	}

	// --- EN-IC-004 / EN-IC-005: multi-sink sequential distribute ---

	/** @Spec EN-IC-004 多汇交付之和 ≤ 源 offer（线损从源侧扣） */
	@Test
	void icDistributeSequential_totalDelivered_notExceedsOffer()
	{
		double offer = 100.0;
		double[] losses = {10.0, 5.0, 20.0};
		double[] demands = {50.0, 80.0, 40.0};
		double[] delivered = EnergyTransferMath.icDistributeSequential(offer, losses, demands);

		double sum = 0.0;
		double remaining = offer;
		for (int i = 0; i < delivered.length; i++)
		{
			sum += delivered[i];
			remaining -= EnergyTransferMath.icSourceConsumed(delivered[i], losses[i]);
		}

		assertTrue(sum <= offer);
		assertTrue(remaining >= -1e-9);
		// first path: inject 90, demand 50 → 50; source pays 60
		// second: remaining 40, inject 35, demand 80 → 35; pays 40
		// third: remaining 0
		assertArrayEquals(new double[] {50.0, 35.0, 0.0}, delivered, 1e-9);
	}

	/** @Spec EN-IC-005 固定顺序：前序路径先按需填满，后序吃 residual */
	@Test
	void icDistributeSequential_earlierPathPriority_fillsBeforeLater()
	{
		double[] delivered = EnergyTransferMath.icDistributeSequential(
			64.0,
			new double[] {0.0, 0.0},
			new double[] {40.0, 40.0});
		assertArrayEquals(new double[] {40.0, 24.0}, delivered, 1e-9);

		// path loss kills residual before second sink
		double[] starved = EnergyTransferMath.icDistributeSequential(
			30.0,
			new double[] {0.0, 25.0},
			new double[] {10.0, 100.0});
		assertArrayEquals(new double[] {10.0, 0.0}, starved, 1e-9);
	}

	// --- EN-IC-006 / EN-IC-007 / EN-IC-008: protection predicates ---

	/** @Spec EN-IC-006 绝缘击穿：超过绝缘阈值且未达导体熔断时 strip */
	@Test
	void icInsulationBreakdown_betweenInsulationAndConductorLimits()
	{
		// Synthetic high-capacity conductor so insulation window is reachable
		double insulation = EnergyTransferMath.IC_INSULATION_BREAKDOWN_ENERGY; // 9001
		double conductor = 10000.0;
		assertFalse(EnergyTransferMath.icInsulationBreakdown(9001.0, insulation, conductor));
		assertTrue(EnergyTransferMath.icInsulationBreakdown(9001.5, insulation, conductor));
		assertTrue(EnergyTransferMath.icInsulationBreakdown(10000.0, insulation, conductor));
		// melt wins over strip when amount exceeds conductor limit
		assertFalse(EnergyTransferMath.icInsulationBreakdown(10000.5, insulation, conductor));
		assertTrue(EnergyTransferMath.icConductorBreakdown(10000.5, conductor));
	}

	/** @Spec EN-IC-007 导体熔断：包能量 &gt; capacity+1（标准电缆） */
	@Test
	void icConductorBreakdown_aboveCapacityPlusOne_isTrue()
	{
		double tinLimit = EnergyTransferMath.icConductorBreakdownEnergy(CableType.tin.capacity);
		assertEquals(33.0, tinLimit);
		assertFalse(EnergyTransferMath.icConductorBreakdown(32.0, tinLimit));
		assertFalse(EnergyTransferMath.icConductorBreakdown(33.0, tinLimit));
		assertTrue(EnergyTransferMath.icConductorBreakdown(33.1, tinLimit));

		double copperLimit = EnergyTransferMath.icConductorBreakdownEnergy(CableType.copper.capacity);
		assertEquals(129.0, copperLimit);
		assertTrue(EnergyTransferMath.icConductorBreakdown(130.0, copperLimit));
	}

	/** @Spec EN-IC-008 汇侧超压：packet &gt; sink 额定（tier 功率） */
	@Test
	void icSinkOverVoltage_packetAboveSinkTierPower_isTrue()
	{
		double lvMax = EnergyTransferMath.icPowerFromTier(VoltageTier.LV.getIcTier());
		assertEquals(32.0, lvMax);
		assertFalse(EnergyTransferMath.icSinkOverVoltage(32.0, lvMax));
		assertTrue(EnergyTransferMath.icSinkOverVoltage(33.0, lvMax));
		assertTrue(EnergyTransferMath.icSinkOverVoltage(128.0, lvMax));

		double mvMax = EnergyTransferMath.icPowerFromTier(VoltageTier.MV.getIcTier());
		assertEquals(128.0, mvMax);
		assertFalse(EnergyTransferMath.icSinkOverVoltage(128.0, mvMax));
		assertTrue(EnergyTransferMath.icSinkOverVoltage(129.0, mvMax));
	}

	// --- EN-IC-009 / EN-IC-010: transformer packet ratios ---

	/** @Spec EN-IC-009 升压：4× 低压侧 → 1× 高压 packet，能量守恒 */
	@Test
	void icTransformer_stepUp_fourLowToOneHigh_conservesEnergy()
	{
		assertEquals(1, EnergyTransferMath.icTransformerOutputPackets(true));
		assertEquals(4, EnergyTransferMath.icTransformerInputAmps(true));
		int low = VoltageTier.LV.getVoltage();
		int high = VoltageTier.MV.getVoltage();
		assertEquals(32, low);
		assertEquals(128, high);
		assertTrue(EnergyTransferMath.icTransformerConservesEnergy(low, high, true));
		// 4 * 32 = 128 * 1
		assertEquals(low * 4L, high * 1L);
	}

	/** @Spec EN-IC-010 降压：1× 高压 → 4× 低压 packet，能量守恒 */
	@Test
	void icTransformer_stepDown_oneHighToFourLow_conservesEnergy()
	{
		assertEquals(4, EnergyTransferMath.icTransformerOutputPackets(false));
		assertEquals(1, EnergyTransferMath.icTransformerInputAmps(false));
		int low = VoltageTier.MV.getVoltage();
		int high = VoltageTier.HV.getVoltage();
		assertTrue(EnergyTransferMath.icTransformerConservesEnergy(low, high, false));
		// 1 * 512 = 4 * 128
		assertEquals(high * 1L, low * 4L);
	}

	/** IC tier ladder anchors used by overvoltage / transformer checks */
	@Test
	void icPowerFromTier_ulvLvMvHv_matchLadder()
	{
		assertEquals(8.0, EnergyTransferMath.icPowerFromTier(0));
		assertEquals(32.0, EnergyTransferMath.icPowerFromTier(1));
		assertEquals(128.0, EnergyTransferMath.icPowerFromTier(2));
		assertEquals(512.0, EnergyTransferMath.icPowerFromTier(3));
		assertEquals(1, EnergyTransferMath.icTierFromPower(32.0));
		assertEquals(2, EnergyTransferMath.icTierFromPower(128.0));
	}

	// --- EN-GT-001 / EN-GT-002: 1A 不可拆；线损只减包内 EU ---

	/** @Spec EN-GT-001 1A 包不可拆分：存活则整安交付，死亡则 0A */
	@Test
	void gtDeliverableAmps_packetSurvives_keepsWholeAmps()
	{
		assertEquals(1, EnergyTransferMath.gtDeliverableAmps(1, 32));
		assertEquals(3, EnergyTransferMath.gtDeliverableAmps(3, 29));
		assertEquals(0, EnergyTransferMath.gtDeliverableAmps(1, 0));
		assertEquals(0, EnergyTransferMath.gtDeliverableAmps(0, 32));
	}

	/** @Spec EN-GT-002 线损后整包注入：loss 只减 EU；包死则 0 */
	@Test
	void gtPacketEuAfterPathLoss_reducesEuOrDies_neverFractionalAmp()
	{
		// LV 32V, tin loss 1/m/A × 3 blocks → 29 EU still one amp
		int tinLoss = CableSpec.forType(CableType.tin).getLossPerMeterPerAmp();
		assertEquals(1, tinLoss);
		int remaining = EnergyTransferMath.gtPacketEuAfterPathLoss(32, tinLoss, tinLoss, tinLoss);
		assertEquals(29, remaining);
		assertEquals(1, EnergyTransferMath.gtDeliverableAmps(1, remaining));

		// loss exhausts packet
		assertEquals(0, EnergyTransferMath.gtPacketEuAfterPathLoss(2, 1, 1));
		assertEquals(0, EnergyTransferMath.gtDeliverableAmps(1, 0));

		// zero-loss glass
		int glassLoss = CableSpec.forType(CableType.glass).getLossPerMeterPerAmp();
		assertEquals(0, glassLoss);
		assertEquals(32, EnergyTransferMath.gtPacketEuAfterPathLoss(32, glassLoss, glassLoss));
	}

	// --- EN-GT-006 / EN-GT-007: 超压 / 超流熔断判定 ---

	/** @Spec EN-GT-006 包电压 &gt; 导线 maxVoltage → 熔断 */
	@Test
	void gtCableOverVoltage_packetAboveCableMax_isTrue()
	{
		CableSpec tin = CableSpec.forType(CableType.tin); // LV 32V
		assertFalse(EnergyTransferMath.gtCableOverVoltage(32, tin.getMaxVoltage().getVoltage()));
		assertTrue(EnergyTransferMath.gtCableOverVoltage(33, tin.getMaxVoltage().getVoltage()));
		assertTrue(EnergyTransferMath.gtCableOverVoltage(128, tin.getMaxVoltage().getVoltage()));
	}

	/** @Spec EN-GT-007 路径安培超过导线 maxAmps → 熔断 */
	@Test
	void gtCableOverCurrent_loadPlusSendExceedsMax_isTrue()
	{
		CableSpec tin = CableSpec.forType(CableType.tin); // 1A
		assertEquals(1, tin.getMaxAmperage());
		assertFalse(EnergyTransferMath.gtCableOverCurrent(0, 1, tin.getMaxAmperage()));
		assertTrue(EnergyTransferMath.gtCableOverCurrent(1, 1, tin.getMaxAmperage()));
		assertTrue(EnergyTransferMath.gtCableOverCurrent(0, 2, tin.getMaxAmperage()));
	}

	// --- EN-GT-009: full 1A before source offers ---

	/** @Spec EN-GT-009 缓冲不足 1A（&lt; V）时 offer 0；满整安才输出 */
	@Test
	void gtOfferAmps_partialBufferBelowOneAmp_isZero()
	{
		int lv = VoltageTier.LV.getVoltage();
		assertEquals(0, EnergyTransferMath.gtOfferAmps(0.0, lv));
		assertEquals(0, EnergyTransferMath.gtOfferAmps(31.9, lv));
		assertEquals(1, EnergyTransferMath.gtOfferAmps(32.0, lv));
		assertEquals(1, EnergyTransferMath.gtOfferAmps(63.9, lv));
		assertEquals(2, EnergyTransferMath.gtOfferAmps(64.0, lv));
		assertEquals(1, EnergyTransferMath.gtOfferAmpsCapped(128.0, lv, 1));
		assertEquals(0, EnergyTransferMath.gtOfferAmpsCapped(64.0, lv, 0));
	}
}
