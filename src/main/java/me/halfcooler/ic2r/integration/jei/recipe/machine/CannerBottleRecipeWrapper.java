package me.halfcooler.ic2r.integration.jei.recipe.machine;

import me.halfcooler.ic2r.api.recipe.ICannerBottleRecipeManager;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;

import java.util.List;

import net.minecraft.world.item.ItemStack;

public class CannerBottleRecipeWrapper implements IJeiRecipeWrapper
{
	private final ICannerBottleRecipeManager.Input input;
	private final ItemStack output;

	public CannerBottleRecipeWrapper(MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack> recipe)
	{
		this.input = recipe.getInput();
		this.output = recipe.getOutput();
	}

	@Override
	public List<List<ItemStack>> getInputs()
	{
		return List.of(this.input.container().getInputs(), this.input.fill().getInputs());
	}

	@Override
	public List<ItemStack> getOutputs()
	{
		return List.of(this.output);
	}
}
