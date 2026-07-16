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
 * Pure-logic recipe match gates + basic-machine datapack smoke (W2.3 / G1.6 / G2.2 / RC-*).
 * No Level / RecipeManager bootstrap.
 * <p>
 * Runtime bridge (documented on {@code RecipeManagerMachineBridge}):
 * JSON {@code ic2r:macerator|extractor|compressor} → Serializer → RecipeManager →
 * {@code loadBasic} → {@code Recipes.*} (shared {@code Rezepte#basicRecipe}).
 * Direct-query path: {@code findMatching} / pure {@link MachineRecipeMatchMath#findMatchingIndex}.
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

	// --- RC-006 (partial): datapack JSON exists and declares type id ---

	@Test
	void macerator_datapack_json_declares_pilot_type() throws Exception
	{
		assertDatapackDeclaresType(
			"data/ic2r/recipe/macerator/cobblestone_to_sand.json",
			MachineRecipeMatchMath.MACERATOR_RECIPE_TYPE_ID,
			"minecraft:cobblestone",
			"minecraft:sand"
		);
		assertEquals("ic2r:macerator", MachineRecipeMatchMath.MACERATOR_RECIPE_TYPE_ID);
		assertEquals("macerator", MachineRecipeMatchMath.MACERATOR_RECIPE_PATH);
	}

	/** G2.2 / RC-006: second basic type (extractor) shares JSON→type id chain with macerator. */
	@Test
	void extractor_datapack_json_declares_type() throws Exception
	{
		assertDatapackDeclaresType(
			"data/ic2r/recipe/extractor/resin_to_rubber.json",
			MachineRecipeMatchMath.EXTRACTOR_RECIPE_TYPE_ID,
			"ic2r:resin",
			"ic2r:rubber"
		);
		assertEquals("ic2r:extractor", MachineRecipeMatchMath.EXTRACTOR_RECIPE_TYPE_ID);
		assertEquals("extractor", MachineRecipeMatchMath.EXTRACTOR_RECIPE_PATH);
	}

	/** G2.2 / RC-006: compressor JSON type smoke (third basic type, same bridge). */
	@Test
	void compressor_datapack_json_declares_type() throws Exception
	{
		assertDatapackDeclaresType(
			"data/ic2r/recipe/compressor/sand_to_sandstone.json",
			MachineRecipeMatchMath.COMPRESSOR_RECIPE_TYPE_ID,
			"minecraft:sand",
			"minecraft:sandstone"
		);
		assertEquals("ic2r:compressor", MachineRecipeMatchMath.COMPRESSOR_RECIPE_TYPE_ID);
		assertEquals("compressor", MachineRecipeMatchMath.COMPRESSOR_RECIPE_PATH);
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

	/**
	 * G2.2: pure stand-in for {@code RecipeManagerMachineBridge#findMatching} —
	 * first-wins + {@link MachineRecipeMatchMath#acceptsMatchedInput} (amount + remainder).
	 * Mirrors compressor-style multi-count inputs (e.g. 4 sand → sandstone).
	 */
	@Test
	void findMatchingIndex_first_wins_with_amount_and_remainder()
	{
		// candidates: wrong item; sand needs 4; sand needs 1 (later — must not win when 4 matches)
		String[] ids = {
			"minecraft:cobblestone",
			"minecraft:sand",
			"minecraft:sand"
		};
		int[] amounts = {1, 4, 1};

		// enough sand for amount-4 recipe → index 1
		assertEquals(1, MachineRecipeMatchMath.findMatchingIndex(
			"minecraft:sand", 4, false, ids, amounts
		));
		// only 2 sand → skip amount-4, hit amount-1 at index 2
		assertEquals(2, MachineRecipeMatchMath.findMatchingIndex(
			"minecraft:sand", 2, false, ids, amounts
		));
		// empty / unknown → -1
		assertEquals(-1, MachineRecipeMatchMath.findMatchingIndex(
			"minecraft:dirt", 8, false, ids, amounts
		));
		assertEquals(-1, MachineRecipeMatchMath.findMatchingIndex(
			null, 4, false, ids, amounts
		));

		// remainder: stack must equal recipe amount
		assertEquals(1, MachineRecipeMatchMath.findMatchingIndex(
			"minecraft:sand", 4, true, ids, amounts
		));
		// 5 sand + remainder → neither sand recipe (4≠5, and after skip 1≠5) if we only had those;
		// with amount-1 still failing exact 5 → -1 for remainder path on both sand entries
		assertEquals(-1, MachineRecipeMatchMath.findMatchingIndex(
			"minecraft:sand", 5, true, ids, amounts
		));
		// exact 1 → hits index 2 (index 1 needs exact 4)
		assertEquals(2, MachineRecipeMatchMath.findMatchingIndex(
			"minecraft:sand", 1, true, ids, amounts
		));

		// length mismatch / null arrays
		assertEquals(-1, MachineRecipeMatchMath.findMatchingIndex(
			"minecraft:sand", 1, false, ids, new int[] {1}
		));
		assertEquals(-1, MachineRecipeMatchMath.findMatchingIndex(
			"minecraft:sand", 1, false, null, amounts
		));
	}

	/** G2.2: findMatching + acceptsMatchedInput combo for extractor single-item spirit (amount 1). */
	@Test
	void findMatchingIndex_extractor_style_single_acceptsMatchedInput()
	{
		String[] ids = {"ic2r:resin", "ic2r:rubber_log"};
		int[] amounts = {1, 1};

		assertEquals(0, MachineRecipeMatchMath.findMatchingIndex(
			"ic2r:resin", 3, false, ids, amounts
		));
		assertTrue(MachineRecipeMatchMath.acceptsMatchedInput(
			MachineRecipeMatchMath.matchesExactItem("ic2r:resin", "ic2r:resin"),
			3, 1, false
		));
		assertFalse(MachineRecipeMatchMath.acceptsMatchedInput(
			MachineRecipeMatchMath.matchesExactItem("ic2r:resin", "ic2r:rubber"),
			3, 1, false
		));
		assertEquals(1, MachineRecipeMatchMath.findMatchingIndex(
			"ic2r:rubber_log", 1, false, ids, amounts
		));
		assertEquals(-1, MachineRecipeMatchMath.findMatchingIndex(
			"ic2r:rubber", 1, false, ids, amounts
		));
	}

	private static void assertDatapackDeclaresType(
		String resource,
		String typeId,
		String inputToken,
		String outputToken
	) throws Exception
	{
		try (InputStream in = openClasspathResource(resource))
		{
			assertNotNull(in, "expected classpath resource " + resource);
			String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			assertTrue(
				json.contains("\"type\": \"" + typeId + "\"") || json.contains("\"type\":\"" + typeId + "\""),
				"JSON must declare type " + typeId
			);
			assertTrue(json.contains(inputToken), "expected input token " + inputToken);
			assertTrue(json.contains(outputToken), "expected output token " + outputToken);
		}
	}

	/**
	 * NeoForge unitTest runs under a TRANSFORMER/mod classloader; prefer the test
	 * class loader, then fall back to the thread context loader.
	 */
	private static InputStream openClasspathResource(String resource)
	{
		InputStream in = MachineRecipeMatchMathTest.class.getClassLoader().getResourceAsStream(resource);
		if (in == null)
		{
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
		}
		return in;
	}
}
