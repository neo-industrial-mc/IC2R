package me.halfcooler.ic2r.core.init;

import me.halfcooler.ic2r.api.recipe.IBasicMachineRecipeManager;
import me.halfcooler.ic2r.api.recipe.ICannerBottleRecipeManager;
import me.halfcooler.ic2r.api.recipe.ICannerEnrichRecipeManager;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.core.block.machine.CannerBottleRecipeManager;
import me.halfcooler.ic2r.core.block.machine.CannerEnrichRecipeManager;
import me.halfcooler.ic2r.core.block.machine.EmptyFluidContainerRecipeManager;
import me.halfcooler.ic2r.core.block.machine.FillFluidContainerRecipeManager;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.recipe.MatterAmplifierRecipeManager;
import me.halfcooler.ic2r.core.recipe.SmeltingRecipeManager;
import me.halfcooler.ic2r.core.recipe.v2.RecipeHolder;
import me.halfcooler.ic2r.core.recipe.v2.RecipeManagerGetter;
import me.halfcooler.ic2r.core.recipe.v2.RecipeManagerMachineBridge;
import me.halfcooler.ic2r.core.ref.Ic2rRecipeTypes;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class Rezepte
{
	static void loadRecipes()
	{
		Recipes.furnace = new SmeltingRecipeManager();
		Recipes.emptyFluidContainer = new EmptyFluidContainerRecipeManager();
		Recipes.fillFluidContainer = new FillFluidContainerRecipeManager();
		Recipes.macerator = basicRecipe(Ic2rRecipeTypes.MACERATOR);
		Recipes.extractor = basicRecipe(Ic2rRecipeTypes.EXTRACTOR);
		Recipes.compressor = basicRecipe(Ic2rRecipeTypes.COMPRESSOR);
		Recipes.centrifuge = basicRecipe(Ic2rRecipeTypes.CENTRIFUGE);
		Recipes.block_cutter = basicRecipe(Ic2rRecipeTypes.BLOCK_CUTTER);
		Recipes.blast_furnace = basicRecipe(Ic2rRecipeTypes.BLAST_FURNACE);
		Recipes.metalformerExtruding = basicRecipe(Ic2rRecipeTypes.METAL_FORMER_EXTRUDING);
		Recipes.metalformerCutting = basicRecipe(Ic2rRecipeTypes.METAL_FORMER_CUTTING);
		Recipes.metalformerRolling = basicRecipe(Ic2rRecipeTypes.METAL_FORMER_ROLLING);
		Recipes.oreWashing = basicRecipe(Ic2rRecipeTypes.ORE_WASHER);
		Recipes.matterFabricator = new RecipeManagerGetter<>(recipeManager ->
		{
			MatterAmplifierRecipeManager manager = new MatterAmplifierRecipeManager();

			// 1.21 RecipeManager wraps recipes in vanilla RecipeHolder; unwrap .value()
			for (var vanillaHolder : recipeManager.getAllRecipesFor(Ic2rRecipeTypes.MATTER_FABRICATOR))
			{
				RecipeHolder<IRecipeInput, Integer> holder = vanillaHolder.value();
				manager.addRecipe(holder.recipe().getInput(), holder.recipe().getOutput(), null, false);
			}

			return manager;
		});
		Recipes.cannerBottle = new RecipeManagerGetter<>(recipeManager ->
		{
			CannerBottleRecipeManager manager = new CannerBottleRecipeManager();

			for (var vanillaHolder : recipeManager.getAllRecipesFor(Ic2rRecipeTypes.CANNER_BOTTLE))
			{
				RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> holder = vanillaHolder.value();
				manager.addRecipe(holder.recipe().getInput(), holder.recipe().getOutput(), null, false);
			}

			return manager;
		});
		Recipes.cannerEnrich = new RecipeManagerGetter<>(recipeManager ->
		{
			CannerEnrichRecipeManager manager = new CannerEnrichRecipeManager();

			for (var vanillaHolder : recipeManager.getAllRecipesFor(Ic2rRecipeTypes.CANNER_ENRICH))
			{
				RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack> holder = vanillaHolder.value();
				manager.addRecipe(holder.recipe().getInput(), holder.recipe().getOutput(), null, false);
			}

			return manager;
		});
	}

	public static void registerRecipes()
	{
		loadRecipes();
	}

	/**
	 * Shared basic-machine wiring (G2.2): every type uses the same
	 * JSON → Serializer → RecipeManager → {@link RecipeManagerMachineBridge#loadBasic} →
	 * {@link RecipeManagerGetter} → {@link Recipes}.* path.
	 * Evidence types: macerator (W2.3), extractor, compressor, plus other basic fields above.
	 */
	private static RecipeManagerGetter<IBasicMachineRecipeManager> basicRecipe(RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> recipeType)
	{
		return new RecipeManagerGetter<>(recipeManager -> RecipeManagerMachineBridge.loadBasic(recipeManager, recipeType));
	}
}
