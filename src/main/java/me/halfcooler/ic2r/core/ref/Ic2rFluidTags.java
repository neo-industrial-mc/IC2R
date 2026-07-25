package me.halfcooler.ic2r.core.ref;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/**
 * Fluid tags used by IC2R. Common tags use the shared {@code c:} namespace.
 */
public final class Ic2rFluidTags
{
	/**
	 * Common steam fluid tag shared by NeoForge / Fabric convention tags.
	 */
	public static final TagKey<Fluid> STEAM = common("steam");

	public static void init()
	{
	}

	private static TagKey<Fluid> common(String path)
	{
		return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", path));
	}
}
