package me.halfcooler.ic2r.energy;

import me.halfcooler.ic2r.core.energy.grid.EnergyTransferMath;
import me.halfcooler.ic2r.core.energy.profile.CableSpec;
import me.halfcooler.ic2r.core.block.wiring.CableType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-logic EnergyNet transfer formulas (no Level / client).
 * Spec IDs: EN-IC-001, EN-IC-003, EN-GT-001, EN-GT-002, EN-GT-006, EN-GT-007.
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
}
