package me.halfcooler.ic2r.core.util;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.network.NetworkManager;

public final class SideGateway
{
	private final NetworkManager clientInstance;
	private final NetworkManager serverInstance;

	public SideGateway()
	{
		try
		{
			if (IC2R.envProxy.isClientEnv())
			{
				this.clientInstance = (NetworkManager) Class.forName("me.halfcooler.ic2r.core.network.NetworkManagerClient").getConstructor().newInstance();
			} else
			{
				this.clientInstance = null;
			}

			this.serverInstance = new NetworkManager();
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public NetworkManager get(boolean simulating)
	{
		return simulating ? this.serverInstance : this.clientInstance;
	}
}
