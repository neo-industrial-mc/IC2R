package me.halfcooler.ic2r.core.recipe.v2;

import com.google.gson.JsonObject;
import me.halfcooler.ic2r.api.recipe.ICannerEnrichRecipeManager;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.ref.Ic2rRecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CannerEnrichRecipeSerializer implements RecipeSerializer<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack>>
{
	public RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack> fromJson(ResourceLocation id, JsonObject json)
	{
		Ic2rFluidStack input = RecipeIo.parseFluidStack(GsonHelper.getAsJsonObject(json, "input_ingredient"));
		IRecipeInput additive = RecipeIo.parseInput(json.get("additive_ingredient"));
		Ic2rFluidStack result = RecipeIo.parseFluidStack(GsonHelper.getAsJsonObject(json, "result"));
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerEnrichRecipeManager.Input(input, additive), result), id, this, Ic2rRecipeTypes.CANNER_ENRICH);
	}

	public RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack> fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
	{
		Ic2rFluidStack input = RecipeIo.readFluidStack(buf);
		IRecipeInput additive = RecipeIo.readInput(buf);
		Ic2rFluidStack result = RecipeIo.readFluidStack(buf);
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerEnrichRecipeManager.Input(input, additive), result), id, this, Ic2rRecipeTypes.CANNER_ENRICH);
	}

	public void toNetwork(FriendlyByteBuf buf, RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack> recipe)
	{
		RecipeIo.writeFluidStack(buf, recipe.recipe().getInput().fluid());
		RecipeIo.writeInput(buf, recipe.recipe().getInput().additive());
		RecipeIo.writeFluidStack(buf, recipe.recipe().getOutput());
	}
}
