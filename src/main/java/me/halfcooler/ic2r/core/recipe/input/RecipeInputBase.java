package me.halfcooler.ic2r.core.recipe.input;

import com.google.gson.JsonElement;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;

public abstract class RecipeInputBase implements IRecipeInput
{
	private List<ItemStack> inputs = null;

	protected RecipeInputBase()
	{
	}

	public abstract JsonElement toJson();

	protected abstract List<ItemStack> listStacks();

	@Override
	public final List<ItemStack> getInputs()
	{
		if (this.inputs == null)
		{
			this.inputs = new ArrayList<>(this.listStacks());
			this.inputs.replaceAll(stack -> StackUtil.setImmutableSize(stack, this.getAmount()));
			this.inputs = Collections.unmodifiableList(this.inputs);
		}

		return this.inputs;
	}
}
