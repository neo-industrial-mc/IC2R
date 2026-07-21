package me.halfcooler.ic2r.core;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * IC2R custom damage types (datapack-registered under {@code data/ic2r/damage_type/})
 * and helpers to create {@link DamageSource} instances from a live {@link Level}.
 * <p>
 * Prefer the Level-based factories ({@link #radiation(Level)}, etc.) over the cached
 * static fields — static sources can be {@code null} before first init and can go stale
 * across world reloads.
 */
public final class Ic2rDamageSource
{
	public static final ResourceKey<DamageType> ELECTRICITY = key("electricity");
	public static final ResourceKey<DamageType> NUKE = key("nuke");
	public static final ResourceKey<DamageType> RADIATION = key("radiation");
	public static final ResourceKey<DamageType> REACTOR_EXPLOSION = key("reactor_explosion");
	public static final ResourceKey<DamageType> CROP_EATING = key("crop_eating");

	/**
	 * Cached sources for API consumers that cannot pass a {@link Level}.
	 * Always prefer {@link #radiation(Level)} / {@link #electricity(Level)} when possible.
	 * Populated by {@link #init(RegistryAccess)}; may be {@code null} until then.
	 */
	public static DamageSource electricity;
	public static DamageSource nuke;
	public static DamageSource radiation;
	public static DamageSource reactorExplosion;

	private Ic2rDamageSource()
	{
	}

	private static ResourceKey<DamageType> key(String path)
	{
		return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ic2r", path));
	}

	/**
	 * Creates a damage source from the level's current damage-type registry.
	 * This is the correct 1.21 way and always uses the active datapack holders.
	 */
	public static DamageSource of(Level level, ResourceKey<DamageType> type)
	{
		return level.damageSources().source(type);
	}

	public static DamageSource electricity(Level level)
	{
		return of(level, ELECTRICITY);
	}

	public static DamageSource radiation(Level level)
	{
		return of(level, RADIATION);
	}

	public static DamageSource nuke(Level level)
	{
		return of(level, NUKE);
	}

	public static DamageSource reactorExplosion(Level level)
	{
		return of(level, REACTOR_EXPLOSION);
	}

	public static DamageSource cropEating(Level level)
	{
		return of(level, CROP_EATING);
	}

	/**
	 * Populates the static cached sources from the given registry access.
	 * Safe to call repeatedly (e.g. on each world load).
	 */
	public static void init(RegistryAccess registryAccess)
	{
		Registry<DamageType> registry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
		electricity = new DamageSource(registry.getHolderOrThrow(ELECTRICITY));
		nuke = new DamageSource(registry.getHolderOrThrow(NUKE));
		radiation = new DamageSource(registry.getHolderOrThrow(RADIATION));
		reactorExplosion = new DamageSource(registry.getHolderOrThrow(REACTOR_EXPLOSION));
	}

	/**
	 * Ensures static sources are initialized. Call when only a level is available
	 * and code still needs the cached fields (e.g. public API).
	 */
	public static void ensureInit(Level level)
	{
		if (radiation == null || electricity == null || nuke == null || reactorExplosion == null)
		{
			init(level.registryAccess());
		}
	}

	public static DamageSource getNukeSource(LivingEntity igniter, Level level)
	{
		if (igniter != null)
		{
			return level.damageSources().source(NUKE, igniter);
		}
		return nuke(level);
	}

	/**
	 * @deprecated Prefer {@link #of(Level, ResourceKey)} or the named factories.
	 *             Kept for CropEating and similar dynamic lookups.
	 */
	@Deprecated
	public static DamageSource create(Level level, String name)
	{
		return of(level, key(name));
	}
}
