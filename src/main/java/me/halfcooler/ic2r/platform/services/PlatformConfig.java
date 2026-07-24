package me.halfcooler.ic2r.platform.services;

import java.nio.file.Path;

/**
 * Config path resolution for common code (no loader config types on this surface).
 */
public interface PlatformConfig
{
	/** Game / mod config directory provided by the loader. */
	Path getConfigDirectory();
}
