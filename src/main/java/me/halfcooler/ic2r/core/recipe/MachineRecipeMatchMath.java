package me.halfcooler.ic2r.core.recipe;

import java.util.Map;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * Pure matching / amount gates for basic machine recipes (W2.3 / G1.6 / RC-*).
 * No Minecraft types — unit-testable without registry or {@code RecipeManager} bootstrap.
 * <p>
 * Runtime path (datapack → serializer → vanilla {@code RecipeManager} →
 * {@link me.halfcooler.ic2r.core.recipe.v2.RecipeManagerMachineBridge} →
 * {@link BasicMachineRecipeManager}) uses the same amount rules as
 * {@link BasicMachineRecipeManager#getOutputFor}.
 * <p>
 * Item/tag/ore identity here is expressed as id tokens or pre-resolved membership flags so tests
 * avoid {@code BuiltInRegistries}; adapters map {@code ItemStack}/{@code Ingredient} into these gates.
 */
public final class MachineRecipeMatchMath
{
	private MachineRecipeMatchMath()
	{
	}

	/**
	 * Whether the offered stack count is enough for the recipe input amount.
	 */
	public static boolean hasSufficientCount(int stackCount, int recipeAmount)
	{
		return stackCount > 0 && recipeAmount > 0 && stackCount >= recipeAmount;
	}

	/**
	 * Amount gate used by {@link BasicMachineRecipeManager#getOutputFor}:
	 * enough count, and if the input has a crafting remainder the stack must be exact.
	 *
	 * @param stackCount          offered item count
	 * @param recipeAmount        required input amount
	 * @param hasRecipeRemainder  true when the input item leaves a container remainder
	 */
	public static boolean canApplyInput(int stackCount, int recipeAmount, boolean hasRecipeRemainder)
	{
		if (!hasSufficientCount(stackCount, recipeAmount))
		{
			return false;
		}

		return !hasRecipeRemainder || stackCount == recipeAmount;
	}

	/**
	 * Combined accept gate used by recipe scan: identity/tag already matched, then amount/remainder.
	 * Mirrors {@link me.halfcooler.ic2r.core.recipe.v2.RecipeManagerMachineBridge#findMatching}.
	 */
	public static boolean acceptsMatchedInput(
		boolean itemMatches,
		int stackCount,
		int recipeAmount,
		boolean hasRecipeRemainder
	)
	{
		return itemMatches && canApplyInput(stackCount, recipeAmount, hasRecipeRemainder);
	}

	/**
	 * RC-001 item identity: exact registry-id (or test token) equality.
	 * Empty/null required or subject never matches.
	 */
	public static boolean matchesExactItem(String requiredId, String subjectId)
	{
		return requiredId != null
			&& !requiredId.isEmpty()
			&& subjectId != null
			&& requiredId.equals(subjectId);
	}

	/**
	 * RC-001 NBT/component partial match: every required key must be present on subject with equal value.
	 * Empty/null required map always accepts (same spirit as no required NBT on {@code RecipeInputItemStack}).
	 * Null subject fails only when required is non-empty.
	 */
	public static boolean matchesRequiredKeys(Map<String, String> required, Map<String, String> subject)
	{
		if (required == null || required.isEmpty())
		{
			return true;
		}

		if (subject == null)
		{
			return false;
		}

		for (Map.Entry<String, String> entry : required.entrySet())
		{
			if (!subject.containsKey(entry.getKey()))
			{
				return false;
			}

			if (!Objects.equals(subject.get(entry.getKey()), entry.getValue()))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * RC-002 tag / RC-003 ore / multi-input: subject accepted if it equals any candidate id.
	 * Mirrors {@code Ingredient}/{@code RecipeInputMultiple} any-of membership without registries.
	 */
	public static boolean matchesAnyCandidate(String subjectId, Iterable<String> candidates)
	{
		if (subjectId == null || subjectId.isEmpty() || candidates == null)
		{
			return false;
		}

		for (String candidate : candidates)
		{
			if (subjectId.equals(candidate))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * RC-004 / RC-005 recycler filter (see {@code TileEntityRecycler#getIsItemBlacklisted}):
	 * <ul>
	 *   <li>Whitelist empty → blacklist mode: reject only if {@code inBlacklist}</li>
	 *   <li>Whitelist non-empty → whitelist mode: reject unless {@code inWhitelist}</li>
	 * </ul>
	 * When rejected, recycler yields no scrap.
	 */
	public static boolean isRecyclerRejected(boolean whitelistEmpty, boolean inBlacklist, boolean inWhitelist)
	{
		return whitelistEmpty ? inBlacklist : !inWhitelist;
	}

	/**
	 * Stack count after consuming {@code recipeAmount} (non-remainder path).
	 * Returns 0 when exact; never negative.
	 */
	public static int countAfterConsume(int stackCount, int recipeAmount)
	{
		if (stackCount <= 0 || recipeAmount <= 0)
		{
			return Math.max(0, stackCount);
		}

		return Math.max(0, stackCount - recipeAmount);
	}

	/**
	 * First matching index in insertion/load order (first-wins; SM-010 / RC match order).
	 *
	 * @return index in {@code [0, recipeCount)}, or {@code -1}
	 */
	public static int firstMatchIndex(int recipeCount, IntPredicate matches)
	{
		if (recipeCount <= 0 || matches == null)
		{
			return -1;
		}

		for (int i = 0; i < recipeCount; i++)
		{
			if (matches.test(i))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * First element that satisfies {@code matches}, else {@code null}.
	 */
	public static <T> T firstMatch(Iterable<? extends T> recipes, Predicate<? super T> matches)
	{
		if (recipes == null || matches == null)
		{
			return null;
		}

		for (T recipe : recipes)
		{
			if (matches.test(recipe))
			{
				return recipe;
			}
		}

		return null;
	}

	/**
	 * Datapack type ids for basic machines sharing {@code RecipeManagerMachineBridge#loadBasic}
	 * (W2.3 macerator pilot; G2.2 extractor/compressor multi-type evidence).
	 * Kept here so pure tests can lock type strings without loading registries.
	 */
	public static final String MACERATOR_RECIPE_TYPE_ID = "ic2r:macerator";

	/** Path segment under {@code data/ic2r/recipes/} for macerator. */
	public static final String MACERATOR_RECIPE_PATH = "macerator";

	/** Second basic type full-chain evidence ({@code ic2r:extractor}). */
	public static final String EXTRACTOR_RECIPE_TYPE_ID = "ic2r:extractor";

	public static final String EXTRACTOR_RECIPE_PATH = "extractor";

	/** Third basic type full-chain evidence ({@code ic2r:compressor}). */
	public static final String COMPRESSOR_RECIPE_TYPE_ID = "ic2r:compressor";

	public static final String COMPRESSOR_RECIPE_PATH = "compressor";

	/**
	 * Pure stand-in for {@code RecipeManagerMachineBridge#findMatching} without Minecraft types.
	 * Each candidate is (itemId, recipeAmount); first that {@link #acceptsMatchedInput} wins.
	 *
	 * @return index of first match, or {@code -1}
	 */
	public static int findMatchingIndex(
		String subjectItemId,
		int stackCount,
		boolean hasRecipeRemainder,
		String[] candidateItemIds,
		int[] candidateAmounts
	)
	{
		if (subjectItemId == null || subjectItemId.isEmpty()
			|| candidateItemIds == null || candidateAmounts == null
			|| candidateItemIds.length != candidateAmounts.length)
		{
			return -1;
		}

		return firstMatchIndex(candidateItemIds.length, i -> acceptsMatchedInput(
			matchesExactItem(candidateItemIds[i], subjectItemId),
			stackCount,
			candidateAmounts[i],
			hasRecipeRemainder
		));
	}
}
