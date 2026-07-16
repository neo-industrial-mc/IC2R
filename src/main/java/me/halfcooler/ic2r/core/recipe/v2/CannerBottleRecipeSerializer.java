package me.halfcooler.ic2r.core.recipe.v2;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import me.halfcooler.ic2r.api.recipe.ICannerBottleRecipeManager;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import me.halfcooler.ic2r.core.ref.Ic2rRecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class CannerBottleRecipeSerializer implements RecipeSerializer<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>>
{
	private static final ResourceLocation RUNTIME_ID = ResourceLocation.fromNamespaceAndPath("ic2r", "runtime");

	private final MapCodec<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> codec =
		JsonRecipeCodecs.mapCodec(this::fromJsonObject);
	private final StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> streamCodec =
		JsonRecipeCodecs.streamCodec(this::fromNetworkBuf, this::toNetworkBuf);

	private RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> fromJsonObject(JsonObject json)
	{
		IRecipeInput container = RecipeIo.parseInput(json.get("container_ingredient"));
		IRecipeInput fill = RecipeIo.parseInput(json.get("fill_ingredient"));
		ItemStack output = RecipeIo.parseOutput(GsonHelper.getAsJsonObject(json, "result"));
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerBottleRecipeManager.Input(container, fill), output), RUNTIME_ID, this, Ic2rRecipeTypes.CANNER_BOTTLE);
	}

	private RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> fromNetworkBuf(RegistryFriendlyByteBuf buf)
	{
		IRecipeInput container = RecipeIo.readInput(buf);
		IRecipeInput fill = RecipeIo.readInput(buf);
		ItemStack output = RecipeIo.readItemStack(buf);
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerBottleRecipeManager.Input(container, fill), output), RUNTIME_ID, this, Ic2rRecipeTypes.CANNER_BOTTLE);
	}

	private void toNetworkBuf(RegistryFriendlyByteBuf buf, RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> recipe)
	{
		RecipeIo.writeInput(buf, recipe.recipe().getInput().container());
		RecipeIo.writeInput(buf, recipe.recipe().getInput().fill());
		RecipeIo.writeItemStack(buf, recipe.recipe().getOutput());
	}

	@Override
	public @NotNull MapCodec<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> codec()
	{
		return this.codec;
	}

	@Override
	public @NotNull StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> streamCodec()
	{
		return this.streamCodec;
	}
}
