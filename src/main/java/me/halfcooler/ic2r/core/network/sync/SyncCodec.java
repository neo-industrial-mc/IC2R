package me.halfcooler.ic2r.core.network.sync;

import me.halfcooler.ic2r.api.network.IGrowingBuffer;

import java.io.IOException;

public interface SyncCodec<T>
{
	void encode(IGrowingBuffer out, T value) throws IOException;

	T decode(IGrowingBuffer in) throws IOException;
}
