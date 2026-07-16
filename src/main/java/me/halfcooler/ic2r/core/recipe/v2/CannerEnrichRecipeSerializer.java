package me.halfcooler.ic2r.core.recipe.v2;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import me.halfcooler.ic2r.api.recipe.ICannerEnrichRecipeManager;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.ref.Ic2rRecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class CannerEnrichRecipeSerializer implements RecipeSerializer<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack>>
{
	private static final ResourceLocation RUNTIME_ID = ResourceLocation.fromNamespaceAndPath("ic2r", "runtime");

	private final MapCodec<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack>> codec =
		JsonRecipeCodecs.mapCodec(this::fromJsonObject);
	private final StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack>> streamCodec =
		JsonRecipeCodecs.streamCodec(this::fromNetworkBuf, this::toNetworkBuf);

	private RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack> fromJsonObject(JsonObject json)
	{
		Ic2rFluidStack input = RecipeIo.parseFluidStack(GsonHelper.getAsJsonObject(json, "input_ingredient"));
		IRecipeInput additive = RecipeIo.parseInput(json.get("additive_ingredient"));
		Ic2rFluidStack result = RecipeIo.parseFluidStack(GsonHelper.getAsJsonObject(json, "result"));
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerEnrichRecipeManager.Input(input, additive), result), RUNTIME_ID, this, Ic2rRecipeTypes.CANNER_ENRICH);
	}

	private RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack> fromNetworkBuf(RegistryFriendlyByteBuf buf)
	{
		Ic2rFluidStack input = RecipeIo.readFluidStack(buf);
		IRecipeInput additive = RecipeIo.readInput(buf);
		Ic2rFluidStack result = RecipeIo.readFluidStack(buf);
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerEnrichRecipeManager.Input(input, additive), result), RUNTIME_ID, this, Ic2rRecipeTypes.CANNER_ENRICH);
	}

	private void toNetworkBuf(RegistryFriendlyByteBuf buf, RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack> recipe)
	{
		RecipeIo.writeFluidStack(buf, recipe.recipe().getInput().fluid());
		RecipeIo.writeInput(buf, recipe.recipe().getInput().additive());
		RecipeIo.writeFluidStack(buf, recipe.recipe().getOutput());
	}

	@Override
	public @NotNull MapCodec<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack>> codec()
	{
		return this.codec;
	}

	@Override
	public @NotNull StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2rFluidStack>> streamCodec()
	{
		return this.streamCodec;
	}
}
