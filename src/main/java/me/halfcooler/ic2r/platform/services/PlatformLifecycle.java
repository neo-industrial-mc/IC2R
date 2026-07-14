package me.halfcooler.ic2r.platform.services;

import java.util.function.Consumer;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Process / side lifecycle and environment detection.
 * <p>
 * Draft SPI. Consolidates environment bits of {@code EnvProxy}
 * ({@code isClientEnv}, {@code isForgeEnv}, {@code getServer}) and side lifecycle from
 * {@code SideProxy} ({@code preInit}, {@code onPostInit}, {@code onServerAvailable},
 * {@code requestTick}, {@code isSimulating}/{@code isRendering}).
 */
public interface PlatformLifecycle
{
	enum LoaderKind
	{
		FORGE,
		NEOFORGE,
		FABRIC,
		UNKNOWN
	}

	boolean isClient();

	/** True when logical server / simulation side is active for this call context. */
	boolean isSimulating();

	/** True when client rendering context is active. */
	boolean isRendering();

	LoaderKind getLoaderKind();

	@Nullable
	MinecraftServer getServer();

	/** Schedule work on the appropriate tick loop. */
	void requestTick(boolean simulating, Runnable task);

	/** Invoked when a server instance becomes available (integrated or dedicated). */
	void onServerAvailable(MinecraftServer server);

	/**
	 * Register a one-shot or multi-fire callback for server-ready.
	 * Draft: implementations may only support registration before first server start.
	 */
	void whenServerAvailable(Consumer<MinecraftServer> callback);

	/** Early common setup hook (maps loosely to SideProxy#preInit). */
	void onBootstrap();

	/** Late common setup hook (maps loosely to SideProxy#onPostInit). */
	void onPostBootstrap();
}
