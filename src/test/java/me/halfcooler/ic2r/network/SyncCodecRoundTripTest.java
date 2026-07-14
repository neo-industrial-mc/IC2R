package me.halfcooler.ic2r.network;

import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.sync.BlockEntitySync;
import me.halfcooler.ic2r.core.network.sync.SyncCodecs;
import me.halfcooler.ic2r.core.network.sync.SyncKey;
import me.halfcooler.ic2r.core.network.sync.SyncedField;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-logic SyncKey / SyncCodec round-trips (no Level / client).
 * Aligns with golden suite NS-005 spirit (encode → decode equality).
 */
class SyncCodecRoundTripTest
{
	/** @Spec NS-005 codec: boolean / int / double value round-trip */
	@Test
	void primitiveCodecs_encodeDecode_equal() throws IOException
	{
		assertRoundTrip(SyncCodecs.BOOLEAN, true);
		assertRoundTrip(SyncCodecs.BOOLEAN, false);
		assertRoundTrip(SyncCodecs.INT, 0);
		assertRoundTrip(SyncCodecs.INT, 42);
		assertRoundTrip(SyncCodecs.INT, -7);
		assertRoundTrip(SyncCodecs.DOUBLE, 0.0);
		assertRoundTrip(SyncCodecs.DOUBLE, 3.141592653589793);
		assertRoundTrip(SyncCodecs.FLOAT, 1.5f);
		assertRoundTrip(SyncCodecs.LONG, Long.MAX_VALUE);
		assertRoundTrip(SyncCodecs.STRING, "gui_progress");
	}

	/** @Spec NS-005: SyncKey wire names are snake_case only */
	@Test
	void syncKey_rejectsNonSnakeCaseWireNames()
	{
		assertTrue(SyncKey.isValidWireName("active"));
		assertTrue(SyncKey.isValidWireName("gui_progress"));
		assertFalse(SyncKey.isValidWireName("guiProgress"));
		assertFalse(SyncKey.isValidWireName("Gui_progress"));
		assertFalse(SyncKey.isValidWireName(""));
		assertFalse(SyncKey.isValidWireName("1bad"));

		assertThrows(IllegalArgumentException.class, () -> SyncKey.of("guiProgress", SyncCodecs.INT));
		SyncKey.of("gui_progress", SyncCodecs.INT); // valid
	}

	/** @Spec NS-005: registry encodeAll → decodeAll restores field values */
	@Test
	void blockEntitySync_encodeAllDecodeAll_restoresValues() throws IOException
	{
		AtomicBoolean active = new AtomicBoolean(true);
		AtomicInteger progress = new AtomicInteger(75);
		AtomicReference<Double> energy = new AtomicReference<>(128.5);

		BlockEntitySync encoder = new BlockEntitySync();
		encoder.add(SyncKey.of("active", SyncCodecs.BOOLEAN), active::get, active::set);
		encoder.add(SyncKey.of("gui_progress", SyncCodecs.INT), progress::get, progress::set);
		encoder.add(SyncKey.of("energy_display", SyncCodecs.DOUBLE), energy::get, energy::set);

		GrowingBuffer buffer = new GrowingBuffer(64);
		encoder.encodeAll(buffer);
		buffer.flip();

		AtomicBoolean active2 = new AtomicBoolean(false);
		AtomicInteger progress2 = new AtomicInteger(0);
		AtomicReference<Double> energy2 = new AtomicReference<>(0.0);

		BlockEntitySync decoder = new BlockEntitySync();
		decoder.add(SyncKey.of("active", SyncCodecs.BOOLEAN), active2::get, active2::set);
		decoder.add(SyncKey.of("gui_progress", SyncCodecs.INT), progress2::get, progress2::set);
		decoder.add(SyncKey.of("energy_display", SyncCodecs.DOUBLE), energy2::get, energy2::set);
		decoder.decodeAll(buffer);

		assertTrue(active2.get());
		assertEquals(75, progress2.get());
		assertEquals(128.5, energy2.get());
	}

	/** Single named field payload: wireName + value */
	@Test
	void syncedField_encodeNamed_roundTrip() throws IOException
	{
		AtomicInteger value = new AtomicInteger(9);
		SyncedField<Integer> field = new SyncedField<>(
			SyncKey.of("gui_progress", SyncCodecs.INT),
			value::get,
			value::set
		);

		GrowingBuffer buffer = new GrowingBuffer(32);
		field.encodeNamed(buffer);
		buffer.flip();

		assertEquals("gui_progress", buffer.readString());
		AtomicInteger decoded = new AtomicInteger();
		SyncedField<Integer> sink = new SyncedField<>(
			SyncKey.of("gui_progress", SyncCodecs.INT),
			decoded::get,
			decoded::set
		);
		sink.decodeValue(buffer);
		assertEquals(9, decoded.get());
	}

	@Test
	void blockEntitySync_duplicateWireName_throws()
	{
		BlockEntitySync sync = new BlockEntitySync();
		sync.add(SyncKey.of("active", SyncCodecs.BOOLEAN), () -> true, v ->
		{
		});
		assertThrows(IllegalArgumentException.class, () ->
			sync.add(SyncKey.of("active", SyncCodecs.BOOLEAN), () -> false, v ->
			{
			})
		);
	}

	/**
	 * G3.3 Sync boundary: empty registry, legacy alias lookup, tryGet/trySet fallback,
	 * unknown wire on decode fails hard (protocol bug).
	 */
	@Test
	void blockEntitySync_legacyLookup_tryGetSet_andUnknownWire() throws IOException
	{
		BlockEntitySync empty = new BlockEntitySync();
		assertTrue(empty.isEmpty());
		assertEquals(0, empty.size());
		assertEquals(null, empty.lookup(null));
		assertEquals(null, empty.lookup("gui_progress"));

		AtomicInteger progress = new AtomicInteger(12);
		BlockEntitySync sync = new BlockEntitySync();
		sync.add(
			SyncKey.of("gui_progress", SyncCodecs.INT),
			progress::get,
			progress::set,
			"guiProgress" // legacy TeUpdate name
		);
		assertFalse(sync.isEmpty());
		assertEquals(1, sync.size());

		// modern wire + legacy alias resolve to the same field
		assertEquals(sync.get("gui_progress"), sync.lookup("gui_progress"));
		assertEquals(sync.get("gui_progress"), sync.lookup("guiProgress"));
		assertEquals(null, sync.lookup("unknown_field"));

		Object[] out = new Object[1];
		assertTrue(sync.tryGetValue("guiProgress", out));
		assertEquals(12, out[0]);
		assertFalse(sync.tryGetValue("missing", out));
		assertThrows(IllegalArgumentException.class, () -> sync.tryGetValue("gui_progress", new Object[0]));

		assertTrue(sync.trySetValue("guiProgress", 99));
		assertEquals(99, progress.get());
		assertFalse(sync.trySetValue("missing", 1));

		// encode then decode with missing key → IOException
		GrowingBuffer buffer = new GrowingBuffer(32);
		sync.encodeAll(buffer);
		buffer.flip();
		BlockEntitySync other = new BlockEntitySync();
		other.add(SyncKey.of("active", SyncCodecs.BOOLEAN), () -> false, v ->
		{
		});
		assertThrows(IOException.class, () -> other.decodeAll(buffer));
	}

	private static <T> void assertRoundTrip(me.halfcooler.ic2r.core.network.sync.SyncCodec<T> codec, T value)
		throws IOException
	{
		GrowingBuffer buffer = new GrowingBuffer(32);
		codec.encode(buffer, value);
		buffer.flip();
		assertEquals(value, codec.decode(buffer));
	}
}
