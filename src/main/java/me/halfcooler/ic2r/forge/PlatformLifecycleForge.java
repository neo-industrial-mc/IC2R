package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.platform.services.PlatformLifecycle;

import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Forge implementation of {@link PlatformLifecycle}.
 */
public final class PlatformLifecycleForge implements PlatformLifecycle
{
	private final boolean client = FMLEnvironment.dist.isClient();

	@Override
	public boolean isClient()
	{
		return this.client;
	}

	@Override
	@Nullable
	public MinecraftServer getServer()
	{
		return ServerLifecycleHooks.getCurrentServer();
	}
}
