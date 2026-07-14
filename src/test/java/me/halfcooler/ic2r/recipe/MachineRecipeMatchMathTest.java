package me.halfcooler.ic2r.recipe;

import me.halfcooler.ic2r.core.recipe.MachineRecipeMatchMath;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-logic recipe match gates + macerator pilot datapack smoke (W2.3 / G1.6 / RC-*).
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

	// ========== G1.6 deepen: RC-001 … RC-005 pure gates ==========

	/** @Spec RC-001 item 精确匹配：只接受相同 id token */
	@Test
	void matchesExactItem_only_accepts_same_id()
	{
		assertTrue(MachineRecipeMatchMath.matchesExactItem("minecraft:iron_ingot", "minecraft:iron_ingot"));
		assertFalse(MachineRecipeMatchMath.matchesExactItem("minecraft:iron_ingot", "minecraft:gold_ingot"));
		assertFalse(MachineRecipeMatchMath.matchesExactItem("minecraft:iron_ingot", null));
		assertFalse(MachineRecipeMatchMath.matchesExactItem(null, "minecraft:iron_ingot"));
		assertFalse(MachineRecipeMatchMath.matchesExactItem("", "minecraft:iron_ingot"));
		assertFalse(MachineRecipeMatchMath.matchesExactItem("minecraft:iron_ingot", ""));
	}

	/** @Spec RC-001 + 数量门闩：不足拒绝、刚好/过量（无 remainder）接受；错物品拒绝 */
	@Test
	void acceptsMatchedInput_insufficient_exact_excess_and_wrong_item()
	{
		// wrong item — amount irrelevant
		assertFalse(MachineRecipeMatchMath.acceptsMatchedInput(false, 8, 2, false));

		// amount: insufficient / exact / excess (no remainder)
		assertFalse(MachineRecipeMatchMath.acceptsMatchedInput(true, 1, 2, false));
		assertTrue(MachineRecipeMatchMath.acceptsMatchedInput(true, 2, 2, false));
		assertTrue(MachineRecipeMatchMath.acceptsMatchedInput(true, 5, 2, false));

		// remainder: excess rejected, exact accepted
		assertFalse(MachineRecipeMatchMath.acceptsMatchedInput(true, 3, 2, true));
		assertTrue(MachineRecipeMatchMath.acceptsMatchedInput(true, 2, 2, true));
	}

	/** @Spec RC-001 NBT/组件：required ⊆ subject 且值相等 */
	@Test
	void matchesRequiredKeys_partial_nbt_subset()
	{
		assertTrue(MachineRecipeMatchMath.matchesRequiredKeys(null, null));
		assertTrue(MachineRecipeMatchMath.matchesRequiredKeys(Map.of(), Map.of("extra", "x")));

		Map<String, String> required = Map.of("damage", "0", "tier", "1");
		assertTrue(MachineRecipeMatchMath.matchesRequiredKeys(required, Map.of("damage", "0", "tier", "1", "extra", "z")));
		assertFalse(MachineRecipeMatchMath.matchesRequiredKeys(required, Map.of("damage", "0")));
		assertFalse(MachineRecipeMatchMath.matchesRequiredKeys(required, Map.of("damage", "1", "tier", "1")));
		assertFalse(MachineRecipeMatchMath.matchesRequiredKeys(required, null));
	}

	/** @Spec RC-002 tag 匹配 / RC-003 ore 等价：任一候选 id 命中即接受，非成员拒绝 */
	@Test
	void matchesAnyCandidate_tag_or_ore_members()
	{
		List<String> ores = List.of("minecraft:iron_ore", "minecraft:deepslate_iron_ore", "ic2r:nether_iron_ore");

		assertTrue(MachineRecipeMatchMath.matchesAnyCandidate("minecraft:iron_ore", ores));
		assertTrue(MachineRecipeMatchMath.matchesAnyCandidate("ic2r:nether_iron_ore", ores));
		assertFalse(MachineRecipeMatchMath.matchesAnyCandidate("minecraft:gold_ore", ores));
		assertFalse(MachineRecipeMatchMath.matchesAnyCandidate(null, ores));
		assertFalse(MachineRecipeMatchMath.matchesAnyCandidate("minecraft:iron_ore", null));
		assertFalse(MachineRecipeMatchMath.matchesAnyCandidate("", ores));
		assertFalse(MachineRecipeMatchMath.matchesAnyCandidate("minecraft:iron_ore", List.of()));
	}

	/** @Spec RC-004 白名单：非空白名单时仅名单内接受（外拒 = isRecyclerRejected true） */
	@Test
	void isRecyclerRejected_whitelist_mode_accepts_only_listed()
	{
		// whitelist mode (empty=false): reject unless inWhitelist
		assertFalse(MachineRecipeMatchMath.isRecyclerRejected(false, true, true));  // listed → accept
		assertTrue(MachineRecipeMatchMath.isRecyclerRejected(false, false, false)); // outside → reject
		assertTrue(MachineRecipeMatchMath.isRecyclerRejected(false, true, false));  // blacklist ignored
	}

	/** @Spec RC-005 黑名单：白名单空时名单内永不匹配（reject）；名单外可回收 */
	@Test
	void isRecyclerRejected_blacklist_mode_when_whitelist_empty()
	{
		assertTrue(MachineRecipeMatchMath.isRecyclerRejected(true, true, false));   // blacklisted
		assertFalse(MachineRecipeMatchMath.isRecyclerRejected(true, false, false)); // not listed → accept
		assertFalse(MachineRecipeMatchMath.isRecyclerRejected(true, false, true));  // whitelist flag ignored
	}

	/** @Spec SM-010 / RC 数量+顺序：first-wins 跳过数量不足的先序配方，命中下一道 */
	@Test
	void firstMatch_skips_amount_fail_then_picks_next()
	{
		// Simulated recipes: (id, amount). Subject: iron_ingot x2
		record Recipe(String id, int amount)
		{
		}

		List<Recipe> recipes = List.of(
			new Recipe("minecraft:iron_ingot", 4), // matches item, amount fail
			new Recipe("minecraft:gold_ingot", 1), // wrong item
			new Recipe("minecraft:iron_ingot", 2)  // match + amount ok
		);

		Recipe hit = MachineRecipeMatchMath.firstMatch(
			recipes,
			r -> MachineRecipeMatchMath.acceptsMatchedInput(
				MachineRecipeMatchMath.matchesExactItem(r.id(), "minecraft:iron_ingot"),
				2,
				r.amount(),
				false
			)
		);

		assertNotNull(hit);
		assertEquals(2, hit.amount());
		assertEquals("minecraft:iron_ingot", hit.id());

		// all amount-insufficient → null
		assertNull(MachineRecipeMatchMath.firstMatch(
			List.of(new Recipe("minecraft:iron_ingot", 8)),
			r -> MachineRecipeMatchMath.acceptsMatchedInput(true, 2, r.amount(), false)
		));
	}
}
