package me.halfcooler.ic2r.core.ref;

import me.halfcooler.ic2r.api.recipe.ICannerBottleRecipeManager;
import me.halfcooler.ic2r.api.recipe.ICannerEnrichRecipeManager;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.recipe.v2.RecipeHolder;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class Ic2rRecipeTypes
{
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> MACERATOR = register("macerator");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> EXTRACTOR = register("extractor");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> COMPRESSOR = register("compressor");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> CENTRIFUGE = register("centrifuge");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> BLOCK_CUTTER = register("block_cutter");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> BLAST_FURNACE = register("blast_furnace");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> METAL_FORMER_EXTRUDING = register("metal_former_extruding");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> METAL_FORMER_CUTTING = register("metal_former_cutting");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> METAL_FORMER_ROLLING = register("metal_former_rolling");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> ORE_WASHER = register("ore_washing");
	public static final RecipeType<RecipeHolder<IRecipeInput, Integer>> MATTER_FABRICATOR = register("matter_fabricator");
	public static final RecipeType<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> CANNER_BOTTLE = register("canner_bottle");
	public static final RecipeType<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack>> CANNER_ENRICH = register("canner_enrich");

	public static void init()
	{
	}

	private static <T extends Recipe<?>> RecipeType<T> register(String name)
	{
		return IC2R.envProxy.registerRecipeType(IC2R.getIdentifier(name));
	}
}
