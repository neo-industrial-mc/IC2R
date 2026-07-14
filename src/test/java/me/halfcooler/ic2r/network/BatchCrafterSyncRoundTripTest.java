package me.halfcooler.ic2r.network;

import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityBatchCrafter;
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
 * G1.5 BatchCrafter sync expansion: {@code gui_progress} + legacy alias {@code guiProgress}.
 * Pure logic — no Level / BlockEntity bootstrap.
 */
class BatchCrafterSyncRoundTripTest
{
	@Test
	void batchCrafterKeys_wireNames_areSnakeCase()
	{
		assertEquals("gui_progress", TileEntityBatchCrafter.KEY_GUI_PROGRESS.wireName());
		assertTrue(SyncKey.isValidWireName(TileEntityBatchCrafter.KEY_GUI_PROGRESS.wireName()));
		assertFalse(SyncKey.isValidWireName(TileEntityBatchCrafter.LEGACY_GUI_PROGRESS_FIELD));
		assertEquals("guiProgress", TileEntityBatchCrafter.LEGACY_GUI_PROGRESS_FIELD);
	}

	@Test
	void batchCrafterSync_encodeAllDecodeAll_restoresProgress() throws IOException
	{
		AtomicReference<Float> guiProgress = new AtomicReference<>(0.55f);

		BlockEntitySync encoder = new BlockEntitySync();
		TileEntityBatchCrafter.bindBatchCrafterSync(encoder, guiProgress::get, guiProgress::set);

		GrowingBuffer buffer = new GrowingBuffer(64);
		encoder.encodeAll(buffer);
		buffer.flip();

		AtomicReference<Float> guiProgress2 = new AtomicReference<>(0f);
		BlockEntitySync decoder = new BlockEntitySync();
		TileEntityBatchCrafter.bindBatchCrafterSync(decoder, guiProgress2::get, guiProgress2::set);
		decoder.decodeAll(buffer);

		assertEquals(0.55f, guiProgress2.get(), 0f);
	}

	@Test
	void lookup_resolvesWireNameThenLegacyAlias()
	{
		BlockEntitySync sync = new BlockEntitySync();
		TileEntityBatchCrafter.bindBatchCrafterSync(sync, () -> 0f, v ->
		{
		});

		assertSame(sync.get("gui_progress"), sync.lookup("gui_progress"));
		assertSame(sync.get("gui_progress"), sync.lookup(TileEntityBatchCrafter.LEGACY_GUI_PROGRESS_FIELD));
		assertEquals(null, sync.get(TileEntityBatchCrafter.LEGACY_GUI_PROGRESS_FIELD));
	}

	@Test
	void tryGetValue_and_trySetValue_useSyncUnderLegacyName()
	{
		AtomicReference<Float> guiProgress = new AtomicReference<>(0.2f);
		BlockEntitySync sync = new BlockEntitySync();
		TileEntityBatchCrafter.bindBatchCrafterSync(sync, guiProgress::get, guiProgress::set);

		Object[] out = new Object[1];
		assertTrue(sync.tryGetValue(TileEntityBatchCrafter.LEGACY_GUI_PROGRESS_FIELD, out));
		assertEquals(0.2f, (Float) out[0], 0f);

		assertTrue(sync.trySetValue(TileEntityBatchCrafter.LEGACY_GUI_PROGRESS_FIELD, 0.9f));
		assertEquals(0.9f, guiProgress.get(), 0f);
	}

	@Test
	void progressNbt_writeUsesProgressKeyOnly()
	{
		net.minecraft.nbt.CompoundTag out = new net.minecraft.nbt.CompoundTag();
		TileEntityBatchCrafter.writeProgressNbt(out, (short) 17);
		assertEquals((short) 17, out.getShort(TileEntityBatchCrafter.NBT_PROGRESS));
		assertFalse(out.contains("guiProgress"));
		assertFalse(out.contains("gui_progress"));
		assertEquals((short) 17, TileEntityBatchCrafter.readProgressNbt(out));
	}
}
