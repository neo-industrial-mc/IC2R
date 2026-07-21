package me.halfcooler.ic2r.nbt;

import me.halfcooler.ic2r.core.util.LegacyItemStackNbt;

import net.minecraft.nbt.CompoundTag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure NBT structure tests for 1.20.1 → 1.21 ItemStack field migration.
 * No ItemStack bootstrap — only {@link LegacyItemStackNbt#normalize(CompoundTag)}.
 */
class LegacyItemStackNbtTest
{
	@Test
	void needsNormalize_detectsLegacyCountAndTag()
	{
		CompoundTag modern = new CompoundTag();
		modern.putString("id", "ic2r:re_battery");
		modern.putInt("count", 1);
		assertFalse(LegacyItemStackNbt.needsNormalize(modern));

		CompoundTag legacy = new CompoundTag();
		legacy.putString("id", "ic2:re_battery");
		legacy.putByte("Count", (byte) 1);
		assertTrue(LegacyItemStackNbt.needsNormalize(legacy));

		CompoundTag withTag = new CompoundTag();
		withTag.putString("id", "ic2r:re_battery");
		withTag.putInt("count", 1);
		CompoundTag tag = new CompoundTag();
		tag.putDouble("charge", 10000.0);
		withTag.put("tag", tag);
		assertTrue(LegacyItemStackNbt.needsNormalize(withTag));
	}

	@Test
	void normalize_convertsCountTagAndPreservesIndex()
	{
		CompoundTag legacy = new CompoundTag();
		legacy.putByte("Index", (byte) 2);
		legacy.putString("id", "ic2:re_battery");
		legacy.putByte("Count", (byte) 3);
		CompoundTag tag = new CompoundTag();
		tag.putDouble("charge", 12345.0);
		legacy.put("tag", tag);

		CompoundTag out = LegacyItemStackNbt.normalize(legacy);

		assertEquals((byte) 2, out.getByte("Index"));
		assertEquals("ic2:re_battery", out.getString("id"));
		assertEquals(3, out.getInt("count"));
		assertFalse(out.contains("Count"));
		assertFalse(out.contains("tag"));
		assertTrue(out.contains("components", 10));
		CompoundTag components = out.getCompound("components");
		assertTrue(components.contains("minecraft:custom_data", 10));
		assertEquals(12345.0, components.getCompound("minecraft:custom_data").getDouble("charge"), 0.0);

		// Input not mutated
		assertTrue(legacy.contains("Count"));
		assertTrue(legacy.contains("tag"));
	}

	@Test
	void normalize_liftsDamageIntoComponent()
	{
		CompoundTag legacy = new CompoundTag();
		legacy.putString("id", "minecraft:iron_pickaxe");
		legacy.putByte("Count", (byte) 1);
		CompoundTag tag = new CompoundTag();
		tag.putInt("Damage", 42);
		legacy.put("tag", tag);

		CompoundTag out = LegacyItemStackNbt.normalize(legacy);
		CompoundTag components = out.getCompound("components");
		assertEquals(42, components.getInt("minecraft:damage"));
		assertFalse(components.contains("minecraft:custom_data"));
	}

	@Test
	void normalize_modernPassthroughCopy()
	{
		CompoundTag modern = new CompoundTag();
		modern.putString("id", "ic2r:mfsu");
		modern.putInt("count", 1);
		CompoundTag components = new CompoundTag();
		CompoundTag custom = new CompoundTag();
		custom.putDouble("energy", 1_000_000.0);
		components.put("minecraft:custom_data", custom);
		modern.put("components", components);

		CompoundTag out = LegacyItemStackNbt.normalize(modern);
		assertEquals(1_000_000.0, out.getCompound("components").getCompound("minecraft:custom_data").getDouble("energy"), 0.0);
		assertFalse(out.contains("Count"));
	}
}
