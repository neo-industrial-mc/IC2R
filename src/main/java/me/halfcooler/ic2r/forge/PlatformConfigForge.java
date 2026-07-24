package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.platform.services.PlatformConfig;

import java.nio.file.Path;

import net.neoforged.fml.loading.FMLPaths;

/**
 * Forge implementation of {@link PlatformConfig}.
 */
public final class PlatformConfigForge implements PlatformConfig
{
	@Override
	public Path getConfigDirectory()
	{
		return FMLPaths.CONFIGDIR.get();
	}
}
