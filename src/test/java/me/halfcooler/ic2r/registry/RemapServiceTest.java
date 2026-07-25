package me.halfcooler.ic2r.registry;

import me.halfcooler.ic2r.core.RemapService;
import me.halfcooler.ic2r.core.RemapService.Alias;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure planning tests for {@link RemapService} (no live registry mutation).
 */
class RemapServiceTest
{
	private static final Alias INDUSTRIAL_DIAMOND_IC2R = new Alias(
		ResourceLocation.fromNamespaceAndPath("ic2r", "industrial_diamond"),
		RemapService.VANILLA_DIAMOND
	);
	private static final Alias INDUSTRIAL_DIAMOND_IC2 = new Alias(
		ResourceLocation.fromNamespaceAndPath("ic2", "industrial_diamond"),
		RemapService.VANILLA_DIAMOND
	);

	@Test
	void namespaceAliases_mapIc2PathToIc2rSamePath()
	{
		ResourceLocation macerator = ResourceLocation.fromNamespaceAndPath("ic2r", "macerator");
		ResourceLocation cable = ResourceLocation.fromNamespaceAndPath("ic2r", "copper_cable");
		ResourceLocation vanilla = ResourceLocation.fromNamespaceAndPath("minecraft", "dirt");

		List<Alias> aliases = RemapService.planNamespaceAliases(List.of(macerator, cable, vanilla));

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
		List<Alias> aliases = RemapService.planNamespaceAliases(List.of(
			ResourceLocation.fromNamespaceAndPath("minecraft", "stone"),
			ResourceLocation.fromNamespaceAndPath("ic2", "legacy_only")
		));
		assertTrue(aliases.isEmpty());
	}

	@Test
	void itemPathRenames_emptyCellToFacadeCell_whenFacadePresent()
	{
		ResourceLocation facade = ResourceLocation.fromNamespaceAndPath("ic2r", "facade_cell");
		List<Alias> aliases = RemapService.planItemPathRenames(Set.of(facade));

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
		List<Alias> aliases = RemapService.planItemPathRenames(Set.of(
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
		List<Alias> aliases = RemapService.planItemPathRenames(Set.of(
			ResourceLocation.fromNamespaceAndPath("ic2r", "iron_ingot")
		));
		assertEquals(2, aliases.size());
		assertTrue(aliases.contains(INDUSTRIAL_DIAMOND_IC2R));
		assertTrue(aliases.contains(INDUSTRIAL_DIAMOND_IC2));
	}

	@Test
	void itemPathRenames_skippedWhenIndustrialDiamondStillRegistered()
	{
		List<Alias> aliases = RemapService.planItemPathRenames(Set.of(
			ResourceLocation.fromNamespaceAndPath("ic2r", "industrial_diamond")
		));
		assertFalse(aliases.stream().anyMatch(a -> a.from().getPath().equals("industrial_diamond")));
	}

	@Test
	void itemPathRenames_miningFilterCardToUpgrade_whenUpgradePresent()
	{
		ResourceLocation upgrade = ResourceLocation.fromNamespaceAndPath("ic2r", "mining_filter_upgrade");
		List<Alias> aliases = RemapService.planItemPathRenames(Set.of(upgrade));

		assertTrue(aliases.contains(new Alias(
			ResourceLocation.fromNamespaceAndPath("ic2r", "mining_filter_card"),
			upgrade
		)));
		assertTrue(aliases.contains(new Alias(
			ResourceLocation.fromNamespaceAndPath("ic2", "mining_filter_card"),
			upgrade
		)));
		assertTrue(aliases.contains(INDUSTRIAL_DIAMOND_IC2R));
		assertTrue(aliases.contains(INDUSTRIAL_DIAMOND_IC2));
		assertEquals(4, aliases.size());
	}

	@Test
	void itemPathRenames_miningFilterCard_skippedWhenStillRegistered()
	{
		List<Alias> aliases = RemapService.planItemPathRenames(Set.of(
			ResourceLocation.fromNamespaceAndPath("ic2r", "mining_filter_upgrade"),
			ResourceLocation.fromNamespaceAndPath("ic2r", "mining_filter_card")
		));
		assertFalse(aliases.stream().anyMatch(a -> a.from().getPath().equals("mining_filter_card")));
	}
}
