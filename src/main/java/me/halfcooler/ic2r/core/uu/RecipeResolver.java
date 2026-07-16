package me.halfcooler.ic2r.core.uu;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.platform.services.PlatformServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.RegistryAccess;

/**
 * Vanilla crafting table recipes → UU graph edges.
 * Only {@link RecipeType#CRAFTING} is used so smelting / machine types are not double-counted.
 */
public class RecipeResolver implements IRecipeResolver
{
	private static final double transformCost = 1.0;

	@Override
	public List<RecipeTransformation> getTransformations()
	{
		RecipeManager recipeManager;
		RegistryAccess registryAccess;
		try
		{
			recipeManager = IC2R.sideProxy.getRecipeManager();
			registryAccess = resolveRegistryAccess();
		} catch (Exception e)
		{
			IC2R.log.debug(LogCategory.Uu, e, "Crafting recipes unavailable for UU graph.");
			return Collections.emptyList();
		}

		if (recipeManager == null || registryAccess == null)
		{
			return Collections.emptyList();
		}

		List<RecipeTransformation> ret = new ArrayList<>();

		for (var vanillaHolder : recipeManager.getAllRecipesFor(RecipeType.CRAFTING))
		{
			try
			{
				CraftingRecipe recipe = vanillaHolder.value();
				NonNullList<Ingredient> ingredients = recipe.getIngredients();
				if (ingredients.isEmpty())
				{
					continue;
				}

				ItemStack output = recipe.getResultItem(registryAccess);
				if (StackUtil.isEmpty(output))
				{
					continue;
				}

				List<List<LeanItemStack>> inputs = RecipeUtil.convertIngredients(ingredients);
				if (inputs.isEmpty())
				{
					continue;
				}

				ret.add(new RecipeTransformation(transformCost, inputs, new LeanItemStack(output)));
			} catch (RuntimeException e)
			{
				IC2R.log.debug(LogCategory.Uu, e, "skipped crafting recipe for UU graph");
			}
		}

		return ret;
	}

	private static RegistryAccess resolveRegistryAccess()
	{
		MinecraftServer server = PlatformServices.lifecycle().getServer();
		if (server != null)
		{
			return server.registryAccess();
		}

		return null;
	}
}
