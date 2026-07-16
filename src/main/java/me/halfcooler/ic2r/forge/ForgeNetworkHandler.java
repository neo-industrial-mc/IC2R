package me.halfcooler.ic2r.forge;

import io.netty.buffer.Unpooled;
import me.halfcooler.ic2r.core.IC2R;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Handles bidirectional {@link Ic2rRawPayload} on the play channel.
 */
public final class ForgeNetworkHandler {

    private ForgeNetworkHandler() {
    }

    public static void handle(Ic2rRawPayload payload, IPayloadContext context) {
        // C2S (serverbound) → simulating/server NetworkManager; S2C → client NetworkManager
        boolean simulating = context.flow().isServerbound();
        IC2R.network.get(simulating).onPacket(Unpooled.wrappedBuffer(payload.data()), context.player());
    }
}
