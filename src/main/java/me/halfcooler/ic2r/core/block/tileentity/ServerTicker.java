package me.halfcooler.ic2r.core.block.tileentity;

/**
 * Explicit opt-in for server-side {@link Ic2rTileEntity#updateEntityServer()} world ticks (W1.3).
 * <p>
 * Replaces reflective {@code getDeclaredMethod("updateEntityServer")} probing.
 * Implement on the highest class in a hierarchy that owns real server-tick logic;
 * subclasses inherit the subscription.
 */
public interface ServerTicker
{
}
