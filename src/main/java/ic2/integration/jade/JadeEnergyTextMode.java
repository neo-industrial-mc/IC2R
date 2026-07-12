package ic2.integration.jade;

/**
 * How the EU energy bar label is formatted on Jade.
 * <ul>
 *   <li>{@link #AMOUNT} — e.g. {@code 1.2k / 40k EU}</li>
 *   <li>{@link #PERCENT} — e.g. {@code 30%}</li>
 *   <li>{@link #BOTH} — e.g. {@code 1.2k / 40k EU (30%)}</li>
 *   <li>{@link #NONE} — bar only, no text</li>
 * </ul>
 * Registered as a Jade plugin enum config (cycle button in-game).
 */
public enum JadeEnergyTextMode
{
	AMOUNT,
	PERCENT,
	BOTH,
	NONE
}
