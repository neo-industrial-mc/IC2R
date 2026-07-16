package me.halfcooler.ic2r.recipe;

import me.halfcooler.ic2r.core.recipe.v2.RecipeSerializerMath;
import me.halfcooler.ic2r.core.recipe.v2.RecipeSerializerMath.InputShape;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure serializer/network/JSON shape gates (G2.4) — no Level / RecipeManager bootstrap.
 * Mirrors {@code BasicMachineRecipeSerializer} network marker and {@code RecipeIo#parseInput}
 * branch order.
 */
class RecipeSerializerMathTest
{
	@Test
	void basic_network_marker_is_zero_only()
	{
		assertEquals(0, RecipeSerializerMath.BASIC_NETWORK_MARKER);
		assertTrue(RecipeSerializerMath.isBasicNetworkMarker((byte) 0));
		assertFalse(RecipeSerializerMath.isBasicNetworkMarker((byte) 1));
		assertFalse(RecipeSerializerMath.isBasicNetworkMarker((byte) -1));
		assertFalse(RecipeSerializerMath.isBasicNetworkMarker((byte) 2));
	}

	@Test
	void basic_machine_json_requires_ingredient_and_result()
	{
		assertTrue(RecipeSerializerMath.hasBasicMachineJsonKeys(true, true));
		assertFalse(RecipeSerializerMath.hasBasicMachineJsonKeys(true, false));
		assertFalse(RecipeSerializerMath.hasBasicMachineJsonKeys(false, true));
		assertFalse(RecipeSerializerMath.hasBasicMachineJsonKeys(false, false));
	}

	@Test
	void countOrDefault_missing_uses_default_present_keeps_zero()
	{
		assertEquals(1, RecipeSerializerMath.countOrDefault(false, 99, 1));
		assertEquals(8, RecipeSerializerMath.countOrDefault(true, 8, 1));
		assertEquals(0, RecipeSerializerMath.countOrDefault(true, 0, 1));
	}

	/**
	 * RecipeIo.parseInput order: array → fluid → data → any → ingredient.
	 * Priority is fixed (fluid wins over data if both present).
	 */
	@Test
	void classifyInputShape_priority_array_fluid_data_any_ingredient()
	{
		assertEquals(InputShape.ARRAY, RecipeSerializerMath.classifyInputShape(true, true, true, true));
		assertEquals(InputShape.FLUID, RecipeSerializerMath.classifyInputShape(false, true, true, true));
		assertEquals(InputShape.ITEM_DATA, RecipeSerializerMath.classifyInputShape(false, false, true, true));
		assertEquals(InputShape.ANY, RecipeSerializerMath.classifyInputShape(false, false, false, true));
		assertEquals(InputShape.INGREDIENT, RecipeSerializerMath.classifyInputShape(false, false, false, false));
	}

	/** Datapack smoke: macerator JSON has the two keys BasicMachineRecipeSerializer expects. */
	@Test
	void macerator_datapack_has_basic_machine_keys() throws Exception
	{
		String resource = "data/ic2r/recipe/macerator/cobblestone_to_sand.json";
		try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource))
		{
			assertNotNull(in, "expected classpath resource " + resource);
			String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			boolean hasIngredient = json.contains("\"ingredient\"");
			boolean hasResult = json.contains("\"result\"");
			assertTrue(RecipeSerializerMath.hasBasicMachineJsonKeys(hasIngredient, hasResult));
		}
	}
}
