package me.halfcooler.ic2r.core.ref;

import me.halfcooler.ic2r.core.IC2R;
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
		// Common steam fluid tag (c:steam) shared by NeoForge / Fabric convention tags.
		if (IC2R.envProxy == null)
		{
			throw new IllegalStateException("IC2R.envProxy not initialized");
		}
		return TagKey.create(Registries.FLUID, ResourceLocation.parse("c:steam"));
	}
}
