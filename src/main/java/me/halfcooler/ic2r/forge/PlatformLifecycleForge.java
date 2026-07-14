package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.platform.services.PlatformLifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Forge implementation of {@link PlatformLifecycle}.
 * Thin adapter: environment bits from FML; side lifecycle delegates to {@code IC2R.sideProxy}.
 */
public final class PlatformLifecycleForge implements PlatformLifecycle
{
	private final boolean client = FMLEnvironment.dist.isClient();
	private final List<Consumer<MinecraftServer>> serverAvailableCallbacks = new ArrayList<>();

	@Override
	public boolean isClient()
	{
		return this.client;
	}

	@Override
	public boolean isSimulating()
	{
		return IC2R.sideProxy.isSimulating();
	}

	@Override
	public boolean isRendering()
	{
		return IC2R.sideProxy.isRendering();
	}

	@Override
	public LoaderKind getLoaderKind()
	{
		return LoaderKind.FORGE;
	}

	@Override
	@Nullable
	public MinecraftServer getServer()
	{
		return ServerLifecycleHooks.getCurrentServer();
	}

	@Override
	public void requestTick(boolean simulating, Runnable task)
	{
		IC2R.sideProxy.requestTick(simulating, task);
	}

	@Override
	public void onServerAvailable(MinecraftServer server)
	{
		IC2R.sideProxy.onServerAvailable(server);
		List<Consumer<MinecraftServer>> toRun;
		synchronized (this.serverAvailableCallbacks)
		{
			toRun = new ArrayList<>(this.serverAvailableCallbacks);
			this.serverAvailableCallbacks.clear();
		}
		for (Consumer<MinecraftServer> cb : toRun)
		{
			cb.accept(server);
		}
	}

	@Override
	public void whenServerAvailable(Consumer<MinecraftServer> callback)
	{
		MinecraftServer current = this.getServer();
		if (current != null)
		{
			callback.accept(current);
			return;
		}
		synchronized (this.serverAvailableCallbacks)
		{
			this.serverAvailableCallbacks.add(callback);
		}
	}

	@Override
	public void onBootstrap()
	{
		IC2R.sideProxy.preInit();
	}

	@Override
	public void onPostBootstrap()
	{
		IC2R.sideProxy.onPostInit();
	}
}
