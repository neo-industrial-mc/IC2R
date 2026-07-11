package ic2.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Raw IC2 network payload. The mod serialises its own {@link GrowingBuffer} content into a byte
 * array and lets the vanilla payload framing deliver it.
 */
public record Ic2Payload(byte[] data) implements CustomPacketPayload {
  public static final Type<Ic2Payload> TYPE = new Type<>(NetworkManager.channelId);

  public static final StreamCodec<FriendlyByteBuf, Ic2Payload> CODEC =
      StreamCodec.of(
          (buf, payload) -> buf.writeBytes(payload.data),
          buf -> {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return new Ic2Payload(data);
          });

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
