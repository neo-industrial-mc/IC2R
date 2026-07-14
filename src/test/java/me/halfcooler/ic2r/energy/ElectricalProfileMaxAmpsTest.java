package me.halfcooler.ic2r.energy;

import me.halfcooler.ic2r.api.energy.profile.VoltageTier;
import me.halfcooler.ic2r.core.energy.profile.ElectricalProfile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GT Mode demand amp formula on {@link ElectricalProfile} (no Level / client).
 * Spec IDs: EN-GT-003, EN-GT-004, EN-GT-005.
 * <p>
 * Working: {@code maxAmps = ⌊2 × recipeEU/t / tierVoltage⌋ + 1}
 * Idle ({@code recipePower ≤ 0}): max sink amperage is 1 (buffer top-up only).
 */
class ElectricalProfileMaxAmpsTest
{
	/** @Spec EN-GT-004 例：16 EU/t @ LV(32V) → 2A */
	@Test
	void getMaxSinkAmperage_recipe16EuPerTickLv_is2A()
	{
		ElectricalProfile profile = new ElectricalProfile(VoltageTier.LV);
		profile.setRecipePower(16);
		// floor(2 * 16 / 32) + 1 = floor(1) + 1 = 2
		assertEquals(2, profile.getMaxSinkAmperage());
	}

	/** @Spec EN-GT-004 / EN-GT-005 2 EU/t @ LV → 1A（下界） */
	@Test
	void getMaxSinkAmperage_recipe2EuPerTickLv_isAtLeast1A()
	{
		ElectricalProfile profile = new ElectricalProfile(VoltageTier.LV);
		profile.setRecipePower(2);
		// floor(2 * 2 / 32) + 1 = floor(0.125) + 1 = 1
		assertEquals(1, profile.getMaxSinkAmperage());
		assertTrue(profile.getMaxSinkAmperage() >= 1);
	}

	/** @Spec EN-GT-005 极低 EU/t 仍至少 1A */
	@Test
	void getMaxSinkAmperage_veryLowRecipePower_isAtLeast1A()
	{
		ElectricalProfile profile = new ElectricalProfile(VoltageTier.LV);
		profile.setRecipePower(1);
		assertEquals(1, profile.getMaxSinkAmperage());
	}

	/** @Spec EN-GT-003 空闲（无配方功率）最多请求 1A 维持缓冲 */
	@Test
	void getMaxSinkAmperage_idleRecipePowerZero_is1A()
	{
		ElectricalProfile profile = new ElectricalProfile(VoltageTier.LV);
		profile.setRecipePower(0);
		assertEquals(1, profile.getMaxSinkAmperage());
	}

	/** @Spec EN-GT-004 更高负载：64 EU/t @ LV → 5A */
	@Test
	void getMaxSinkAmperage_recipe64EuPerTickLv_is5A()
	{
		ElectricalProfile profile = new ElectricalProfile(VoltageTier.LV);
		profile.setRecipePower(64);
		// floor(2 * 64 / 32) + 1 = floor(4) + 1 = 5
		assertEquals(5, profile.getMaxSinkAmperage());
	}

	/** Voltage tier anchors used by GT packet EU (1A × V). */
	@Test
	void voltageTier_lvMvHv_matchGtLadder()
	{
		assertEquals(32, VoltageTier.LV.getVoltage());
		assertEquals(128, VoltageTier.MV.getVoltage());
		assertEquals(512, VoltageTier.HV.getVoltage());
		assertEquals(VoltageTier.LV, VoltageTier.fromIcTier(1));
		assertEquals(VoltageTier.MV, VoltageTier.fromPower(128));
	}
}
