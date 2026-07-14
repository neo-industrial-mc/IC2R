package me.halfcooler.ic2r.integration.jade;

/**
 * Controls when a Jade tooltip line or bar is shown.
 * <ul>
 *   <li>{@link #ALWAYS} — always visible</li>
 *   <li>{@link #SHIFT} — only while holding the Jade details key (default Shift)</li>
 *   <li>{@link #NEVER} — hidden</li>
 * </ul>
 * Registered as a Jade plugin enum config (cycle button in-game).
 */
public enum JadeDisplayMode
{
	ALWAYS,
	SHIFT,
	NEVER;

	public boolean isVisible(boolean showDetails)
	{
		return switch (this)
		{
			case ALWAYS -> true;
			case SHIFT -> showDetails;
			case NEVER -> false;
		};
	}
}
