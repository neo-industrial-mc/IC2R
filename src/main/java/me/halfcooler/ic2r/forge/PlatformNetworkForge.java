package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.network.NetworkManager;
import me.halfcooler.ic2r.platform.services.PlatformNetwork;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

/**
 * Forge implementation of {@link PlatformNetwork} (G3.6).
 * <p>
 * Primary channel {@link NetworkManager#channelId} is registered in {@link FmlMod}
 * via {@code NetworkRegistry.newEventChannel(...).registerObject(ForgeNetworkHandler)}.
 * {@link #registerChannel} is therefore an <strong>idempotent no-op</strong> for the
 * runtime channel (and records other ids without re-wiring handlers).
 * <p>
 * Send paths mirror {@link NetworkManager} / {@code NetworkManagerClient}: vanilla
 * custom-payload packets on the given channel id (payload already framed by caller).
 */
public final class PlatformNetworkForge implements PlatformNetwork
{
	/** Channels known as registered (FmlMod pre-seeds the primary NetworkManager id). */
	private final Set<ResourceLocation> registered =
		Collections.newSetFromMap(new ConcurrentHashMap<>());

	public PlatformNetworkForge()
	{
		this.registered.add(NetworkManager.channelId);
	}

	@Override
	public void registerChannel(ResourceLocation channelId)
	{
		if (channelId == null)
		{
			return;
		}
		// Idempotent: FmlMod already registered NetworkManager.channelId + ForgeNetworkHandler.
		// Extra channel ids are recorded only — current IC2R uses a single custom channel.
		this.registered.add(channelId);
	}

	@Override
	public void sendToPlayer(ServerPlayer player, ResourceLocation channelId, FriendlyByteBuf payload)
	{
		if (player == null || channelId == null || payload == null)
		{
			return;
		}
		player.connection.send(new ClientboundCustomPayloadPacket(channelId, copyPayload(payload)));
	}

	@Override
	public void sendToServer(ResourceLocation channelId, FriendlyByteBuf payload)
	{
		if (channelId == null || payload == null || FMLEnvironment.dist != Dist.CLIENT)
		{
			// Dedicated server / wrong side: SPI allows no-op.
			return;
		}
		// Reflection avoids hard client type linkage on dedicated-server class load paths.
		try
		{
			Class<?> mcClass = Class.forName("net.minecraft.client.Minecraft");
			Object mc = mcClass.getMethod("getInstance").invoke(null);
			if (mc == null)
			{
				return;
			}
			Object connection = mcClass.getMethod("getConnection").invoke(mc);
			if (connection == null)
			{
				return;
			}
			Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket");
			Object packet = packetClass
				.getConstructor(ResourceLocation.class, FriendlyByteBuf.class)
				.newInstance(channelId, copyPayload(payload));
			connection.getClass().getMethod("send", Packet.class).invoke(connection, packet);
		}
		catch (ReflectiveOperationException ignored)
		{
			// Client connection unavailable.
		}
	}

	@Override
	public void sendToTracking(@Nullable Player around, ResourceLocation channelId, FriendlyByteBuf payload)
	{
		if (channelId == null || payload == null)
		{
			return;
		}
		if (!(around instanceof ServerPlayer serverPlayer))
		{
			// Draft SPI: dimension-wide fan-out when around is null is implementation-defined;
			// without a player anchor we no-op (avoids accidental global spam).
			return;
		}
		if (!(serverPlayer.level() instanceof ServerLevel level))
		{
			return;
		}
		// Fresh packet per recipient so buffer consumption cannot cross players.
		for (ServerPlayer tracking : level.getChunkSource().chunkMap.getPlayers(new ChunkPos(serverPlayer.blockPosition()), false))
		{
			tracking.connection.send(new ClientboundCustomPayloadPacket(channelId, copyPayload(payload)));
		}
	}

	private static FriendlyByteBuf copyPayload(FriendlyByteBuf payload)
	{
		int reader = payload.readerIndex();
		int readable = payload.readableBytes();
		FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.buffer(readable));
		if (readable > 0)
		{
			copy.writeBytes(payload, reader, readable);
		}
		return copy;
	}
}
