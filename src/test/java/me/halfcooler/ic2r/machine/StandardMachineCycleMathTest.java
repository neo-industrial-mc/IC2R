package me.halfcooler.ic2r.machine;

import me.halfcooler.ic2r.core.block.machine.tileentity.StandardMachineCycleMath;
import me.halfcooler.ic2r.core.block.machine.tileentity.StandardMachineCycleMath.CycleTickResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic standard-machine cycle (G1.4 / SM-*).
 * No Level / TE / client; mirrors {@code TileEntityStandardMachine} + overclock rates.
 */
class StandardMachineCycleMathTest
{

	/**
	 * @Spec SM-001 满足条件时每 tick 进度 +1，直至 operationLength 完成并归零
	 */
	@Test
	void tick_withRecipeAndEnergy_advancesProgressUntilComplete()
	{
		final int length = 4;
		final int consume = 2;
		double energy = 100.0;
		short progress = 0;

		for (int i = 1; i < length; i++)
		{
			CycleTickResult r = StandardMachineCycleMath.tick(progress, length, consume, energy, true);
			assertTrue(r.energyConsumed());
			assertFalse(r.operationCompleted());
			assertTrue(r.shouldBeActive());
			assertEquals(i, r.progress());
			assertEquals(energy - consume, r.energyStored(), 1e-9);
			progress = r.progress();
			energy = r.energyStored();
		}

		CycleTickResult done = StandardMachineCycleMath.tick(progress, length, consume, energy, true);
		assertTrue(done.operationCompleted());
		assertEquals(0, done.progress());
		assertEquals(energy - consume, done.energyStored(), 1e-9);
	}


	/**
	 * @Spec SM-002 推进进度时恰好扣除 energyConsume EU
	 */
	@Test
	void tick_whenAdvancing_consumesExactlyEnergyConsume()
	{
		CycleTickResult r = StandardMachineCycleMath.tick((short) 0, 10, 7, 50.0, true);
		assertTrue(r.energyConsumed());
		assertEquals(43.0, r.energyStored(), 1e-9);
		assertEquals(1, r.progress());
	}


	/**
	 * @Spec SM-003 缓冲 EU 不足时不推进进度，且配方仍就绪时进度保留
	 */
	@Test
	void tick_energyInsufficient_blocksProgressButKeepsIt()
	{
		CycleTickResult r = StandardMachineCycleMath.tick((short) 5, 20, 10, 9.0, true);
		assertFalse(r.energyConsumed());
		assertFalse(r.operationCompleted());
		assertFalse(r.shouldBeActive());
		assertEquals(5, r.progress());
		assertEquals(9.0, r.energyStored(), 1e-9);
	}

	/**
	 * @Spec SM-003 恢复供电后从保留进度继续（不强制归零）
	 */
	@Test
	void tick_afterEnergyRestore_resumesFromRetainedProgress()
	{
		CycleTickResult blocked = StandardMachineCycleMath.tick((short) 3, 10, 5, 0.0, true);
		assertEquals(3, blocked.progress());

		CycleTickResult resumed = StandardMachineCycleMath.tick(
			blocked.progress(), 10, 5, 20.0, true
		);
		assertTrue(resumed.energyConsumed());
		assertEquals(4, resumed.progress());
		assertEquals(15.0, resumed.energyStored(), 1e-9);
	}


	/**
	 * @Spec SM-004 输出满（recipeReady=false）不耗电、进度清零，不得当作可运行
	 */
	@Test
	void tick_outputFull_noEnergyNoProgress()
	{
		CycleTickResult r = StandardMachineCycleMath.tick((short) 8, 30, 4, 100.0, false);
		assertFalse(r.energyConsumed());
		assertFalse(r.operationCompleted());
		assertFalse(r.shouldBeActive());
		assertEquals(0, r.progress());
		assertEquals(100.0, r.energyStored(), 1e-9);
	}


	/**
	 * @Spec SM-005 输入不足时 recipeReady=false：停止处理、清进度、不耗电
	 */
	@Test
	void tick_inputMissing_clearsProgressWithoutConsume()
	{
		CycleTickResult r = StandardMachineCycleMath.tick((short) 2, 10, 3, 40.0, false);
		assertEquals(0, r.progress());
		assertFalse(r.energyConsumed());
		assertEquals(40.0, r.energyStored(), 1e-9);
	}


