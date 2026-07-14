package me.halfcooler.ic2r.core.recipe.v2;

/**
 * Pure JSON/network shape helpers for machine recipe serializers (G2.4).
 * No Minecraft registry / Level / RecipeManager — unit-testable without bootstrap.
 * <p>
 * Mirrors {@link BasicMachineRecipeSerializer} network marker and {@link RecipeIo#parseInput}
 * branching order (array → fluid → data → any → vanilla ingredient).
 */
public final class RecipeSerializerMath
{
	/** Network type byte written by {@link BasicMachineRecipeSerializer#toNetwork}. */
	public static final byte BASIC_NETWORK_MARKER = 0;

	private RecipeSerializerMath()
	{
	}

	/**
	 * Whether the network type marker is the basic-machine recipe layout (byte 0).
	 */
	public static boolean isBasicNetworkMarker(byte type)
	{
		return type == BASIC_NETWORK_MARKER;
	}

	/**
	 * Basic datapack recipe objects require both {@code ingredient} and {@code result} keys.
	 */
	public static boolean hasBasicMachineJsonKeys(boolean hasIngredient, boolean hasResult)
	{
		return hasIngredient && hasResult;
	}

	/**
	 * Count field: when key absent use default (typically 1); when present keep declared value
	 * (including 0 — same as GsonHelper.getAsInt defaulting only on missing keys).
	 */
	public static int countOrDefault(boolean hasCountKey, int declaredCount, int defaultCount)
	{
		return hasCountKey ? declaredCount : defaultCount;
	}

	/**
	 * Pre-registry classification of an input JSON element (RecipeIo.parseInput order).
	 */
	public enum InputShape
	{
		ARRAY,
		FLUID,
		ITEM_DATA,
		ANY,
		INGREDIENT
	}

	/**
	 * Classify input shape without resolving items/fluids.
	 *
	 * @param isArray  true if root element is a JSON array
	 * @param hasFluid object has {@code fluid} key
	 * @param hasData  object has {@code data} key
	 * @param hasAny   object has {@code any} key
	 */
	public static InputShape classifyInputShape(boolean isArray, boolean hasFluid, boolean hasData, boolean hasAny)
	{
		if (isArray)
		{
			return InputShape.ARRAY;
		}

		if (hasFluid)
		{
			return InputShape.FLUID;
		}

		if (hasData)
		{
			return InputShape.ITEM_DATA;
		}

		if (hasAny)
		{
			return InputShape.ANY;
		}

		return InputShape.INGREDIENT;
	}
}
