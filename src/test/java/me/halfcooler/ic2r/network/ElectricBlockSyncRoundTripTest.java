package me.halfcooler.ic2r.network;

import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityElectricBlock;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.sync.BlockEntitySync;
import me.halfcooler.ic2r.core.network.sync.SyncKey;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * G1.5 ElectricBlock sync: {@code redstone_mode} + legacy alias {@code redstoneMode}.
 * Pure logic — no Level / BlockEntity bootstrap.
 */
class ElectricBlockSyncRoundTripTest
{
	@Test
	void electricBlockKeys_wireNames_areSnakeCase()
	{
		assertEquals("redstone_mode", TileEntityElectricBlock.KEY_REDSTONE_MODE.wireName());
		assertTrue(SyncKey.isValidWireName(TileEntityElectricBlock.KEY_REDSTONE_MODE.wireName()));
		assertFalse(SyncKey.isValidWireName(TileEntityElectricBlock.LEGACY_REDSTONE_MODE_FIELD));
		assertEquals("redstoneMode", TileEntityElectricBlock.LEGACY_REDSTONE_MODE_FIELD);
	}

	@Test
	void electricBlockSync_encodeAllDecodeAll_restoresMode() throws IOException
	{
		AtomicReference<Byte> mode = new AtomicReference<>((byte) 4);

		BlockEntitySync encoder = new BlockEntitySync();
		TileEntityElectricBlock.bindElectricBlockSync(encoder, mode::get, mode::set);

		GrowingBuffer buffer = new GrowingBuffer(32);
		encoder.encodeAll(buffer);
		buffer.flip();

		AtomicReference<Byte> mode2 = new AtomicReference<>((byte) 0);
		BlockEntitySync decoder = new BlockEntitySync();
		TileEntityElectricBlock.bindElectricBlockSync(decoder, mode2::get, mode2::set);
		decoder.decodeAll(buffer);

		assertEquals((byte) 4, mode2.get());
	}

	@Test
	void lookup_resolvesWireNameThenLegacyAlias()
	{
		BlockEntitySync sync = new BlockEntitySync();
		TileEntityElectricBlock.bindElectricBlockSync(sync, () -> (byte) 0, v ->
		{
		});

		assertSame(sync.get("redstone_mode"), sync.lookup("redstone_mode"));
		assertSame(sync.get("redstone_mode"), sync.lookup(TileEntityElectricBlock.LEGACY_REDSTONE_MODE_FIELD));
		assertEquals(null, sync.get(TileEntityElectricBlock.LEGACY_REDSTONE_MODE_FIELD));
	}

	@Test
	void tryGetValue_and_trySetValue_useSyncUnderLegacyName()
	{
		AtomicReference<Byte> mode = new AtomicReference<>((byte) 1);
		BlockEntitySync sync = new BlockEntitySync();
		TileEntityElectricBlock.bindElectricBlockSync(sync, mode::get, mode::set);

		Object[] out = new Object[1];
		assertTrue(sync.tryGetValue(TileEntityElectricBlock.LEGACY_REDSTONE_MODE_FIELD, out));
		assertEquals((byte) 1, out[0]);

		assertTrue(sync.trySetValue(TileEntityElectricBlock.LEGACY_REDSTONE_MODE_FIELD, (byte) 6));
		assertEquals((byte) 6, mode.get());

		assertTrue(sync.tryGetValue("redstone_mode", out));
		assertEquals((byte) 6, out[0]);
	}
}
