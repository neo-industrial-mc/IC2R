package ic2.core.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

final class Ic2GameTestAssertions
{
	private Ic2GameTestAssertions()
	{
	}

	static void assertNear(GameTestHelper helper, double actual, double expected, String name)
	{
		helper.assertTrue(Math.abs(actual - expected) < 1.0E-6, name + ": expected " + expected + ", got " + actual);
	}
}
