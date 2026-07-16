package me.halfcooler.ic2r.core.recipe.v2;

public final class RecipeSerializerMath
{
	public static final byte BASIC_NETWORK_MARKER = 0;

	private RecipeSerializerMath()
	{
	}
	
	public static boolean isBasicNetworkMarker(byte type)
	{
		return type == BASIC_NETWORK_MARKER;
	}
	
	public static boolean hasBasicMachineJsonKeys(boolean hasIngredient, boolean hasResult)
	{
		return hasIngredient && hasResult;
	}
	
	public static int countOrDefault(boolean hasCountKey, int declaredCount, int defaultCount)
	{
		return hasCountKey ? declaredCount : defaultCount;
	}
	
	public enum InputShape
	{
		ARRAY,
		FLUID,
		ITEM_DATA,
		ANY,
		INGREDIENT
	}
	
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
