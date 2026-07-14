package me.halfcooler.ic2r.core.util;

import me.halfcooler.ic2r.core.network.NetworkManager;
import me.halfcooler.ic2r.platform.services.PlatformServices;

public final class SideGateway
{
	private final NetworkManager clientInstance;
	private final NetworkManager serverInstance;

	public SideGateway()
	{
		try
		{
			if (PlatformServices.lifecycle().isClient())
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
