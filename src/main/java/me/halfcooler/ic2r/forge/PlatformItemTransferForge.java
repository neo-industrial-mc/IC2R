package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.item.EnvItemHandler;
import me.halfcooler.ic2r.core.proxy.EnvProxy;
import me.halfcooler.ic2r.platform.services.PlatformItemTransfer;

/**
 * Forge implementation of {@link PlatformItemTransfer}.
 */
public final class PlatformItemTransferForge implements PlatformItemTransfer
{
	private static EnvProxy proxy()
	{
		EnvProxy env = IC2R.envProxy;
		if (env == null)
		{
			throw new IllegalStateException("IC2R.envProxy not initialized; cannot use PlatformItemTransferForge");
		}
		return env;
	}

	@Override
	public EnvItemHandler createHandler()
	{
		return proxy().createItemHandler();
	}
}
