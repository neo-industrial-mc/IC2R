package me.halfcooler.ic2r.nbt;

import me.halfcooler.ic2r.core.block.comp.Components;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;

import net.minecraft.nbt.CompoundTag;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TE component bag key migration: {@code components} → {@code ic2r_components}
 * to avoid colliding with vanilla BlockEntity DataComponentMap key.
 */
class TeComponentsNbtMigrationTest
{
	@BeforeAll
	static void initComponents()
	{
		// Idempotent if already registered by another test class.
		try
		{
			Components.init();
		}
		catch (IllegalStateException ignored)
		{
			// already initialized
		}
	}

	@Test
	void read_prefersModernKey()
	{
		CompoundTag nbt = new CompoundTag();
		CompoundTag modern = new CompoundTag();
		CompoundTag energy = new CompoundTag();
		energy.putDouble("energy_buffer", 500.0);
		modern.put("energy", energy);
		nbt.put(Ic2rTileEntity.NBT_TE_COMPONENTS, modern);

		CompoundTag legacy = new CompoundTag();
		CompoundTag energyLegacy = new CompoundTag();
		energyLegacy.putDouble("storage", 1.0);
		legacy.put("energy", energyLegacy);
		nbt.put(Ic2rTileEntity.LEGACY_NBT_TE_COMPONENTS, legacy);

		CompoundTag read = Ic2rTileEntity.readTeComponentsNbt(nbt);
		assertNotNull(read);
		assertEquals(500.0, read.getCompound("energy").getDouble("energy_buffer"), 0.0);
	}

	@Test
	void read_fallsBackToLegacyWhenLooksLikeIc2r()
	{
		CompoundTag nbt = new CompoundTag();
		CompoundTag legacy = new CompoundTag();
		CompoundTag energy = new CompoundTag();
		energy.putDouble("storage", 999.0);
		legacy.put("energy", energy);
		CompoundTag fluid = new CompoundTag();
		legacy.put("fluid", fluid);
		nbt.put(Ic2rTileEntity.LEGACY_NBT_TE_COMPONENTS, legacy);

		assertTrue(Ic2rTileEntity.looksLikeIc2rTeComponents(legacy));
		CompoundTag read = Ic2rTileEntity.readTeComponentsNbt(nbt);
		assertNotNull(read);
		assertEquals(999.0, read.getCompound("energy").getDouble("storage"), 0.0);
	}

	@Test
	void read_ignoresVanillaStyleComponentsMap()
	{
		CompoundTag nbt = new CompoundTag();
		CompoundTag vanilla = new CompoundTag();
		// Namespaced keys are DataComponent types, not IC2R component ids.
		vanilla.putString("minecraft:custom_name", "{\"text\":\"x\"}");
		nbt.put(Ic2rTileEntity.LEGACY_NBT_TE_COMPONENTS, vanilla);

		assertNull(Ic2rTileEntity.readTeComponentsNbt(nbt));
	}

	@Test
	void constants_documentCollisionFix()
	{
		assertEquals("ic2r_components", Ic2rTileEntity.NBT_TE_COMPONENTS);
		assertEquals("components", Ic2rTileEntity.LEGACY_NBT_TE_COMPONENTS);
	}
}
