package me.halfcooler.ic2r.core.block.machine;

import me.halfcooler.ic2r.api.recipe.ICannerEnrichRecipeManager;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import me.halfcooler.ic2r.api.recipe.MachineRecipeResult;
import me.halfcooler.ic2r.api.recipe.RecipeOutput;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.util.LiquidUtil;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class CannerEnrichRecipeManager implements ICannerEnrichRecipeManager
{
	private final List<MachineRecipe<ICannerEnrichRecipeManager.Input, Ic2rFluidStack>> recipes = new ArrayList<>();

	public boolean addRecipe(ICannerEnrichRecipeManager.Input input, Ic2rFluidStack output, CompoundTag metadata, boolean replace)
	{
		if (input.fluid() == null)
		{
			throw new NullPointerException("The fluid recipe input is null.");
		}

		if (input.additive() == null)
		{
			throw new NullPointerException("The additive recipe input is null.");
		}

		if (output == null)
		{
			throw new NullPointerException("The recipe output is null.");
		}

		if (!LiquidUtil.check(input.fluid()))
		{
			throw new IllegalArgumentException("The fluid recipe input is invalid.");
		}

		if (!LiquidUtil.check(output))
		{
			throw new IllegalArgumentException("The fluid recipe output is invalid.");
		}

		for (ItemStack stack : input.additive().getInputs())
		{
			MachineRecipe<ICannerEnrichRecipeManager.Input, Ic2rFluidStack> recipe = this.getRecipe(input.fluid(), stack, true);
			if (recipe != null)
			{
				if (!replace)
				{
					return false;
				}

				this.recipes.remove(recipe);
			}
		}

		this.recipes.add(new MachineRecipe<>(input, output));
		return true;
	}

	public void addRecipe(Ic2rFluidStack fluid, IRecipeInput additive, Ic2rFluidStack output)
	{
		if (!this.addRecipe(new ICannerEnrichRecipeManager.Input(fluid, additive), output, null, false))
		{
			throw new RuntimeException("ambiguous recipe: [" + fluid + "+" + additive.getInputs() + " -> " + output + "]");
		}
	}

	public MachineRecipeResult<ICannerEnrichRecipeManager.Input, Ic2rFluidStack, ICannerEnrichRecipeManager.RawInput> apply(
		ICannerEnrichRecipeManager.RawInput input, boolean acceptTest
	)
	{
		MachineRecipe<ICannerEnrichRecipeManager.Input, Ic2rFluidStack> recipe = this.getRecipe(input.fluid(), input.additive(), acceptTest);
		if (recipe == null)
		{
			return null;
		}

		Ic2rFluidStack remainingFluid;
		if (input.fluid() == null)
		{
			remainingFluid = null;
		} else
		{
			remainingFluid = input.fluid().copy();
			remainingFluid.decreaseMb(recipe.getInput().fluid().getAmountMb());
			if (remainingFluid.isEmpty())
			{
				remainingFluid = null;
			}
		}

		return recipe.getResult(
			new ICannerEnrichRecipeManager.RawInput(remainingFluid, StackUtil.copyShrunk(input.additive(), recipe.getInput().additive().getAmount()))
		);
	}

	private MachineRecipe<ICannerEnrichRecipeManager.Input, Ic2rFluidStack> getRecipe(Ic2rFluidStack fluid, ItemStack additive, boolean acceptTest)
	{
		if (acceptTest || fluid != null && !StackUtil.isEmpty(additive))
		{
			for (MachineRecipe<ICannerEnrichRecipeManager.Input, Ic2rFluidStack> recipe : this.recipes)
			{
				if ((fluid == null || fluid.hasExactFluid(recipe.getInput().fluid()) && (acceptTest || recipe.getInput().fluid().getAmountMb() <= fluid.getAmountMb()))
					&& (
					additive == null
						|| recipe.getInput().additive().matches(additive) && (acceptTest || recipe.getInput().additive().getAmount() <= StackUtil.getSize(additive))
				))
				{
					return recipe;
				}
			}

			return null;
		} else
		{
			return null;
		}
	}

	@Override
	public RecipeOutput getOutputFor(Ic2rFluidStack fluid, ItemStack additive, boolean adjustInput, boolean acceptTest)
	{
		MachineRecipeResult<ICannerEnrichRecipeManager.Input, Ic2rFluidStack, ICannerEnrichRecipeManager.RawInput> result = this.apply(
			new ICannerEnrichRecipeManager.RawInput(fluid, additive), acceptTest
		);
		if (result == null)
		{
			return null;
		}

		if (adjustInput)
		{
			fluid.setAmountMb(result.adjustedInput().fluid() == null ? 0 : result.adjustedInput().fluid().getAmountMb());
			additive.setCount(StackUtil.isEmpty(result.adjustedInput().additive()) ? 0 : StackUtil.getSize(result.adjustedInput().additive()));
		}

		CompoundTag output = new CompoundTag();
		result.getOutput().toNbt(output);
		return new RecipeOutput(output);
	}

	@Override
	public Iterable<? extends MachineRecipe<ICannerEnrichRecipeManager.Input, Ic2rFluidStack>> getRecipes()
	{
		return this.recipes;
	}

	@Override
	public boolean isIterable()
	{
		return true;
	}
}
