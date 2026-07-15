package me.halfcooler.ic2r.core.uu;

import me.halfcooler.ic2r.api.recipe.IBasicMachineRecipeManager;
import me.halfcooler.ic2r.api.recipe.IMachineRecipeManager;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.platform.services.PlatformServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Pulls IC2 machine recipes into the UU graph.
 * Managers obtained via {@link Recipes.IGetter} are resolved only when the server (and recipes) exist.
 */
public class MachineRecipeResolver implements IRecipeResolver
{
	private static final double transformCost = 14.0;
	private final Supplier<IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>> managerSupplier;

	public MachineRecipeResolver(IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> manager)
	{
		this.managerSupplier = () -> manager;
	}

	public MachineRecipeResolver(Recipes.IGetter<? extends IBasicMachineRecipeManager> getter)
	{
		this.managerSupplier = () ->
		{
			Level level = resolveLevel();
			return level == null ? null : getter.get(level);
		};
	}

	private static Level resolveLevel()
	{
		MinecraftServer server = PlatformServices.lifecycle().getServer();
		return server != null ? server.overworld() : null;
	}

	@Override
	public List<RecipeTransformation> getTransformations()
	{
		IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> manager = this.managerSupplier.get();
		if (manager == null || !manager.isIterable())
		{
			return Collections.emptyList();
		}

		List<RecipeTransformation> ret = new ArrayList<>();

		for (MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe : manager.getRecipes())
		{
			try
			{
				List<ItemStack> inputStacks = recipe.getInput().getInputs();
				if (inputStacks == null || inputStacks.isEmpty())
				{
					continue;
				}

				List<List<LeanItemStack>> inputs = RecipeUtil.convertInputs(inputStacks);
				List<LeanItemStack> outputs = RecipeUtil.convertOutputs(recipe.getOutput());
				if (outputs.isEmpty())
				{
					continue;
				}

				ret.add(new RecipeTransformation(transformCost, inputs, outputs));
			} catch (IllegalArgumentException e)
			{
				IC2R.log.warn(LogCategory.Uu, e, "invalid machine recipe for UU graph");
			} catch (RuntimeException e)
			{
				IC2R.log.debug(LogCategory.Uu, e, "skipped machine recipe for UU graph");
			}
		}

		return ret;
	}
}
