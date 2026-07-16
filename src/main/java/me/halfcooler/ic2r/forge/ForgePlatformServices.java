package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.item.Ic2rBucketItem;
import me.halfcooler.ic2r.forge.fluid.Ic2rFluidTankHandler;
import me.halfcooler.ic2r.forge.item.tool.ShearableAccessForge;
import me.halfcooler.ic2r.forge.util.PumpUtilForge;
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
	/** True while constructing adapters so nested {@link #install} from class-init is a no-op. */
	private static volatile boolean installing;

	private ForgePlatformServices()
	{
	}

	/** Idempotent (and re-entrant-safe) install from the Forge mod entry / {@code IC2R} static. */
	public static void install()
	{
		if (PlatformServices.isInstalled() || installing)
		{
			return;
		}

		installing = true;
		try
		{
			Ic2rFluidTank.setHandlerFactory(Ic2rFluidTankHandler::new);
			InvSlot.setHandlerFactory(me.halfcooler.ic2r.forge.block.invslot.InvSlotItemHandler::new);
			// EventHooks.onBucketUse was removed in NeoForge 1.21; no global bucket-use cancel hook.
			Ic2rBucketItem.setBucketUseHook((player, level, stack, hit) -> null);
			PumpUtilForge.install();
			ShearableAccessForge.install();
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
		finally
		{
			installing = false;
		}
	}
}
