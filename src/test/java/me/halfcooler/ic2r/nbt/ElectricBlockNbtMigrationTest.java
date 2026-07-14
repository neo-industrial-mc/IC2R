package me.halfcooler.ic2r.nbt;

import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityElectricBlock;

import net.minecraft.nbt.CompoundTag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * G1.5 ElectricBlock NBT: {@code redstone_mode} write + legacy {@code redstoneMode} read.
 * Pure logic — no Level / BlockEntity bootstrap.
 */
class ElectricBlockNbtMigrationTest
{
	@Test
	void electricBlock_legacyCamelCase_isReadable()
	{
		CompoundTag legacy = new CompoundTag();
		legacy.putByte(TileEntityElectricBlock.LEGACY_NBT_REDSTONE_MODE, (byte) 3);

		assertEquals((byte) 3, TileEntityElectricBlock.readRedstoneModeNbt(legacy));
		assertEquals("redstoneMode", TileEntityElectricBlock.LEGACY_NBT_REDSTONE_MODE);
		assertEquals("redstone_mode", TileEntityElectricBlock.NBT_REDSTONE_MODE);
	}

	@Test
	void electricBlock_write_usesSnakeCaseOnly()
	{
		CompoundTag out = new CompoundTag();
		TileEntityElectricBlock.writeRedstoneModeNbt(out, (byte) 5);

		assertEquals((byte) 5, out.getByte(TileEntityElectricBlock.NBT_REDSTONE_MODE));
		assertTrue(out.contains(TileEntityElectricBlock.NBT_REDSTONE_MODE));
		assertFalse(out.contains(TileEntityElectricBlock.LEGACY_NBT_REDSTONE_MODE));
	}

	@Test
	void electricBlock_bothKeys_prefersModern()
	{
		CompoundTag both = new CompoundTag();
		both.putByte(TileEntityElectricBlock.NBT_REDSTONE_MODE, (byte) 2);
		both.putByte(TileEntityElectricBlock.LEGACY_NBT_REDSTONE_MODE, (byte) 6);
		assertEquals((byte) 2, TileEntityElectricBlock.readRedstoneModeNbt(both));
	}

	@Test
	void electricBlock_newKeyRoundTrip_andLegacyUpgrade()
	{
		CompoundTag written = new CompoundTag();
		TileEntityElectricBlock.writeRedstoneModeNbt(written, (byte) 1);
		assertEquals((byte) 1, TileEntityElectricBlock.readRedstoneModeNbt(written));

		CompoundTag legacy = new CompoundTag();
		legacy.putByte(TileEntityElectricBlock.LEGACY_NBT_REDSTONE_MODE, (byte) 4);
		byte loaded = TileEntityElectricBlock.readRedstoneModeNbt(legacy);
		CompoundTag upgraded = new CompoundTag();
		TileEntityElectricBlock.writeRedstoneModeNbt(upgraded, loaded);
		assertEquals((byte) 4, upgraded.getByte(TileEntityElectricBlock.NBT_REDSTONE_MODE));
		assertFalse(upgraded.contains(TileEntityElectricBlock.LEGACY_NBT_REDSTONE_MODE));
	}

	@Test
	void electricBlock_missingKeys_defaultZero()
	{
		assertEquals((byte) 0, TileEntityElectricBlock.readRedstoneModeNbt(new CompoundTag()));
	}
}
