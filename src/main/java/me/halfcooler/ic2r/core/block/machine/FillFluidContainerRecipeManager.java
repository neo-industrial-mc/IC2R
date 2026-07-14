package me.halfcooler.ic2r.core.block.machine;

import me.halfcooler.ic2r.api.recipe.IFillFluidContainerRecipeManager;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import me.halfcooler.ic2r.api.recipe.MachineRecipeResult;
import me.halfcooler.ic2r.api.util.FluidContainerOutputMode;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.util.LiquidUtil;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class FillFluidContainerRecipeManager implements IFillFluidContainerRecipeManager
{
	public boolean addRecipe(Void input, Collection<ItemStack> output, CompoundTag metadata, boolean replace)
	{
		return false;
	}

	public MachineRecipeResult<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input> apply(
		IFillFluidContainerRecipeManager.Input input, boolean acceptTest
	)
	{
		return this.apply(input, FluidContainerOutputMode.AnyToOutput, acceptTest);
	}

	@Override
	public MachineRecipeResult<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input> apply(
		IFillFluidContainerRecipeManager.Input input, FluidContainerOutputMode outputMode, boolean acceptTest
	)
	{
		if (!StackUtil.isEmpty(input.container()) && input.fluid() != null)
		{
			if (input.fluid().isEmpty())
			{
				return null;
			}

			LiquidUtil.FluidOperationResult result = LiquidUtil.fillContainer(input.container(), input.fluid(), outputMode);
			if (result == null)
			{
				return null;
			}

			Collection<ItemStack> output = StackUtil.isEmpty(result.extraOutput) ? Collections.emptyList() : Collections.singletonList(result.extraOutput);
			Ic2rFluidStack changedFluid = result.fluidChange.getAmountMb() >= input.fluid().getAmountMb()
				? null
				: input.fluid().copyWithAmountMb(input.fluid().getAmountMb() - result.fluidChange.getAmountMb());
			return (MachineRecipeResult) new MachineRecipe<>(null, output).getResult(new IFillFluidContainerRecipeManager.Input(result.inPlaceOutput, changedFluid));
		} else if (!acceptTest)
		{
			return null;
		} else if (StackUtil.isEmpty(input.container()) && input.fluid() == null)
		{
			return null;
		} else
		{
			return !StackUtil.isEmpty(input.container()) && !LiquidUtil.isFillableFluidContainer(input.container())
				? null
				: (MachineRecipeResult) new MachineRecipe<>(null, Collections.emptyList()).getResult(input);
		}
	}

	@Override
	public Iterable<? extends MachineRecipe<Void, Collection<ItemStack>>> getRecipes()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isIterable()
	{
		return false;
	}
}
