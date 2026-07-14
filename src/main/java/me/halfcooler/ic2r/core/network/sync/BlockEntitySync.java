package me.halfcooler.ic2r.core.network.sync;

import me.halfcooler.ic2r.api.network.IGrowingBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Per-block-entity registry of {@link SyncedField}s for the modern sync path.
 * <p>
 * G1.1: TeUpdate / {@code NetworkManager.writeFieldData} prefer this table for registered fields
 * (standard-machine {@code gui_progress}/{@code active}). Legacy packet field names (e.g.
 * {@code guiProgress}) resolve via aliases so the DataEncoder + string-name wire format stays
 * compatible. Unregistered fields still use reflection.
 */
public final class BlockEntitySync
{
	private final Map<String, SyncedField<?>> byWireName = new LinkedHashMap<>();
	/** Legacy TeUpdate / {@code getNetworkedFields()} names → same field as modern wire. */
	private final Map<String, SyncedField<?>> byLegacyName = new HashMap<>();
	private final List<SyncedField<?>> fields = new ArrayList<>();

	/**
	 * Registers a type-safe synced field. Wire name must be unique within this registry.
	 */
	public <T> SyncedField<T> add(SyncKey<T> key, Supplier<T> getter, Consumer<T> setter)
	{
		return this.add(key, getter, setter, (String[]) null);
	}

	/**
	 * Registers a synced field and optional legacy names used on the TeUpdate wire
	 * ({@code getNetworkedFields()} / reflection field names).
	 * <p>
	 * Aliases equal to the modern wire name are ignored (already registered under wire).
	 */
	public <T> SyncedField<T> add(SyncKey<T> key, Supplier<T> getter, Consumer<T> setter, String... legacyAliases)
	{
		Objects.requireNonNull(key, "key");
		if (this.byWireName.containsKey(key.wireName()) || this.byLegacyName.containsKey(key.wireName()))
		{
			throw new IllegalArgumentException("duplicate SyncKey wire name: " + key.wireName());
		}

		SyncedField<T> field = new SyncedField<>(key, getter, setter);
		this.fields.add(field);
		this.byWireName.put(key.wireName(), field);

		if (legacyAliases != null)
		{
			for (String alias : legacyAliases)
			{
				if (alias == null || alias.isEmpty() || alias.equals(key.wireName()))
				{
					continue;
				}

				if (this.byWireName.containsKey(alias) || this.byLegacyName.containsKey(alias))
				{
					throw new IllegalArgumentException("duplicate Sync legacy alias: " + alias);
				}

				this.byLegacyName.put(alias, field);
			}
		}

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

	/** Lookup by modern snake_case wire name only. */
	public SyncedField<?> get(String wireName)
	{
		return this.byWireName.get(wireName);
	}

	/**
	 * Resolve a field for TeUpdate / network R/W: modern wire name first, then legacy alias.
	 *
	 * @return registered field, or {@code null} if this sync table does not own the name
	 */
	public SyncedField<?> lookup(String name)
	{
		if (name == null)
		{
			return null;
		}

		SyncedField<?> field = this.byWireName.get(name);
		if (field != null)
		{
			return field;
		}

		return this.byLegacyName.get(name);
	}

	/**
	 * Apply a decoded TeUpdate value via Sync setter when {@code name} is registered.
	 *
	 * @return {@code true} if applied via Sync; {@code false} if caller should use reflection
	 */
	@SuppressWarnings("unchecked")
	public boolean trySetValue(String name, Object value)
	{
		SyncedField<?> field = this.lookup(name);
		if (field == null)
		{
			return false;
		}

		((SyncedField<Object>) field).set(value);
		return true;
	}

	/**
	 * Read current value via Sync getter when {@code name} is registered.
	 *
	 * @return {@code true} if resolved via Sync ({@code out[0]} holds the value, may be null);
	 * {@code false} if caller should use reflection
	 */
	public boolean tryGetValue(String name, Object[] out)
	{
		Objects.requireNonNull(out, "out");
		if (out.length < 1)
		{
			throw new IllegalArgumentException("out must have length >= 1");
		}

		SyncedField<?> field = this.lookup(name);
		if (field == null)
		{
			return false;
		}

		out[0] = field.get();
		return true;
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
