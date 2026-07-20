package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.util.LogCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

/**
 * World-save registry migration for NeoForge 1.21+.
 * <p>
 * Pre-20.1.40 worlds store {@code ic2:*} registry ids. Current registrations use {@code ic2r:*}
 * with the same path. Forge's {@code MissingMappingsEvent} performed this remap on 1.20.1; that
 * event no longer exists on NeoForge 1.21. Registry aliases are the supported replacement:
 * {@link Registry#get} / {@link Registry#getHolder} resolve through {@link Registry#resolve}, so
 * chunk palettes, item stacks, block entities, fluids, etc. keep working.
 * <p>
 * Aliases registered during {@code RegisterEvent} are captured into NeoForge's frozen registry
 * snapshot and restored for clients / local loads.
 * <p>
 * See {@code docs/patches/remove-registry-compat-ic2-namespace-and-empty-cell.patch}.
 */
public final class LegacyRegistryRemap
{
	/** Pre-20.1.40 registry namespace. */
	public static final String LEGACY_NAMESPACE = "ic2";

	/** Current mod registry namespace. */
	public static final String CURRENT_NAMESPACE = "ic2r";

	/** Earlier internal item rename (still under current namespace in some saves). */
	public static final String LEGACY_EMPTY_CELL_PATH = "empty_cell";

	public static final String FACADE_CELL_PATH = "facade_cell";

	private LegacyRegistryRemap()
	{
	}

	/**
	 * Plan path-preserving namespace aliases: {@code ic2:path} → {@code ic2r:path}
	 * for every registered id already under {@link #CURRENT_NAMESPACE}.
	 * <p>
	 * Pure planning helper for tests; does not touch live registries.
	 */
	public static List<Alias> planNamespaceAliases(Collection<ResourceLocation> registeredIds)
	{
		Objects.requireNonNull(registeredIds, "registeredIds");
		List<Alias> out = new ArrayList<>();
		for (ResourceLocation id : registeredIds)
		{
			if (!CURRENT_NAMESPACE.equals(id.getNamespace()))
			{
				continue;
			}
			ResourceLocation from = ResourceLocation.fromNamespaceAndPath(LEGACY_NAMESPACE, id.getPath());
			out.add(new Alias(from, id));
		}
		return out;
	}

	/**
	 * Plan item-only path renames that are not covered by namespace preservation.
	 */
	public static List<Alias> planItemPathRenames(Collection<ResourceLocation> registeredItemIds)
	{
		Objects.requireNonNull(registeredItemIds, "registeredItemIds");
		boolean hasFacade = false;
		boolean hasEmpty = false;
		for (ResourceLocation id : registeredItemIds)
		{
			if (!CURRENT_NAMESPACE.equals(id.getNamespace()))
			{
				continue;
			}
			if (FACADE_CELL_PATH.equals(id.getPath()))
			{
				hasFacade = true;
			}
			if (LEGACY_EMPTY_CELL_PATH.equals(id.getPath()))
			{
				hasEmpty = true;
			}
		}
		if (!hasFacade || hasEmpty)
		{
			return List.of();
		}

		ResourceLocation facade = ResourceLocation.fromNamespaceAndPath(CURRENT_NAMESPACE, FACADE_CELL_PATH);
		return List.of(
			new Alias(ResourceLocation.fromNamespaceAndPath(CURRENT_NAMESPACE, LEGACY_EMPTY_CELL_PATH), facade),
			new Alias(ResourceLocation.fromNamespaceAndPath(LEGACY_NAMESPACE, LEGACY_EMPTY_CELL_PATH), facade)
		);
	}

	/**
	 * Apply all legacy aliases to a live registry after its entries are registered.
	 * Safe to call for every registry: only {@code ic2r:*} entries produce aliases.
	 */
	public static void apply(Registry<?> registry)
	{
		Objects.requireNonNull(registry, "registry");

		int applied = 0;
		for (Alias alias : planNamespaceAliases(registry.keySet()))
		{
			if (tryAddAlias(registry, alias))
			{
				applied++;
			}
		}

		if (registry.key() == Registries.ITEM)
		{
			for (Alias alias : planItemPathRenames(registry.keySet()))
			{
				if (tryAddAlias(registry, alias))
				{
					applied++;
				}
			}
		}

		if (applied > 0)
		{
			IC2R.log.debug(
				LogCategory.Resource,
				"Legacy registry aliases: applied %d for %s (ic2 → ic2r)",
				applied,
				registry.key().location()
			);
		}
	}

	private static boolean tryAddAlias(Registry<?> registry, Alias alias)
	{
		// containsKey does not follow aliases; only skip if the legacy id is a real entry.
		if (registry.containsKey(alias.from()))
		{
			return false;
		}
		// Target must exist (or resolve through an existing alias chain).
		if (registry.get(alias.to()) == null && !registry.containsKey(alias.to()))
		{
			return false;
		}
		try
		{
			registry.addAlias(alias.from(), alias.to());
			return true;
		}
		catch (IllegalStateException ex)
		{
			// Duplicate / loop: another mod or prior call already mapped this key.
			IC2R.log.debug(
				LogCategory.Resource,
				"Legacy registry alias skipped %s → %s: %s",
				alias.from(),
				alias.to(),
				ex.getMessage()
			);
			return false;
		}
	}

	/**
	 * One registry alias mapping.
	 *
	 * @param from missing / legacy id
	 * @param to   currently registered id
	 */
	public record Alias(ResourceLocation from, ResourceLocation to)
	{
		public Alias
		{
			Objects.requireNonNull(from, "from");
			Objects.requireNonNull(to, "to");
		}
	}
}
