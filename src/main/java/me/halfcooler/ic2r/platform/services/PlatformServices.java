package me.halfcooler.ic2r.platform.services;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * Access point for platform SPI implementations.
 * Prefer {@link #install} from the loader entry; {@link ServiceLoader} is a fallback.
 */
public final class PlatformServices
{
	private static volatile PlatformEnergyBridge energy;
	private static volatile PlatformFluidBridge fluid;
	private static volatile PlatformItemTransfer itemTransfer;
	private static volatile PlatformPlayerUi playerUi;
	private static volatile PlatformConfig config;
	private static volatile PlatformLifecycle lifecycle;

	private PlatformServices()
	{
	}

	/**
	 * Explicit install (preferred). All arguments required.
	 */
	public static void install(
		PlatformEnergyBridge energy,
		PlatformFluidBridge fluid,
		PlatformItemTransfer itemTransfer,
		PlatformPlayerUi playerUi,
		PlatformConfig config,
		PlatformLifecycle lifecycle
	)
	{
		PlatformServices.energy = Objects.requireNonNull(energy, "energy");
		PlatformServices.fluid = Objects.requireNonNull(fluid, "fluid");
		PlatformServices.itemTransfer = Objects.requireNonNull(itemTransfer, "itemTransfer");
		PlatformServices.playerUi = Objects.requireNonNull(playerUi, "playerUi");
		PlatformServices.config = Objects.requireNonNull(config, "config");
		PlatformServices.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
	}

	public static PlatformEnergyBridge energy()
	{
		return require(energy, PlatformEnergyBridge.class, () -> PlatformServices.energy = load(PlatformEnergyBridge.class));
	}

	public static PlatformFluidBridge fluid()
	{
		return require(fluid, PlatformFluidBridge.class, () -> PlatformServices.fluid = load(PlatformFluidBridge.class));
	}

	public static PlatformItemTransfer itemTransfer()
	{
		return require(itemTransfer, PlatformItemTransfer.class, () -> PlatformServices.itemTransfer = load(PlatformItemTransfer.class));
	}

	public static PlatformPlayerUi playerUi()
	{
		return require(playerUi, PlatformPlayerUi.class, () -> PlatformServices.playerUi = load(PlatformPlayerUi.class));
	}

	public static PlatformConfig config()
	{
		return require(config, PlatformConfig.class, () -> PlatformServices.config = load(PlatformConfig.class));
	}

	public static PlatformLifecycle lifecycle()
	{
		return require(lifecycle, PlatformLifecycle.class, () -> PlatformServices.lifecycle = load(PlatformLifecycle.class));
	}

	/**
	 * True if {@link #install} has been called.
	 */
	public static boolean isInstalled()
	{
		return energy != null
			&& fluid != null
			&& itemTransfer != null
			&& playerUi != null
			&& config != null
			&& lifecycle != null;
	}

	/**
	 * Discover a single SPI implementation via {@link ServiceLoader}.
	 * Used as fallback when {@link #install} was not called.
	 */
	public static <T> T load(Class<T> type)
	{
		return ServiceLoader.load(type, PlatformServices.class.getClassLoader())
			.findFirst()
			.orElseThrow(() -> new IllegalStateException(
				"No platform SPI implementation for " + type.getName()
					+ ". Call PlatformServices.install(...) from the loader entry or register META-INF/services."
			));
	}

	private static <T> T require(T current, Class<T> type, Supplier<T> loader)
	{
		if (current != null)
		{
			return current;
		}
		return loader.get();
	}
}
