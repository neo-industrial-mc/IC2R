package me.halfcooler.ic2r.nbt;

import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntityConversionGenerator;
import me.halfcooler.ic2r.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;

import net.minecraft.nbt.CompoundTag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W1.5 Energy-domain NBT pilot: snake_case write + legacy key read.
 * Aligns with golden suite NS-001 / NS-002 / NS-003.
 * <p>
 * Pure logic — no Level / BlockEntity bootstrap.
 */
class EnergyNbtMigrationTest
{
	/** @Spec NS-001: Energy component — old key {@code storage} alone loads correct value */
	@Test
	void energyComponent_legacyStorageKey_isReadable()
	{
		CompoundTag legacy = new CompoundTag();
		legacy.putDouble(Energy.LEGACY_NBT_STORAGE, 1234.5);

		assertEquals(1234.5, Energy.readEnergyBuffer(legacy), 0.0);
		assertFalse(legacy.contains(Energy.NBT_ENERGY_BUFFER));
		assertTrue(legacy.contains(Energy.LEGACY_NBT_STORAGE));
	}

	/** @Spec NS-003: Energy component — write emits only {@code energy_buffer} */
	@Test
	void energyComponent_write_usesSnakeCaseOnly()
	{
		CompoundTag out = new CompoundTag();
		Energy.writeEnergyBuffer(out, 500.0);

		assertEquals(500.0, out.getDouble(Energy.NBT_ENERGY_BUFFER), 0.0);
		assertTrue(out.contains(Energy.NBT_ENERGY_BUFFER));
		assertFalse(out.contains(Energy.LEGACY_NBT_STORAGE));
	}

	/** @Spec NS-003 + NS-001: Energy component new-key round-trip and legacy→read then rewrite */
	@Test
	void energyComponent_newKeyRoundTrip_andLegacyUpgradeRead()
	{
		CompoundTag written = new CompoundTag();
		Energy.writeEnergyBuffer(written, 88.0);
		assertEquals(88.0, Energy.readEnergyBuffer(written), 0.0);

		// Simulate save after load from legacy: write path upgrades to new key only
		CompoundTag legacy = new CompoundTag();
		legacy.putDouble(Energy.LEGACY_NBT_STORAGE, 77.0);
		double loaded = Energy.readEnergyBuffer(legacy);
		CompoundTag upgraded = new CompoundTag();
		Energy.writeEnergyBuffer(upgraded, loaded);
		assertEquals(77.0, upgraded.getDouble(Energy.NBT_ENERGY_BUFFER), 0.0);
		assertFalse(upgraded.contains(Energy.LEGACY_NBT_STORAGE));
	}

	/** @Spec NS-002: when both keys present, modern {@code energy_buffer} wins */
	@Test
	void energyComponent_bothKeys_prefersModern()
	{
		CompoundTag both = new CompoundTag();
		both.putDouble(Energy.NBT_ENERGY_BUFFER, 10.0);
		both.putDouble(Energy.LEGACY_NBT_STORAGE, 999.0);
		assertEquals(10.0, Energy.readEnergyBuffer(both), 0.0);
	}

	/** @Spec NS-001: ConversionGenerator camelCase {@code energyBuffer} alone is readable */
	@Test
	void conversionGenerator_legacyCamelCase_isReadable()
	{
		CompoundTag legacy = new CompoundTag();
		legacy.putDouble(TileEntityConversionGenerator.LEGACY_NBT_ENERGY_BUFFER, 256.0);

		assertEquals(256.0, TileEntityConversionGenerator.readEnergyBufferNbt(legacy), 0.0);
		assertEquals("energyBuffer", TileEntityConversionGenerator.LEGACY_NBT_ENERGY_BUFFER);
		assertEquals("energy_buffer", TileEntityConversionGenerator.NBT_ENERGY_BUFFER);
	}

	/** @Spec NS-003: ConversionGenerator write uses snake_case only */
	@Test
	void conversionGenerator_write_usesSnakeCaseOnly()
	{
		CompoundTag out = new CompoundTag();
		TileEntityConversionGenerator.writeEnergyBufferNbt(out, 64.0);

		assertEquals(64.0, out.getDouble(TileEntityConversionGenerator.NBT_ENERGY_BUFFER), 0.0);
		assertFalse(out.contains(TileEntityConversionGenerator.LEGACY_NBT_ENERGY_BUFFER));
	}

	/** G1.5: NuclearReactor camelCase {@code energyBuffer} alone is readable */
	@Test
	void nuclearReactor_legacyCamelCase_isReadable()
	{
		CompoundTag legacy = new CompoundTag();
		legacy.putDouble(TileEntityNuclearReactorElectric.LEGACY_NBT_ENERGY_BUFFER, 512.0);

		assertEquals(512.0, TileEntityNuclearReactorElectric.readEnergyBufferNbt(legacy), 0.0);
		assertEquals("energyBuffer", TileEntityNuclearReactorElectric.LEGACY_NBT_ENERGY_BUFFER);
		assertEquals("energy_buffer", TileEntityNuclearReactorElectric.NBT_ENERGY_BUFFER);
	}

	/** G1.5: NuclearReactor write uses snake_case only */
	@Test
	void nuclearReactor_write_usesSnakeCaseOnly()
	{
		CompoundTag out = new CompoundTag();
		TileEntityNuclearReactorElectric.writeEnergyBufferNbt(out, 128.0);

		assertEquals(128.0, out.getDouble(TileEntityNuclearReactorElectric.NBT_ENERGY_BUFFER), 0.0);
		assertFalse(out.contains(TileEntityNuclearReactorElectric.LEGACY_NBT_ENERGY_BUFFER));
	}

	/** G1.5: NuclearReactor modern key wins over legacy when both present */
	@Test
	void nuclearReactor_bothKeys_prefersModern()
	{
		CompoundTag both = new CompoundTag();
		both.putDouble(TileEntityNuclearReactorElectric.NBT_ENERGY_BUFFER, 11.0);
		both.putDouble(TileEntityNuclearReactorElectric.LEGACY_NBT_ENERGY_BUFFER, 999.0);
		assertEquals(11.0, TileEntityNuclearReactorElectric.readEnergyBufferNbt(both), 0.0);
	}

	/** G1.5: NuclearReactor legacy→read then rewrite upgrades key */
	@Test
	void nuclearReactor_legacyUpgradeReadWrite()
	{
		CompoundTag legacy = new CompoundTag();
		legacy.putDouble(TileEntityNuclearReactorElectric.LEGACY_NBT_ENERGY_BUFFER, 44.0);
		double loaded = TileEntityNuclearReactorElectric.readEnergyBufferNbt(legacy);
		CompoundTag upgraded = new CompoundTag();
		TileEntityNuclearReactorElectric.writeEnergyBufferNbt(upgraded, loaded);
		assertEquals(44.0, upgraded.getDouble(TileEntityNuclearReactorElectric.NBT_ENERGY_BUFFER), 0.0);
		assertFalse(upgraded.contains(TileEntityNuclearReactorElectric.LEGACY_NBT_ENERGY_BUFFER));
	}
}
