package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.network.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
public record Ic2rRawPayload(byte[] data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<Ic2rRawPayload> TYPE =
        new CustomPacketPayload.Type<>(NetworkManager.channelId);

    public static final StreamCodec<FriendlyByteBuf, Ic2rRawPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeVarInt(payload.data.length);
            buf.writeBytes(payload.data);
        },
        buf -> {
            int len = buf.readVarInt();
            byte[] data = new byte[len];
            buf.readBytes(data);
            return new Ic2rRawPayload(data);
        });

    public static Ic2rRawPayload fromByteBuf(io.netty.buffer.ByteBuf data) {
        byte[] bytes = new byte[data.readableBytes()];
        data.getBytes(data.readerIndex(), bytes);
        return new Ic2rRawPayload(bytes);
    }

    public static Ic2rRawPayload fromFriendly(FriendlyByteBuf payload) {
        int reader = payload.readerIndex();
        int readable = payload.readableBytes();
        byte[] bytes = new byte[readable];
        if (readable > 0) {
            payload.getBytes(reader, bytes);
        }
        return new Ic2rRawPayload(bytes);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public ResourceLocation id() {
        return TYPE.id();
    }
}
