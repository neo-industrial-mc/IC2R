package me.halfcooler.ic2r.core.recipe;

import me.halfcooler.ic2r.api.recipe.IBasicMachineRecipeManager;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import me.halfcooler.ic2r.api.recipe.RecipeOutput;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class BasicMachineRecipeManager extends MachineRecipeHelper<IRecipeInput, Collection<ItemStack>> implements IBasicMachineRecipeManager
{
	private static boolean checkListEquality(Collection<ItemStack> a, Collection<ItemStack> b)
	{
		if (a.size() != b.size())
		{
			return false;
		}

		ListIterator<ItemStack> itB = new ArrayList<>(b).listIterator();

		label32:
		for (ItemStack stack : a)
		{
			while (itB.hasNext())
			{
				if (StackUtil.checkItemEqualityStrict(stack, itB.next()))
				{
					itB.remove();

					while (itB.hasPrevious())
					{
						itB.previous();
					}
					continue label32;
				}
			}

			return false;
		}

		return true;
	}

	protected IRecipeInput getForInput(IRecipeInput input)
	{
		return input;
	}

	@Override
	protected boolean consumeContainer(ItemStack input, ItemStack inContainer, MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe)
	{
		for (ItemStack output : recipe.getOutput())
		{
			if (StackUtil.checkItemEqualityStrict(inContainer, output))
			{
				return true;
			}

			if (IC2R.envProxy.hasRecipeRemainder(output) && StackUtil.checkItemEqualityStrict(input, IC2R.envProxy.getRecipeRemainder(output)))
			{
				return true;
			}
		}

		return false;
	}

	public boolean addRecipe(MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe, boolean replace)
	{
		if (recipe == null)
		{
			throw new NullPointerException("null recipe");
		}

		if (recipe.getInput() == null)
		{
			throw new NullPointerException("null recipe input");
		}

		if (recipe.getOutput() == null)
		{
			throw new NullPointerException("null recipe output");
		}

		if (recipe.getOutput().isEmpty())
		{
			throw new IllegalArgumentException("no outputs");
		}

		IRecipeInput input = recipe.getInput();

		for (ItemStack is : input.getInputs())
		{
			MachineRecipe<IRecipeInput, Collection<ItemStack>> recipeGet = this.getRecipe(is);
			if (recipeGet != null)
			{
				if (!replace)
				{
					IC2R.log
						.debug(
							LogCategory.Recipe,
							"Skipping %s => %s due to duplicate recipe for %s (%s => %s)",
							input,
							recipeGet.getOutput(),
							is,
							recipeGet.getInput(),
							recipeGet.getOutput()
						);
					return false;
				}

				while (true)
				{
					this.recipes.remove(recipeGet.getInput());
					this.removeCachedRecipes(input);
					recipeGet = this.getRecipe(is);
					if (recipeGet == null)
					{
						break;
					}
				}
			}
		}

		this.recipes.put(input, recipe);
		this.addToCache(recipe);
		return true;
	}

	public boolean addRecipe(IRecipeInput input, CompoundTag metadata, boolean replace, ItemStack... outputs)
	{
		return this.addRecipe(input, Arrays.asList(outputs), metadata, replace);
	}

	public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, CompoundTag metadata, boolean replace)
	{
		if (input == null)
		{
			throw new NullPointerException("null recipe input");
		}

		if (output == null)
		{
			throw new NullPointerException("null recipe output");
		}

		if (output.isEmpty())
		{
			throw new IllegalArgumentException("no outputs");
		}

		List<ItemStack> items = new ArrayList<>(output.size());

		for (ItemStack stack : output)
		{
			if (StackUtil.isEmpty(stack))
			{
				this.displayError("The output ItemStack " + StackUtil.toStringSafe(stack) + " is invalid.");
				return false;
			}

			if (input.matches(stack) && (metadata == null || !metadata.contains("ignoreSameInputOutput")))
			{
				this.displayError("The output ItemStack " + stack.toString() + " is the same as the recipe input " + input + ".");
				return false;
			}

			items.add(stack.copy());
		}

		for (ItemStack is : input.getInputs())
		{
			MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = this.getRecipe(is);
			if (recipe != null)
			{
				if (!replace)
				{
					IC2R.log
						.debug(
							LogCategory.Recipe,
							"Skipping %s => %s due to duplicate recipe for %s (%s => %s)",
							input,
							output,
							is,
							recipe.getInput(),
							recipe.getOutput()
						);
					return false;
				}

				while (true)
				{
					this.recipes.remove(input);
					this.removeCachedRecipes(input);
					recipe = this.getRecipe(is);
					if (recipe == null)
					{
						break;
					}
				}
			}
		}

		MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = new MachineRecipe<>(input, items, metadata);
		this.recipes.put(input, recipe);
		this.addToCache(recipe);
		return true;
	}

	@Override
	public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput)
	{
		MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = this.getRecipe(input);
		if (recipe == null)
		{
			return null;
		}

		boolean hasRemainder = IC2R.envProxy.hasRecipeRemainder(input);
		if (MachineRecipeMatchMath.canApplyInput(StackUtil.getSize(input), recipe.getInput().getAmount(), hasRemainder))
		{
			if (adjustInput)
			{
				if (hasRemainder)
				{
					throw new UnsupportedOperationException("can't adjust input item, use apply() instead");
				}

				input.shrink(recipe.getInput().getAmount());
			}

			return new RecipeOutput(recipe.getMetaData(), new ArrayList<>(recipe.getOutput()));
		} else
		{
			return null;
		}
	}

	public void removeRecipe(ItemStack input, Collection<ItemStack> output)
	{
		MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = this.getRecipe(input);
		if (recipe != null && checkListEquality(recipe.getOutput(), output))
		{
			this.recipes.remove(recipe.getInput());
			this.removeCachedRecipes(recipe.getInput());
		}
	}

	private void displayError(String msg)
	{
		if (IC2RConfig.recipes.ignoreInvalidRecipes.get())
		{
			IC2R.log.warn(LogCategory.Recipe, msg);
		} else
		{
			throw new RuntimeException(msg);
		}
	}
}
