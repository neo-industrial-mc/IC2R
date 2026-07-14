package me.halfcooler.ic2r.nbt;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityStandardMachine;

import net.minecraft.nbt.CompoundTag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W1.5 standard-machine NBT pilot.
 * <p>
 * Process progress was already a single-segment lowercase key ({@code progress}) and is retained.
 * Network GUI fraction remains dual-write {@code gui_progress} / legacy field {@code guiProgress} (W1.2).
 * Aligns with golden suite NS-003 (snake_case write) for the standard-machine domain.
 * Pure logic — no Level / BlockEntity bootstrap.
 */
class StandardMachineNbtMigrationTest
{
	/** @Spec NS-003: standard machine writes only snake_case-legal {@code progress} */
	@Test
	void progress_write_usesSnakeCaseKeyOnly()
	{
		assertEquals("progress", TileEntityStandardMachine.NBT_PROGRESS);

		CompoundTag out = new CompoundTag();
		TileEntityStandardMachine.writeProgressNbt(out, (short) 42);

		assertTrue(out.contains(TileEntityStandardMachine.NBT_PROGRESS));
		assertEquals((short) 42, out.getShort(TileEntityStandardMachine.NBT_PROGRESS));
		// No camelCase twin for world-save progress (guiProgress is network-only)
		assertFalse(out.contains("guiProgress"));
		assertFalse(out.contains("gui_progress"));
	}

	/** @Spec NS-003: new-key round-trip preserves progress ticks */
	@Test
	void progress_newKey_roundTrip()
	{
		CompoundTag tag = new CompoundTag();
		TileEntityStandardMachine.writeProgressNbt(tag, (short) 300);
		assertEquals((short) 300, TileEntityStandardMachine.readProgressNbt(tag));
	}

	/**
	 * @Spec NS-001-style: existing archives that only have {@code progress} still load
	 * (key was already modern; migration is "retain + explicit constant").
	 */
	@Test
	void progress_existingArchiveKey_isReadable()
	{
		CompoundTag archive = new CompoundTag();
		archive.putShort("progress", (short) 15);
		assertEquals((short) 15, TileEntityStandardMachine.readProgressNbt(archive));
	}
}
