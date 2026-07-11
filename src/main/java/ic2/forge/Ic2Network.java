package ic2.forge;

import ic2.core.IC2;
import ic2.core.network.Ic2Payload;
import io.netty.buffer.Unpooled;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class Ic2Network {
  private Ic2Network() {}

  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .optional()
        .playBidirectional(Ic2Payload.TYPE, Ic2Payload.CODEC, Ic2Network::handle);
  }

  private static void handle(Ic2Payload payload, IPayloadContext context) {
    if (context.flow().isClientbound()) {
      IC2.network
          .get(false)
          .onPacket(Unpooled.wrappedBuffer(payload.data()), IC2.sideProxy.getPlayerInstance());
    } else {
      IC2.network.get(true).onPacket(Unpooled.wrappedBuffer(payload.data()), context.player());
    }
  }
}
