package me.halfcooler.ic2r.core.network.sync;

import me.halfcooler.ic2r.api.network.IGrowingBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Per-block-entity registry of {@link SyncedField}s for the modern sync path.
 * <p>
 * Skeleton only (W1.1): machines still use {@code getNetworkedFields()} + reflection until W1.2 dual-write.
 * Encode/decode helpers support golden-style round-trips and future {@code TeUpdate} integration.
 */
public final class BlockEntitySync
{
	private final Map<String, SyncedField<?>> byWireName = new LinkedHashMap<>();
	private final List<SyncedField<?>> fields = new ArrayList<>();

	/**
	 * Registers a type-safe synced field. Wire name must be unique within this registry.
	 */
	public <T> SyncedField<T> add(SyncKey<T> key, Supplier<T> getter, Consumer<T> setter)
	{
		Objects.requireNonNull(key, "key");
		if (this.byWireName.containsKey(key.wireName()))
		{
			throw new IllegalArgumentException("duplicate SyncKey wire name: " + key.wireName());
		}

		SyncedField<T> field = new SyncedField<>(key, getter, setter);
		this.fields.add(field);
		this.byWireName.put(key.wireName(), field);
		return field;
	}

	public boolean isEmpty()
	{
		return this.fields.isEmpty();
	}

	public int size()
	{
		return this.fields.size();
	}

	public List<SyncedField<?>> fields()
	{
		return Collections.unmodifiableList(this.fields);
	}

	public SyncedField<?> get(String wireName)
	{
		return this.byWireName.get(wireName);
	}

	/**
	 * Payload: {@code varInt(count)} then for each field {@code string(wireName) + value}.
	 * Order is registration order.
	 */
	public void encodeAll(IGrowingBuffer out) throws IOException
	{
		out.writeVarInt(this.fields.size());
		for (SyncedField<?> field : this.fields)
		{
			field.encodeNamed(out);
		}
	}

	/**
	 * Inverse of {@link #encodeAll(IGrowingBuffer)}. Unknown wire names fail hard (protocol bug).
	 */
	public void decodeAll(IGrowingBuffer in) throws IOException
	{
		int count = in.readVarInt();
		for (int i = 0; i < count; i++)
		{
			String wireName = in.readString();
			SyncedField<?> field = this.byWireName.get(wireName);
			if (field == null)
			{
				throw new IOException("unknown SyncKey on wire: " + wireName);
			}

			field.decodeValue(in);
		}
	}
}
