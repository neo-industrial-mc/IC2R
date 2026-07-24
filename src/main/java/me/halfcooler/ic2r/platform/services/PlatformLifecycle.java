package me.halfcooler.ic2r.platform.services;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Process / side lifecycle and environment detection used by common code.
 */
public interface PlatformLifecycle
{
	boolean isClient();

	@Nullable
	MinecraftServer getServer();
}
