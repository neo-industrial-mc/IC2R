package me.halfcooler.ic2r.registry;

import me.halfcooler.ic2r.forge.LegacyRegistryRemap;
import me.halfcooler.ic2r.forge.LegacyRegistryRemap.Alias;

import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure planning tests for {@link LegacyRegistryRemap} (no live registry mutation).
 */
class LegacyRegistryRemapTest
{
	private static final Alias INDUSTRIAL_DIAMOND_IC2R = new Alias(
		ResourceLocation.fromNamespaceAndPath("ic2r", "industrial_diamond"),
		LegacyRegistryRemap.VANILLA_DIAMOND
	);
	private static final Alias INDUSTRIAL_DIAMOND_IC2 = new Alias(
		ResourceLocation.fromNamespaceAndPath("ic2", "industrial_diamond"),
		LegacyRegistryRemap.VANILLA_DIAMOND
	);

	@Test
	void namespaceAliases_mapIc2PathToIc2rSamePath()
	{
		ResourceLocation macerator = ResourceLocation.fromNamespaceAndPath("ic2r", "macerator");
		ResourceLocation cable = ResourceLocation.fromNamespaceAndPath("ic2r", "copper_cable");
		ResourceLocation vanilla = ResourceLocation.fromNamespaceAndPath("minecraft", "dirt");

		List<Alias> aliases = LegacyRegistryRemap.planNamespaceAliases(List.of(macerator, cable, vanilla));

		assertEquals(2, aliases.size());
		assertTrue(aliases.contains(new Alias(
			ResourceLocation.fromNamespaceAndPath("ic2", "macerator"),
			macerator
		)));
		assertTrue(aliases.contains(new Alias(
			ResourceLocation.fromNamespaceAndPath("ic2", "copper_cable"),
			cable
		)));
	}

	@Test
	void namespaceAliases_ignoreNonIc2rEntries()
	{
		List<Alias> aliases = LegacyRegistryRemap.planNamespaceAliases(List.of(
			ResourceLocation.fromNamespaceAndPath("minecraft", "stone"),
			ResourceLocation.fromNamespaceAndPath("ic2", "legacy_only")
		));
		assertTrue(aliases.isEmpty());
	}

	@Test
	void itemPathRenames_emptyCellToFacadeCell_whenFacadePresent()
	{
		ResourceLocation facade = ResourceLocation.fromNamespaceAndPath("ic2r", "facade_cell");
		List<Alias> aliases = LegacyRegistryRemap.planItemPathRenames(Set.of(facade));

		assertTrue(aliases.contains(new Alias(
			ResourceLocation.fromNamespaceAndPath("ic2r", "empty_cell"),
			facade
		)));
		assertTrue(aliases.contains(new Alias(
			ResourceLocation.fromNamespaceAndPath("ic2", "empty_cell"),
			facade
		)));
		assertTrue(aliases.contains(INDUSTRIAL_DIAMOND_IC2R));
		assertTrue(aliases.contains(INDUSTRIAL_DIAMOND_IC2));
		assertEquals(4, aliases.size());
	}

	@Test
	void itemPathRenames_skippedWhenEmptyCellStillRegistered()
	{
		List<Alias> aliases = LegacyRegistryRemap.planItemPathRenames(Set.of(
			ResourceLocation.fromNamespaceAndPath("ic2r", "facade_cell"),
			ResourceLocation.fromNamespaceAndPath("ic2r", "empty_cell")
		));
		assertFalse(aliases.stream().anyMatch(a -> a.from().getPath().equals("empty_cell")));
		assertTrue(aliases.contains(INDUSTRIAL_DIAMOND_IC2R));
		assertTrue(aliases.contains(INDUSTRIAL_DIAMOND_IC2));
	}

	@Test
	void itemPathRenames_industrialDiamondWhenNotRegistered()
	{
		List<Alias> aliases = LegacyRegistryRemap.planItemPathRenames(Set.of(
			ResourceLocation.fromNamespaceAndPath("ic2r", "iron_ingot")
		));
		assertEquals(2, aliases.size());
		assertTrue(aliases.contains(INDUSTRIAL_DIAMOND_IC2R));
		assertTrue(aliases.contains(INDUSTRIAL_DIAMOND_IC2));
	}

	@Test
	void itemPathRenames_skippedWhenIndustrialDiamondStillRegistered()
	{
		List<Alias> aliases = LegacyRegistryRemap.planItemPathRenames(Set.of(
			ResourceLocation.fromNamespaceAndPath("ic2r", "industrial_diamond")
		));
		assertFalse(aliases.stream().anyMatch(a -> a.from().getPath().equals("industrial_diamond")));
	}
}
