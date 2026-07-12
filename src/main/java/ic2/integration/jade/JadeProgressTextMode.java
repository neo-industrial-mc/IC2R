package ic2.integration.jade;

/**
 * How progress labels are formatted on Jade.
 * <ul>
 *   <li>{@link #PERCENT} — e.g. {@code 45%}</li>
 *   <li>{@link #FRACTION} — elapsed / total recipe time, e.g. {@code 1.5s / 15.0s}</li>
 *   <li>{@link #BOTH} — time plus percent, e.g. {@code 1.5s / 15.0s (10%)} (default)</li>
 *   <li>{@link #NONE} — bar only, no text</li>
 * </ul>
 * Registered as a Jade plugin enum config (cycle button in-game).
 */
public enum JadeProgressTextMode
{
	PERCENT,
	FRACTION,
	BOTH,
	NONE
}
