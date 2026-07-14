package me.halfcooler.ic2r.recipe;

import me.halfcooler.ic2r.core.recipe.MachineRecipeMatchMath;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-logic recipe match gates + macerator pilot datapack smoke (W2.3 / RC-*).
 * No Level / RecipeManager bootstrap.
 * <p>
 * Runtime bridge (documented on {@code RecipeManagerMachineBridge}):
 * JSON {@code ic2r:macerator} → Serializer → RecipeManager → loadBasic → Recipes.macerator.
 */
class MachineRecipeMatchMathTest
{
	// --- RC-001 spirit: amount / acceptance gates (item match itself needs registries) ---

	@Test
	void hasSufficientCount_requires_positive_and_at_least_recipe_amount()
	{
		assertTrue(MachineRecipeMatchMath.hasSufficientCount(1, 1));
		assertTrue(MachineRecipeMatchMath.hasSufficientCount(8, 2));
		assertFalse(MachineRecipeMatchMath.hasSufficientCount(0, 1));
		assertFalse(MachineRecipeMatchMath.hasSufficientCount(1, 0));
		assertFalse(MachineRecipeMatchMath.hasSufficientCount(1, 2));
		assertFalse(MachineRecipeMatchMath.hasSufficientCount(-1, 1));
	}

	/** Mirrors BasicMachineRecipeManager.getOutputFor amount + remainder gate. */
	@Test
	void canApplyInput_remainder_requires_exact_count()
	{
		assertTrue(MachineRecipeMatchMath.canApplyInput(4, 2, false));
		assertTrue(MachineRecipeMatchMath.canApplyInput(2, 2, false));
		assertFalse(MachineRecipeMatchMath.canApplyInput(1, 2, false));

		assertTrue(MachineRecipeMatchMath.canApplyInput(2, 2, true));
		assertFalse(MachineRecipeMatchMath.canApplyInput(3, 2, true));
		assertFalse(MachineRecipeMatchMath.canApplyInput(1, 2, true));
	}

	@Test
	void countAfterConsume_never_negative()
	{
		assertEquals(3, MachineRecipeMatchMath.countAfterConsume(5, 2));
		assertEquals(0, MachineRecipeMatchMath.countAfterConsume(2, 2));
		assertEquals(0, MachineRecipeMatchMath.countAfterConsume(1, 2));
		assertEquals(0, MachineRecipeMatchMath.countAfterConsume(0, 1));
		assertEquals(4, MachineRecipeMatchMath.countAfterConsume(4, 0));
	}

	// --- first-wins scan order (SM-010 / RC match order) ---

	@Test
	void firstMatchIndex_returns_earliest_true()
	{
		assertEquals(0, MachineRecipeMatchMath.firstMatchIndex(3, i -> i == 0));
		assertEquals(2, MachineRecipeMatchMath.firstMatchIndex(3, i -> i == 2));
		assertEquals(-1, MachineRecipeMatchMath.firstMatchIndex(3, i -> false));
		assertEquals(-1, MachineRecipeMatchMath.firstMatchIndex(0, i -> true));
		assertEquals(-1, MachineRecipeMatchMath.firstMatchIndex(2, null));
	}

	@Test
	void firstMatch_returns_first_predicate_hit()
	{
		List<String> recipes = List.of("a", "b", "c");
		assertEquals("b", MachineRecipeMatchMath.firstMatch(recipes, s -> s.equals("b")));
		assertEquals("a", MachineRecipeMatchMath.firstMatch(recipes, s -> true));
		assertNull(MachineRecipeMatchMath.firstMatch(recipes, s -> false));
		assertNull(MachineRecipeMatchMath.firstMatch(null, s -> true));
	}

	// --- RC-006 (partial): pilot JSON exists and declares type id ---

	@Test
	void macerator_datapack_json_declares_pilot_type() throws Exception
	{
		String resource = "data/ic2r/recipes/macerator/cobblestone_to_sand.json";
		try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource))
		{
			assertNotNull(in, "expected classpath resource " + resource);
			String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			assertTrue(
				json.contains("\"type\": \"ic2r:macerator\"") || json.contains("\"type\":\"ic2r:macerator\""),
				"JSON must declare type ic2r:macerator"
			);
			assertTrue(json.contains("minecraft:cobblestone"));
			assertTrue(json.contains("minecraft:sand"));
		}

		assertEquals("ic2r:macerator", MachineRecipeMatchMath.MACERATOR_RECIPE_TYPE_ID);
		assertEquals("macerator", MachineRecipeMatchMath.MACERATOR_RECIPE_PATH);
	}
}
