package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.platform.services.PlatformConfig;

import java.nio.file.Path;

import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

/**
 * Forge implementation of {@link PlatformConfig} (G3.6).
 * <p>
 * Config directory from {@link FMLPaths#CONFIGDIR}. Common/client config registration is
 * already performed in {@link FmlMod} via {@code ModLoadingContext#registerConfig}; SPI
 * {@link #registerCommonConfig}/{@link #registerClientConfig} are therefore
 * <strong>idempotent no-ops</strong> (avoids double-registration of the same specs).
 * <p>
 * {@link #isCommonConfigLoaded()} reports {@link IC2RConfig#SPEC}{@code .isLoaded()}.
 */
public final class PlatformConfigForge implements PlatformConfig
{
	@Override
	public Path getConfigDirectory()
	{
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public void registerCommonConfig(Object spec, @Nullable String relativeFileName)
	{
		// FmlMod already registers IC2RConfig.SPEC / IC2RUuScanConfig.SPEC — no-op.
	}

	@Override
	public void registerClientConfig(Object spec, @Nullable String relativeFileName)
	{
		// FmlMod already registers IC2RClientConfig.SPEC on client dist — no-op.
	}

	@Override
	public boolean isCommonConfigLoaded()
	{
		return IC2RConfig.SPEC.isLoaded();
	}
}
