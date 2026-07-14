package me.halfcooler.ic2r.core.recipe;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * Pure matching / amount gates for basic machine recipes (W2.3 / RC-*).
 * No Minecraft types — unit-testable without registry or {@code RecipeManager} bootstrap.
 * <p>
 * Runtime path (datapack → serializer → vanilla {@code RecipeManager} →
 * {@link me.halfcooler.ic2r.core.recipe.v2.RecipeManagerMachineBridge} →
 * {@link BasicMachineRecipeManager}) uses the same amount rules as
 * {@link BasicMachineRecipeManager#getOutputFor}.
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
	 * Datapack type id for the macerator pilot ({@code ic2r:macerator}).
	 * Kept here so pure tests can lock the pilot type string without loading registries.
	 */
	public static final String MACERATOR_RECIPE_TYPE_ID = "ic2r:macerator";

	/** Path segment under {@code data/ic2r/recipes/} for the pilot type. */
	public static final String MACERATOR_RECIPE_PATH = "macerator";
}
