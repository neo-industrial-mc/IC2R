package me.halfcooler.ic2r.platform.services;

import me.halfcooler.ic2r.core.fluid.EnvFluidHandler;

/**
 * Factory for the domain fluid environment handler (loader capability bridge).
 */
public interface PlatformFluidBridge
{
	EnvFluidHandler createHandler();
}
