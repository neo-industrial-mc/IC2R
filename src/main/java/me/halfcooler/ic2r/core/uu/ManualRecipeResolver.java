package me.halfcooler.ic2r.core.uu;

import me.halfcooler.ic2r.core.ref.Ic2rItems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ManualRecipeResolver implements IRecipeResolver
{
	private static final double transformCost = 0.0;

	private static RecipeTransformation toTransform(Item input, Item output)
	{
		return toTransform(new ItemStack(input), new ItemStack(output));
	}

	private static RecipeTransformation toTransform(ItemStack input, ItemStack output)
	{
		List<List<LeanItemStack>> inputs = Collections.singletonList(Collections.singletonList(new LeanItemStack(input)));
		List<LeanItemStack> outputs = Collections.singletonList(new LeanItemStack(output));
		return new RecipeTransformation(0.0, inputs, outputs);
	}

	@Override
	public List<RecipeTransformation> getTransformations()
	{
		List<RecipeTransformation> ret = new ArrayList<>();
		ret.add(toTransform(Ic2rItems.URANIUM_FUEL_ROD, Ic2rItems.DEPLETED_URANIUM_FUEL_ROD));
		ret.add(toTransform(Ic2rItems.DUAL_URANIUM_FUEL_ROD, Ic2rItems.DEPLETED_DUAL_URANIUM_FUEL_ROD));
		ret.add(toTransform(Ic2rItems.QUAD_URANIUM_FUEL_ROD, Ic2rItems.DEPLETED_QUAD_URANIUM_FUEL_ROD));
		ret.add(toTransform(Ic2rItems.MOX_FUEL_ROD, Ic2rItems.DEPLETED_MOX_FUEL_ROD));
		ret.add(toTransform(Ic2rItems.DUAL_MOX_FUEL_ROD, Ic2rItems.DEPLETED_DUAL_MOX_FUEL_ROD));
		ret.add(toTransform(Ic2rItems.QUAD_MOX_FUEL_ROD, Ic2rItems.DEPLETED_QUAD_MOX_FUEL_ROD));
		return ret;
	}
}
