package me.halfcooler.ic2r.core.uu;

import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityRecycler;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;

public class RecyclerResolver implements ILateRecipeResolver
{
	private static final double transformCost = 55.0 * TileEntityRecycler.recycleChance() / 4000.0 * 107.0;

	@Override
	public List<RecipeTransformation> getTransformations(Iterable<LeanItemStack> obtainableStacks)
	{
		List<LeanItemStack> input = new ArrayList<>();

		for (LeanItemStack obtainableStack : obtainableStacks)
		{
			ItemStack stack = obtainableStack.toMcStack();
			if (!StackUtil.isEmpty(stack) && !Recipes.recycler.apply(stack, false).getOutput().isEmpty())
			{
				input.add(new LeanItemStack(stack, TileEntityRecycler.recycleChance()));
			}
		}

		return List.of(new RecipeTransformation(transformCost, Collections.singletonList(input), new LeanItemStack(Ic2rItems.SCRAP)));
	}
}
