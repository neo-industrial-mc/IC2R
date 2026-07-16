package me.halfcooler.ic2r.core.uu;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.StackUtil;
import me.halfcooler.ic2r.platform.services.PlatformServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.core.RegistryAccess;

/**
 * Vanilla furnace smelting recipes → UU graph edges.
 */
public class VanillaSmeltingResolver implements IRecipeResolver
{
	private static final double transformCost = 14.0;

	@Override
	public List<RecipeTransformation> getTransformations()
	{
		RecipeManager recipeManager;
		RegistryAccess registryAccess;
		try
		{
			recipeManager = IC2R.sideProxy.getRecipeManager();
			MinecraftServer server = PlatformServices.lifecycle().getServer();
			registryAccess = server != null ? server.registryAccess() : null;
		} catch (Exception e)
		{
			IC2R.log.debug(LogCategory.Uu, e, "Smelting recipes unavailable for UU graph.");
			return Collections.emptyList();
		}

		if (recipeManager == null || registryAccess == null)
		{
			return Collections.emptyList();
		}

		List<RecipeTransformation> ret = new ArrayList<>();

		for (SmeltingRecipe recipe : recipeManager.getAllRecipesFor(RecipeType.SMELTING))
		{
			try
			{
				List<List<LeanItemStack>> inputs = RecipeUtil.convertIngredients(recipe.getIngredients());
				if (inputs.isEmpty())
				{
					continue;
				}

				ItemStack outputStack = recipe.getResultItem(registryAccess);
				if (StackUtil.isEmpty(outputStack))
				{
					continue;
				}

				ret.add(new RecipeTransformation(transformCost, inputs, new LeanItemStack(outputStack)));
			} catch (IllegalArgumentException e)
			{
				IC2R.log.warn(LogCategory.Uu, e, "invalid smelting recipe for UU graph");
			} catch (RuntimeException e)
			{
				IC2R.log.debug(LogCategory.Uu, e, "skipped smelting recipe for UU graph");
			}
		}

		return ret;
	}
}
