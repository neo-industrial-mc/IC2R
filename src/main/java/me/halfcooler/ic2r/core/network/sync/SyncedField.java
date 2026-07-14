package me.halfcooler.ic2r.core.network.sync;

import me.halfcooler.ic2r.api.network.IGrowingBuffer;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Binding of a {@link SyncKey} to host get/set accessors for type-safe payload R/W.
 * <p>
 * Does not perform reflection; callers supply explicit accessors (W1.2+ machine migration).
 */
public final class SyncedField<T>
{
	private final SyncKey<T> key;
	private final Supplier<T> getter;
	private final Consumer<T> setter;

	public SyncedField(SyncKey<T> key, Supplier<T> getter, Consumer<T> setter)
	{
		this.key = Objects.requireNonNull(key, "key");
		this.getter = Objects.requireNonNull(getter, "getter");
		this.setter = Objects.requireNonNull(setter, "setter");
	}

	public SyncKey<T> key()
	{
		return this.key;
	}

	public T get()
	{
		return this.getter.get();
	}

	public void set(T value)
	{
		this.setter.accept(value);
	}

	/** Encodes the current value (payload bytes only; no wire name). */
	public void encodeValue(IGrowingBuffer out) throws IOException
	{
		this.key.codec().encode(out, this.get());
	}

	/** Decodes a value and applies it via the setter. */
	public void decodeValue(IGrowingBuffer in) throws IOException
	{
		this.set(this.key.codec().decode(in));
	}

	/**
	 * Encodes {@code wireName} then value — convenient unit for future packet assembly.
	 */
	public void encodeNamed(IGrowingBuffer out) throws IOException
	{
		out.writeString(this.key.wireName());
		this.encodeValue(out);
	}
}
