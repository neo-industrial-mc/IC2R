package me.halfcooler.ic2r.platform.services;

import me.halfcooler.ic2r.core.item.EnvItemHandler;

/**
 * Factory for the domain item-transfer environment handler (loader inventory capability bridge).
 */
public interface PlatformItemTransfer
{
	EnvItemHandler createHandler();
}
