package me.halfcooler.ic2r.network;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityStandardMachine;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.sync.BlockEntitySync;
import me.halfcooler.ic2r.core.network.sync.SyncKey;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W1.2 standard-machine sync pilot: encode/decode of {@code gui_progress} + {@code active}
 * using the same SyncKey bindings as {@link TileEntityStandardMachine#bindStandardMachineSync}.
 * <p>
 * Aligns with golden suite NS-005. Pure logic — no Level / BlockEntity bootstrap.
 */
class StandardMachineSyncRoundTripTest
{
	/** @Spec NS-005: standard-machine SyncKey wire names are snake_case */
	@Test
	void standardMachineKeys_wireNames_areSnakeCase()
	{
		assertEquals("gui_progress", TileEntityStandardMachine.KEY_GUI_PROGRESS.wireName());
		assertEquals("active", TileEntityStandardMachine.KEY_ACTIVE.wireName());
		assertTrue(SyncKey.isValidWireName(TileEntityStandardMachine.KEY_GUI_PROGRESS.wireName()));
		assertTrue(SyncKey.isValidWireName(TileEntityStandardMachine.KEY_ACTIVE.wireName()));
		assertFalse(SyncKey.isValidWireName(TileEntityStandardMachine.LEGACY_GUI_PROGRESS_FIELD));
	}

	/** Dual-write: legacy reflection names remain distinct from modern wire names where needed */
	@Test
	void dualWrite_legacyFieldNames_preservedForReflectionPath()
	{
		assertEquals("guiProgress", TileEntityStandardMachine.LEGACY_GUI_PROGRESS_FIELD);
		assertEquals("active", TileEntityStandardMachine.LEGACY_ACTIVE_FIELD);
		// progress renames camel→snake; active already legal as wire name
		assertEquals(TileEntityStandardMachine.LEGACY_ACTIVE_FIELD, TileEntityStandardMachine.KEY_ACTIVE.wireName());
		assertFalse(
			TileEntityStandardMachine.LEGACY_GUI_PROGRESS_FIELD.equals(
				TileEntityStandardMachine.KEY_GUI_PROGRESS.wireName()
			)
		);
	}

	/** @Spec NS-005: gui_progress + active encode → decode restores values (standard-machine semantics) */
	@Test
	void standardMachineSync_encodeAllDecodeAll_restoresProgressAndActive() throws IOException
	{
		AtomicBoolean active = new AtomicBoolean(true);
		AtomicReference<Float> guiProgress = new AtomicReference<>(0.42f);

		BlockEntitySync encoder = new BlockEntitySync();
		TileEntityStandardMachine.bindStandardMachineSync(
			encoder,
			active::get,
			active::set,
			guiProgress::get,
			guiProgress::set
		);

		GrowingBuffer buffer = new GrowingBuffer(64);
		encoder.encodeAll(buffer);
		buffer.flip();

		AtomicBoolean active2 = new AtomicBoolean(false);
		AtomicReference<Float> guiProgress2 = new AtomicReference<>(0f);

		BlockEntitySync decoder = new BlockEntitySync();
		TileEntityStandardMachine.bindStandardMachineSync(
			decoder,
			active2::get,
			active2::set,
			guiProgress2::get,
			guiProgress2::set
		);
		decoder.decodeAll(buffer);

		assertTrue(active2.get());
		assertEquals(0.42f, guiProgress2.get(), 0f);
	}

	/** @Spec NS-005: idle / zero-progress boundary */
	@Test
	void standardMachineSync_inactiveZeroProgress_roundTrip() throws IOException
	{
		AtomicBoolean active = new AtomicBoolean(false);
		AtomicReference<Float> guiProgress = new AtomicReference<>(0f);

		BlockEntitySync encoder = new BlockEntitySync();
		TileEntityStandardMachine.bindStandardMachineSync(
			encoder, active::get, active::set, guiProgress::get, guiProgress::set
		);

		GrowingBuffer buffer = new GrowingBuffer(32);
		encoder.encodeAll(buffer);
		buffer.flip();

		AtomicBoolean active2 = new AtomicBoolean(true);
		AtomicReference<Float> guiProgress2 = new AtomicReference<>(1f);
		BlockEntitySync decoder = new BlockEntitySync();
		TileEntityStandardMachine.bindStandardMachineSync(
			decoder, active2::get, active2::set, guiProgress2::get, guiProgress2::set
		);
		decoder.decodeAll(buffer);

		assertFalse(active2.get());
		assertEquals(0f, guiProgress2.get(), 0f);
	}

	/** @Spec NS-005: full progress while active (completion edge) */
	@Test
	void standardMachineSync_fullProgressActive_roundTrip() throws IOException
	{
		AtomicBoolean active = new AtomicBoolean(true);
		AtomicReference<Float> guiProgress = new AtomicReference<>(1f);

		BlockEntitySync encoder = new BlockEntitySync();
		TileEntityStandardMachine.bindStandardMachineSync(
			encoder, active::get, active::set, guiProgress::get, guiProgress::set
		);

		GrowingBuffer buffer = new GrowingBuffer(32);
		encoder.encodeAll(buffer);
		buffer.flip();

		AtomicBoolean active2 = new AtomicBoolean(false);
		AtomicReference<Float> guiProgress2 = new AtomicReference<>(-1f);
		BlockEntitySync decoder = new BlockEntitySync();
		TileEntityStandardMachine.bindStandardMachineSync(
			decoder, active2::get, active2::set, guiProgress2::get, guiProgress2::set
		);
		decoder.decodeAll(buffer);

		assertTrue(active2.get());
		assertEquals(1f, guiProgress2.get(), 0f);
	}

	@Test
	void bindStandardMachineSync_registersExpectedKeysAndOrder()
	{
		BlockEntitySync sync = new BlockEntitySync();
		TileEntityStandardMachine.bindStandardMachineSync(
			sync, () -> false, v ->
			{
			}, () -> 0f, v ->
			{
			}
		);

		assertEquals(2, sync.size());
		assertSame(TileEntityStandardMachine.KEY_ACTIVE, sync.fields().get(0).key());
		assertSame(TileEntityStandardMachine.KEY_GUI_PROGRESS, sync.fields().get(1).key());
		assertEquals(TileEntityStandardMachine.KEY_ACTIVE, sync.get("active").key());
		assertEquals(TileEntityStandardMachine.KEY_GUI_PROGRESS, sync.get("gui_progress").key());
	}

	/** G1.1: legacy TeUpdate names map to the same Sync fields as modern wire names */
	@Test
	void lookup_resolvesWireNameThenLegacyAlias()
	{
		BlockEntitySync sync = new BlockEntitySync();
		TileEntityStandardMachine.bindStandardMachineSync(
			sync, () -> false, v ->
			{
			}, () -> 0f, v ->
			{
			}
		);

		assertSame(sync.get("gui_progress"), sync.lookup("gui_progress"));
		assertSame(sync.get("gui_progress"), sync.lookup(TileEntityStandardMachine.LEGACY_GUI_PROGRESS_FIELD));
		assertSame(sync.get("active"), sync.lookup("active"));
		assertSame(sync.get("active"), sync.lookup(TileEntityStandardMachine.LEGACY_ACTIVE_FIELD));
		assertEquals(null, sync.lookup("unknownField"));
		assertEquals(null, sync.get(TileEntityStandardMachine.LEGACY_GUI_PROGRESS_FIELD));
	}

	/**
	 * G1.1 / NS-005: TeUpdate write path uses Sync getter under legacy packet names;
	 * values match direct getters (what DataEncoder would encode).
	 */
	@Test
	void tryGetValue_prefersSyncGetter_forLegacyAndWireNames()
	{
		AtomicBoolean active = new AtomicBoolean(true);
		AtomicReference<Float> guiProgress = new AtomicReference<>(0.75f);
		AtomicBoolean getterUsed = new AtomicBoolean(false);

		BlockEntitySync sync = new BlockEntitySync();
		TileEntityStandardMachine.bindStandardMachineSync(
			sync,
			() ->
			{
				getterUsed.set(true);
				return active.get();
			},
			active::set,
			guiProgress::get,
			guiProgress::set
		);

		Object[] out = new Object[1];
		assertTrue(sync.tryGetValue(TileEntityStandardMachine.LEGACY_GUI_PROGRESS_FIELD, out));
		assertEquals(0.75f, (Float) out[0], 0f);

		assertTrue(sync.tryGetValue("gui_progress", out));
		assertEquals(0.75f, (Float) out[0], 0f);

		assertTrue(sync.tryGetValue(TileEntityStandardMachine.LEGACY_ACTIVE_FIELD, out));
		assertEquals(Boolean.TRUE, out[0]);
		assertTrue(getterUsed.get());

		assertFalse(sync.tryGetValue("energy", out));
	}

	/**
	 * G1.1: TeUpdate apply path uses Sync setter under legacy names (no reflection required).
	 */
	@Test
	void trySetValue_prefersSyncSetter_updatesMockState()
	{
		AtomicBoolean active = new AtomicBoolean(false);
		AtomicReference<Float> guiProgress = new AtomicReference<>(0f);
		AtomicBoolean setterUsed = new AtomicBoolean(false);

		BlockEntitySync sync = new BlockEntitySync();
		TileEntityStandardMachine.bindStandardMachineSync(
			sync,
			active::get,
			active::set,
			guiProgress::get,
			v ->
			{
				setterUsed.set(true);
				guiProgress.set(v);
			}
		);

		assertTrue(sync.trySetValue(TileEntityStandardMachine.LEGACY_GUI_PROGRESS_FIELD, 0.33f));
		assertEquals(0.33f, guiProgress.get(), 0f);
		assertTrue(setterUsed.get());

		assertTrue(sync.trySetValue("active", Boolean.TRUE));
		assertTrue(active.get());

		assertFalse(sync.trySetValue("notRegistered", 1));
	}
}
