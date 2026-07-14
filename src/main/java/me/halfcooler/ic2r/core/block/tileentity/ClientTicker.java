package me.halfcooler.ic2r.core.block.tileentity;

/**
 * Explicit opt-in for client-side {@link Ic2rTileEntity#updateEntityClient()} world ticks (W1.3).
 * <p>
 * Replaces reflective {@code getDeclaredMethod("updateEntityClient")} probing.
 * Implement on the highest class in a hierarchy that owns real client-tick logic;
 * subclasses inherit the subscription.
 */
public interface ClientTicker
{
}
