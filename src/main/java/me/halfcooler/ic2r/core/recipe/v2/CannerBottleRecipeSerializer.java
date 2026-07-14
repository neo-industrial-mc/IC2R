package me.halfcooler.ic2r.core.recipe.v2;

import com.google.gson.JsonObject;
import me.halfcooler.ic2r.api.recipe.ICannerBottleRecipeManager;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import me.halfcooler.ic2r.core.ref.Ic2rRecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CannerBottleRecipeSerializer implements RecipeSerializer<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>>
{
	public RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> fromJson(ResourceLocation id, JsonObject json)
	{
		IRecipeInput container = RecipeIo.parseInput(json.get("container_ingredient"));
		IRecipeInput fill = RecipeIo.parseInput(json.get("fill_ingredient"));
		ItemStack output = RecipeIo.parseOutput(GsonHelper.getAsJsonObject(json, "result"));
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerBottleRecipeManager.Input(container, fill), output), id, this, Ic2rRecipeTypes.CANNER_BOTTLE);
	}

	public RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
	{
		IRecipeInput container = RecipeIo.readInput(buf);
		IRecipeInput fill = RecipeIo.readInput(buf);
		ItemStack output = buf.readItem();
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerBottleRecipeManager.Input(container, fill), output), id, this, Ic2rRecipeTypes.CANNER_BOTTLE);
	}

	public void toNetwork(FriendlyByteBuf buf, RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> recipe)
	{
		RecipeIo.writeInput(buf, recipe.recipe().getInput().container());
		RecipeIo.writeInput(buf, recipe.recipe().getInput().fill());
		buf.writeItem(recipe.recipe().getOutput());
	}
}
