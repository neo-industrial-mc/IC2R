package me.halfcooler.ic2r.core.recipe;

import me.halfcooler.ic2r.core.fluid.FluidTransferMath;

import java.util.Map;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public final class MachineRecipeMatchMath
{
	public static final String MACERATOR_RECIPE_TYPE_ID = "ic2r:macerator";
	public static final String MACERATOR_RECIPE_PATH = "macerator";
	public static final String EXTRACTOR_RECIPE_TYPE_ID = "ic2r:extractor";
	public static final String EXTRACTOR_RECIPE_PATH = "extractor";
	public static final String COMPRESSOR_RECIPE_TYPE_ID = "ic2r:compressor";
	public static final String COMPRESSOR_RECIPE_PATH = "compressor";

	private MachineRecipeMatchMath()
	{
	}

	public static boolean hasSufficientCount(int stackCount, int recipeAmount)
	{
		return recipeAmount > 0 && stackCount >= recipeAmount;
	}

	public static boolean canApplyInput(int stackCount, int recipeAmount, boolean hasRecipeRemainder)
	{
		if (!hasSufficientCount(stackCount, recipeAmount))
		{
			return false;
		}

		return !hasRecipeRemainder || stackCount == recipeAmount;
	}

	public static boolean acceptsMatchedInput(
		boolean itemMatches,
		int stackCount,
		int recipeAmount,
		boolean hasRecipeRemainder
	)
	{
		return itemMatches && canApplyInput(stackCount, recipeAmount, hasRecipeRemainder);
	}

	public static boolean matchesExactItem(String requiredId, String subjectId)
	{
		return requiredId != null
			&& !requiredId.isEmpty()
			&& requiredId.equals(subjectId);
	}

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

	public static boolean isRecyclerRejected(boolean whitelistEmpty, boolean inBlacklist, boolean inWhitelist)
	{
		return whitelistEmpty ? inBlacklist : !inWhitelist;
	}

	public static int countAfterConsume(int stackCount, int recipeAmount)
	{
		return FluidTransferMath.remainingOfferAfterFill(stackCount, recipeAmount);
	}

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
