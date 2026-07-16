package me.halfcooler.ic2r.core.network.sync;

import java.util.Objects;
import java.util.regex.Pattern;

public record SyncKey<T>(String wireName, SyncCodec<T> codec)
{
	private static final Pattern SNAKE_CASE = Pattern.compile("^[a-z][a-z0-9_]*$");

	public SyncKey
	{
		Objects.requireNonNull(wireName, "wireName");
		Objects.requireNonNull(codec, "codec");
		if (!isValidWireName(wireName))
		{
			throw new IllegalArgumentException(
				"SyncKey wire name must be snake_case (e.g. gui_progress, active), got: " + wireName
			);
		}
	}

	public static <T> SyncKey<T> of(String wireName, SyncCodec<T> codec)
	{
		return new SyncKey<>(wireName, codec);
	}
	
	public static boolean isValidWireName(String name)
	{
		return name != null && SNAKE_CASE.matcher(name).matches();
	}
}
