package me.halfcooler.ic2r.core.ref;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.platform.services.PlatformLifecycle;
import me.halfcooler.ic2r.platform.services.PlatformServices;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class Ic2rFluidTags
{
	public static final TagKey<Fluid> STEAM = create();

	public static void init()
	{
	}

	private static TagKey<Fluid> create()
	{
		// G3.5: force IC2R <clinit> (installs SPI) then pick loader-specific tag id
		if (IC2R.envProxy == null)
		{
			throw new IllegalStateException("IC2R.envProxy not initialized");
		}
		boolean fabric = PlatformServices.lifecycle().getLoaderKind() == PlatformLifecycle.LoaderKind.FABRIC;
		ResourceLocation id = ResourceLocation.parse(fabric ? "c:steam" : "forge:steam");
		return TagKey.create(Registries.FLUID, id);
	}
}
