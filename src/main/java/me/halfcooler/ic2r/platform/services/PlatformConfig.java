package me.halfcooler.ic2r.platform.services;

import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;

/**
 * Config file registration and path resolution.
 * <p>
 * Draft SPI. Today Forge uses {@code ModConfig} + {@code ForgeConfigSpec} in {@code FmlMod}
 * and {@code IC2RConfig}. Common code must not depend on Forge config types; this interface
 * is intentionally thin until a first config read path is migrated (W3.2+).
 */
public interface PlatformConfig
{
	/** Game / mod config directory (loader-provided). */
	Path getConfigDirectory();

	/**
	 * Register a common (synced or server) config file for this mod.
	 * {@code relativeFileName} may be null for the default common config name.
	 * <p>
	 * Spec object type is left as {@link Object} in this draft to avoid baking
	 * {@code ForgeConfigSpec} into the SPI; implementations cast/validate.
	 */
	void registerCommonConfig(Object spec, @Nullable String relativeFileName);

	/** Register a client-only config. No-op on dedicated server. */
	void registerClientConfig(Object spec, @Nullable String relativeFileName);

	/** Whether common config values are available for reading. */
	boolean isCommonConfigLoaded();
}