	/**
	 * @Spec SM-001 GUI：guiProgress = progress / operationLength
	 */
	@Test
	void guiProgress_isProgressOverLength()
	{
		assertEquals(0.0F, StandardMachineCycleMath.guiProgress(0, 100), 1e-6F);
		assertEquals(0.5F, StandardMachineCycleMath.guiProgress(50, 100), 1e-6F);
		assertEquals(1.0F, StandardMachineCycleMath.guiProgress(10, 10), 1e-6F);
		assertEquals(0.0F, StandardMachineCycleMath.guiProgress(5, 0), 1e-6F);
	}


	/**
	 * @Spec SM-006 超频：processTimeMultiplier=0.7、energyDemandMultiplier=1.6（单枚超频）
	 * 缩短 operationLength、提高 energyDemand；无升级时与默认一致。
	 */
	@Test
	void overclock_oneModule_shortensLengthAndRaisesDemand()
	{
		final int defaultLen = 100;
		final int defaultEu = 2;

		assertEquals(1, StandardMachineCycleMath.operationsPerTick(defaultLen, 0, 1.0));
		assertEquals(defaultLen, StandardMachineCycleMath.operationLength(defaultLen, 0, 1.0));
		assertEquals(defaultEu, StandardMachineCycleMath.energyDemand(defaultEu, 0, 1.0));

		int ocLen = StandardMachineCycleMath.operationLength(defaultLen, 0, 0.7);
		int ocEu = StandardMachineCycleMath.energyDemand(defaultEu, 0, 1.6);
		int ocOps = StandardMachineCycleMath.operationsPerTick(defaultLen, 0, 0.7);

		assertTrue(ocLen < defaultLen, "overclock shortens cycle length");
		assertEquals(70, ocLen);
		assertEquals(3, ocEu);
		assertEquals(1, ocOps);
	}

	/**
	 * @Spec SM-006 超频后进度按比例重标定，避免长度变化丢进度
	 */
	@Test
	void rescaleProgress_preservesRatioOnLengthChange()
	{
		short scaled = StandardMachineCycleMath.rescaleProgress((short) 50, 100, 70);
		assertEquals(35, scaled);

		assertEquals(0, StandardMachineCycleMath.rescaleProgress((short) 0, 100, 70));
	}

	/**
	 * canOperate gate used by TE before useEnergy
	 */
	@Test
	void canOperate_requiresRecipeAndEnergy()
	{
		assertTrue(StandardMachineCycleMath.canOperate(true, 10.0, 10));
		assertTrue(StandardMachineCycleMath.canOperate(true, 10.0, 9));
		assertFalse(StandardMachineCycleMath.canOperate(true, 9.0, 10));
		assertFalse(StandardMachineCycleMath.canOperate(false, 100.0, 1));
	}


	/**
	 * defaultOperationLength == 0 → historical IC2 batch: 64 ops/tick, length 1.
	 * rescale with non-positive lengths → 0 (no NaN/negative progress).
	 */
	@Test
	void zeroDefaultLength_andInvalidRescale()
	{
		assertEquals(64, StandardMachineCycleMath.operationsPerTick(0, 0, 1.0));
		assertEquals(1, StandardMachineCycleMath.operationLength(0, 0, 1.0));
		assertEquals(64, StandardMachineCycleMath.operationsPerTick(0, 10, 0.7));
		assertEquals(1, StandardMachineCycleMath.operationLength(0, 10, 0.7));

		assertEquals(0, StandardMachineCycleMath.rescaleProgress((short) 50, 0, 70));
		assertEquals(0, StandardMachineCycleMath.rescaleProgress((short) 50, 100, 0));
		assertEquals(0, StandardMachineCycleMath.rescaleProgress((short) 50, -1, 70));
	}

	/**
	 * Heavy overclock stack: processTime→0-ish stackOpLen drives opsPerTick toward MAX;
	 * energyDemand applies IC2 rounding via applyModifier.
	 */
	@Test
	void multiOverclock_extremeTimeMultiplier_andEnergyClamp()
	{
		int ops = StandardMachineCycleMath.operationsPerTick(100, 0, 1e-9);
		assertTrue(ops > 1);
		assertTrue(ops <= Integer.MAX_VALUE);

		assertEquals(Integer.MAX_VALUE, StandardMachineCycleMath.applyModifier(
			Integer.MAX_VALUE, 0, 2.0));

		int len = StandardMachineCycleMath.operationLength(100, 0, 0.49);
		int eu = StandardMachineCycleMath.energyDemand(2, 0, 2.56);
		assertTrue(len < 70, "two OC shorter than one OC (70)");
		assertEquals(5, eu);

		CycleTickResult r = StandardMachineCycleMath.tick((short) 0, 0, 1, 10.0, true);
		assertTrue(r.operationCompleted());
		assertEquals(0, r.progress());
		assertEquals(9.0, r.energyStored(), 1e-9);
	}
}
