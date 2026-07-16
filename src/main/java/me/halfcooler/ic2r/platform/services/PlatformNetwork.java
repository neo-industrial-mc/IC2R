package me.halfcooler.ic2r.platform.services;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

/**
 * Custom payload channel registration and packet dispatch.
 * <p>
 * Draft SPI. Today Forge registers an event channel for {@code NetworkManager.channelId}
 * in {@code FmlMod} / {@code ForgeNetworkHandler}. Common code already mostly uses vanilla
 * packet types; this SPI captures the remaining loader-specific channel glue.
 */
public interface PlatformNetwork
{
	/**
	 * Register (or ensure registration of) a custom payload channel.
	 * Idempotent preferred.
	 */
	void registerChannel(ResourceLocation channelId);

	/** Server → one client. */
	void sendToPlayer(ServerPlayer player, ResourceLocation channelId, FriendlyByteBuf payload);

	/** Client → server. No-op or throw on dedicated server if called incorrectly. */
	void sendToServer(ResourceLocation channelId, FriendlyByteBuf payload);

	/**
	 * Server → tracking clients around a player (or dimension broadcast if {@code around} is null).
	 * Exact fan-out is implementation-defined; draft only.
	 */
	void sendToTracking(@Nullable Player around, ResourceLocation channelId, FriendlyByteBuf payload);
}
