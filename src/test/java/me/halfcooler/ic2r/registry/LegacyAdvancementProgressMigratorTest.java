package me.halfcooler.ic2r.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.halfcooler.ic2r.forge.LegacyAdvancementProgressMigrator;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure JSON / id remapping tests for {@link LegacyAdvancementProgressMigrator}.
 */
class LegacyAdvancementProgressMigratorTest
{
	@Test
	void remapAdvancementId_treePath_rebrandNamespaceAndPrefix()
	{
		ResourceLocation old = ResourceLocation.fromNamespaceAndPath("ic2", "ic2/root");
		ResourceLocation expected = ResourceLocation.fromNamespaceAndPath("ic2r", "ic2r/root");
		assertEquals(expected, LegacyAdvancementProgressMigrator.remapAdvancementId(old));
	}

	@Test
	void remapAdvancementId_nestedTreePath()
	{
		ResourceLocation old = ResourceLocation.fromNamespaceAndPath(
			"ic2",
			"ic2/build_generator/build_compressor/root"
		);
		ResourceLocation expected = ResourceLocation.fromNamespaceAndPath(
			"ic2r",
			"ic2r/build_generator/build_compressor/root"
		);
		assertEquals(expected, LegacyAdvancementProgressMigrator.remapAdvancementId(old));
	}

	@Test
	void remapAdvancementId_recipeUnlock_namespaceOnly()
	{
		ResourceLocation old = ResourceLocation.fromNamespaceAndPath("ic2", "recipes/shaped/wrench");
		ResourceLocation expected = ResourceLocation.fromNamespaceAndPath("ic2r", "recipes/shaped/wrench");
		assertEquals(expected, LegacyAdvancementProgressMigrator.remapAdvancementId(old));
	}

	@Test
	void remapAdvancementId_alreadyCurrent_unchanged()
	{
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath("ic2r", "ic2r/root");
		assertEquals(id, LegacyAdvancementProgressMigrator.remapAdvancementId(id));
	}

	@Test
	void remapAdvancementId_vanilla_unchanged()
	{
		ResourceLocation id = ResourceLocation.withDefaultNamespace("story/mine_stone");
		assertEquals(id, LegacyAdvancementProgressMigrator.remapAdvancementId(id));
	}

	@Test
	void remapCriterionKey_namespace()
	{
		assertEquals(
			"ic2r:root",
			LegacyAdvancementProgressMigrator.remapCriterionKey("ic2r/root", "ic2:root")
		);
		assertEquals(
			"ic2r:generator",
			LegacyAdvancementProgressMigrator.remapCriterionKey("ic2r/build_generator/root", "ic2r:generator")
		);
	}

	@Test
	void remapCriterionKey_industrialDiamond_toImpossible()
	{
		assertEquals(
			"impossible",
			LegacyAdvancementProgressMigrator.remapCriterionKey(
				LegacyAdvancementProgressMigrator.COAL_DIAMOND_PATH,
				"ic2r:industrial_diamond"
			)
		);
		assertEquals(
			"impossible",
			LegacyAdvancementProgressMigrator.remapCriterionKey(
				LegacyAdvancementProgressMigrator.COAL_DIAMOND_PATH,
				"ic2:industrial_diamond"
			)
		);
	}

	@Test
	void migrateProgressJson_rewritesLegacyTreeAndCriteria()
	{
		String raw = """
			{
			  "ic2:ic2/root": {
			    "criteria": {
			      "ic2:root": "2024-01-01 12:00:00 +0000"
			    },
			    "done": true
			  },
			  "minecraft:story/root": {
			    "criteria": {
			      "crafting_table": "2024-01-01 12:00:00 +0000"
			    },
			    "done": true
			  },
			  "DataVersion": 3465
			}
			""";
		JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
		Optional<JsonObject> migrated = LegacyAdvancementProgressMigrator.migrateProgressJson(root);
		assertTrue(migrated.isPresent());

		JsonObject out = migrated.get();
		assertTrue(out.has("ic2r:ic2r/root"));
		assertFalse(out.has("ic2:ic2/root"));
		assertTrue(out.has("minecraft:story/root"));
		assertEquals(3465, out.get("DataVersion").getAsInt());

		JsonObject criteria = out.getAsJsonObject("ic2r:ic2r/root").getAsJsonObject("criteria");
		assertTrue(criteria.has("ic2r:root"));
		assertFalse(criteria.has("ic2:root"));
	}

	@Test
	void migrateProgressJson_noopWhenAlreadyCurrent()
	{
		String raw = """
			{
			  "ic2r:ic2r/root": {
			    "criteria": {
			      "ic2r:root": "2024-01-01 12:00:00 +0000"
			    },
			    "done": true
			  }
			}
			""";
		JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
		assertTrue(LegacyAdvancementProgressMigrator.migrateProgressJson(root).isEmpty());
	}

	@Test
	void migrateProgressJson_mergesWhenBothLegacyAndCurrentPresent()
	{
		String raw = """
			{
			  "ic2:ic2/root": {
			    "criteria": {
			      "ic2:root": "2024-01-01 12:00:00 +0000"
			    },
			    "done": true
			  },
			  "ic2r:ic2r/root": {
			    "criteria": {
			      "ic2r:root": "2024-06-01 12:00:00 +0000"
			    },
			    "done": true
			  }
			}
			""";
		JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
		Optional<JsonObject> migrated = LegacyAdvancementProgressMigrator.migrateProgressJson(root);
		assertTrue(migrated.isPresent());
		JsonObject out = migrated.get();
		assertFalse(out.has("ic2:ic2/root"));
		assertTrue(out.has("ic2r:ic2r/root"));
		assertTrue(out.getAsJsonObject("ic2r:ic2r/root").getAsJsonObject("criteria").has("ic2r:root"));
	}

	@Test
	void migrateProgressJson_coalDiamondCriterion()
	{
		String raw = """
			{
			  "ic2r:ic2r/build_generator/build_compressor/build_coal_diamond": {
			    "criteria": {
			      "ic2r:industrial_diamond": "2024-01-01 12:00:00 +0000"
			    },
			    "done": true
			  }
			}
			""";
		JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
		Optional<JsonObject> migrated = LegacyAdvancementProgressMigrator.migrateProgressJson(root);
		assertTrue(migrated.isPresent());
		JsonObject criteria = migrated.get()
			.getAsJsonObject("ic2r:ic2r/build_generator/build_compressor/build_coal_diamond")
			.getAsJsonObject("criteria");
		assertTrue(criteria.has("impossible"));
		assertFalse(criteria.has("ic2r:industrial_diamond"));
	}

	@Test
	void migrateProgressJson_recipeUnlockFromLegacyNamespace()
	{
		String raw = """
			{
			  "ic2:recipes/shaped/wrench": {
			    "criteria": {
			      "has_ingots/bronze": "2024-01-01 12:00:00 +0000"
			    },
			    "done": true
			  }
			}
			""";
		JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
		Optional<JsonObject> migrated = LegacyAdvancementProgressMigrator.migrateProgressJson(root);
		assertTrue(migrated.isPresent());
		assertTrue(migrated.get().has("ic2r:recipes/shaped/wrench"));
		assertFalse(migrated.get().has("ic2:recipes/shaped/wrench"));
	}
}
