package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.network.NetworkManager;
import me.halfcooler.ic2r.platform.services.PlatformNetwork;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
public final class PlatformNetworkForge implements PlatformNetwork
{
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
		this.registered.add(channelId);
	}

	@Override
	public void sendToPlayer(ServerPlayer player, ResourceLocation channelId, FriendlyByteBuf payload)
	{
		if (player == null || channelId == null || payload == null)
		{
			return;
		}
		// Primary channel uses typed payload; other ids are currently unused at runtime.
		if (NetworkManager.channelId.equals(channelId))
		{
			PacketDistributor.sendToPlayer(player, Ic2rRawPayload.fromFriendly(payload));
		}
	}

	@Override
	public void sendToServer(ResourceLocation channelId, FriendlyByteBuf payload)
	{
		if (channelId == null || payload == null || FMLEnvironment.dist != Dist.CLIENT)
		{
			return;
		}
		if (NetworkManager.channelId.equals(channelId))
		{
			PacketDistributor.sendToServer(Ic2rRawPayload.fromFriendly(payload));
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
			return;
		}
		if (!(serverPlayer.level() instanceof ServerLevel level))
		{
			return;
		}
		if (!NetworkManager.channelId.equals(channelId))
		{
			return;
		}
		for (ServerPlayer tracking : level.getChunkSource().chunkMap.getPlayers(new ChunkPos(serverPlayer.blockPosition()), false))
		{
			PacketDistributor.sendToPlayer(tracking, Ic2rRawPayload.fromFriendly(payload));
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
