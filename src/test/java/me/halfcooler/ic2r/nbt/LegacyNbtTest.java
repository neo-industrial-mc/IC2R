package me.halfcooler.ic2r.nbt;

import me.halfcooler.ic2r.core.util.LegacyNbt;

import net.minecraft.nbt.CompoundTag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure unit tests for {@link LegacyNbt} primary-then-legacy key resolution.
 */
class LegacyNbtTest
{
	@Test
	void getDouble_prefersPrimaryOverLegacy()
	{
		CompoundTag tag = new CompoundTag();
		tag.putDouble("energy_buffer", 42.5);
		tag.putDouble("energyBuffer", 1.0);
		assertEquals(42.5, LegacyNbt.getDouble(tag, "energy_buffer", "energyBuffer"), 0.0);
	}

	@Test
	void getDouble_fallsBackToLegacyWhenPrimaryMissing()
	{
		CompoundTag tag = new CompoundTag();
		tag.putDouble("energyBuffer", 99.0);
		assertEquals(99.0, LegacyNbt.getDouble(tag, "energy_buffer", "energyBuffer"), 0.0);
	}

	@Test
	void getDouble_missingKeys_returnsZero()
	{
		assertEquals(0.0, LegacyNbt.getDouble(new CompoundTag(), "energy_buffer", "energyBuffer"), 0.0);
	}

	@Test
	void getShort_and_getInt_roundTripStyle()
	{
		CompoundTag tag = new CompoundTag();
		tag.putShort("progress", (short) 120);
		assertEquals((short) 120, LegacyNbt.getShort(tag, "progress"));
		tag.putInt("fuel", 7);
		assertEquals(7, LegacyNbt.getInt(tag, "fuel", "totalFuel"));
	}

	@Test
	void contains_primaryOrLegacy()
	{
		CompoundTag tag = new CompoundTag();
		assertFalse(LegacyNbt.contains(tag, "energy_buffer", "storage"));
		tag.putDouble("storage", 1.0);
		assertTrue(LegacyNbt.contains(tag, "energy_buffer", "storage"));
	}
}
