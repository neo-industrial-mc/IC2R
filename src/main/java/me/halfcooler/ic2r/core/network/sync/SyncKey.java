package me.halfcooler.ic2r.core.network.sync;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Logical network sync key: identity + codec, with a {@code snake_case} wire name.
 * <p>
 * Replaces ad-hoc Java field-name strings from {@code getNetworkedFields()} for new code paths.
 * Example: logical progress field → wire name {@code gui_progress}.
 *
 * @param <T> value type carried on the wire for this key
 */
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

	/**
	 * {@code snake_case}: starts with a lowercase letter; only {@code [a-z0-9_]} thereafter.
	 */
	public static boolean isValidWireName(String name)
	{
		return name != null && SNAKE_CASE.matcher(name).matches();
	}
}
