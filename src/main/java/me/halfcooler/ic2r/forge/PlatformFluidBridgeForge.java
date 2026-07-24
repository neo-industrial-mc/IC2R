package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.fluid.EnvFluidHandler;
import me.halfcooler.ic2r.core.proxy.EnvProxy;
import me.halfcooler.ic2r.platform.services.PlatformFluidBridge;

/**
 * Forge implementation of {@link PlatformFluidBridge}.
 */
public final class PlatformFluidBridgeForge implements PlatformFluidBridge
{
	private static EnvProxy proxy()
	{
		EnvProxy env = IC2R.envProxy;
		if (env == null)
		{
			throw new IllegalStateException("IC2R.envProxy not initialized; cannot use PlatformFluidBridgeForge");
		}
		return env;
	}

	@Override
	public EnvFluidHandler createHandler()
	{
		return proxy().createFluidStackHandler();
	}
}
