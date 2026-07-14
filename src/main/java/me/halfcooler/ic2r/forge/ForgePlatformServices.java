package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.platform.services.PlatformServices;

/**
 * Wires Forge SPI adapters into {@link PlatformServices}.
 * <p>
 * G3.6: all eight facets are real thin Forge adapters (no stub classes):
 * {@link PlatformLifecycleForge}, {@link PlatformRegistryForge}, {@link PlatformEnergyBridgeForge},
 * {@link PlatformFluidBridgeForge}, {@link PlatformItemTransferForge}, {@link PlatformNetworkForge},
 * {@link PlatformPlayerUiForge}, {@link PlatformConfigForge}.
 * EnvProxy dual-track remains for unmigrated call sites (incremental E3+).
 */
public final class ForgePlatformServices
{
	private ForgePlatformServices()
	{
	}

	/** Idempotent install from the Forge mod entry / {@code IC2R} static. */
	public static void install()
	{
		if (PlatformServices.isInstalled())
		{
			return;
		}

		PlatformServices.install(
			new PlatformRegistryForge(),
			new PlatformEnergyBridgeForge(),
			new PlatformFluidBridgeForge(),
			new PlatformItemTransferForge(),
			new PlatformNetworkForge(),
			new PlatformPlayerUiForge(),
			new PlatformConfigForge(),
			new PlatformLifecycleForge()
		);
	}
}
