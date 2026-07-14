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
		ResourceLocation id = ResourceLocation.parse(IC2R.envProxy.isFabricEnv() ? "c:steam" : "forge:steam");
		return TagKey.create(Registries.FLUID, id);
	}
}
