package ic2.integration.jade;

/**
 * How progress labels are formatted on Jade.
 * <ul>
 *   <li>{@link #PERCENT} — e.g. {@code 45%}</li>
 *   <li>{@link #FRACTION} — actual progress, e.g. {@code 120 / 300}</li>
 *   <li>{@link #BOTH} — e.g. {@code 120 / 300 (45%)}</li>
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
