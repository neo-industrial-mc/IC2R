package me.halfcooler.ic2r.integration.jei.recipe.machine;

import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;

public class IORecipeWrapper implements IJeiRecipeWrapper
{
	private final IRecipeInput input;
	private final Collection<ItemStack> output;

	public IORecipeWrapper(MachineRecipe<IRecipeInput, Collection<ItemStack>> container)
	{
		this(container.getInput(), container.getOutput());
	}

	public IORecipeWrapper(IRecipeInput input, Collection<ItemStack> output)
	{
		this.input = input;
		this.output = output;
	}

	public List<List<ItemStack>> getInputs()
	{
		List<ItemStack> inputs = this.input.getInputs();
		return inputs.isEmpty() ? Collections.emptyList() : Collections.singletonList(inputs);
	}

	public List<ItemStack> getOutputs()
	{
		return new ArrayList<>(this.output);
	}
}
