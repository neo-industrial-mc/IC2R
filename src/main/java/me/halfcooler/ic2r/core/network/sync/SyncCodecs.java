package me.halfcooler.ic2r.core.network.sync;

import me.halfcooler.ic2r.api.network.IGrowingBuffer;

import java.io.IOException;
import java.util.Objects;

/**
 * Built-in codecs for common primitive / simple network payload types.
 * Value bytes only — wire names are handled by {@link SyncKey} / {@link BlockEntitySync}.
 */
public final class SyncCodecs
{
	public static final SyncCodec<Boolean> BOOLEAN = new SyncCodec<>()
	{
		@Override
		public void encode(IGrowingBuffer out, Boolean value) throws IOException
		{
			out.writeBoolean(Objects.requireNonNull(value, "value"));
		}

		@Override
		public Boolean decode(IGrowingBuffer in) throws IOException
		{
			return in.readBoolean();
		}
	};

	public static final SyncCodec<Byte> BYTE = new SyncCodec<>()
	{
		@Override
		public void encode(IGrowingBuffer out, Byte value) throws IOException
		{
			out.writeByte(Objects.requireNonNull(value, "value"));
		}

		@Override
		public Byte decode(IGrowingBuffer in) throws IOException
		{
			return in.readByte();
		}
	};

	public static final SyncCodec<Short> SHORT = new SyncCodec<>()
	{
		@Override
		public void encode(IGrowingBuffer out, Short value) throws IOException
		{
			out.writeShort(Objects.requireNonNull(value, "value"));
		}

		@Override
		public Short decode(IGrowingBuffer in) throws IOException
		{
			return in.readShort();
		}
	};

	public static final SyncCodec<Integer> INT = new SyncCodec<>()
	{
		@Override
		public void encode(IGrowingBuffer out, Integer value) throws IOException
		{
			out.writeInt(Objects.requireNonNull(value, "value"));
		}

		@Override
		public Integer decode(IGrowingBuffer in) throws IOException
		{
			return in.readInt();
		}
	};

	public static final SyncCodec<Long> LONG = new SyncCodec<>()
	{
		@Override
		public void encode(IGrowingBuffer out, Long value) throws IOException
		{
			out.writeLong(Objects.requireNonNull(value, "value"));
		}

		@Override
		public Long decode(IGrowingBuffer in) throws IOException
		{
			return in.readLong();
		}
	};

	public static final SyncCodec<Float> FLOAT = new SyncCodec<>()
	{
		@Override
		public void encode(IGrowingBuffer out, Float value) throws IOException
		{
			out.writeFloat(Objects.requireNonNull(value, "value"));
		}

		@Override
		public Float decode(IGrowingBuffer in) throws IOException
		{
			return in.readFloat();
		}
	};

	public static final SyncCodec<Double> DOUBLE = new SyncCodec<>()
	{
		@Override
		public void encode(IGrowingBuffer out, Double value) throws IOException
		{
			out.writeDouble(Objects.requireNonNull(value, "value"));
		}

		@Override
		public Double decode(IGrowingBuffer in) throws IOException
		{
			return in.readDouble();
		}
	};

	public static final SyncCodec<String> STRING = new SyncCodec<>()
	{
		@Override
		public void encode(IGrowingBuffer out, String value) throws IOException
		{
			out.writeString(Objects.requireNonNull(value, "value"));
		}

		@Override
		public String decode(IGrowingBuffer in) throws IOException
		{
			return in.readString();
		}
	};

	private SyncCodecs()
	{
	}
}
