package me.halfcooler.ic2r.core.network.sync;

import me.halfcooler.ic2r.api.network.IGrowingBuffer;

import java.io.IOException;

/**
 * Type-safe encode/decode for a single synced value.
 * <p>
 * Wire format is owned by the codec (typically raw primitives on {@link IGrowingBuffer}).
 * {@code GrowingBuffer} remains an internal buffer implementation; call sites should prefer this API
 * over reflection field names.
 */
public interface SyncCodec<T>
{
	void encode(IGrowingBuffer out, T value) throws IOException;

	T decode(IGrowingBuffer in) throws IOException;
}
