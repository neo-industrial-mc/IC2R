package ic2.core.recipe.v2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.ref.Ic2RecipeTypes;

import java.util.stream.Stream;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CannerEnrichRecipeSerializer implements RecipeSerializer<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>>
{
	private final MapCodec<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>> codec = new MapCodec<>()
	{
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops)
		{
			return Stream.of("input_ingredient", "additive_ingredient", "result").map(ops::createString);
		}

		@Override
		public <T> DataResult<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>> decode(DynamicOps<T> ops, MapLike<T> input)
		{
			try
			{
				JsonObject json = new JsonObject();
				input.entries().forEach(entry ->
				{
					JsonElement key = ops.convertTo(JsonOps.INSTANCE, entry.getFirst());
					json.add(key.getAsString(), ops.convertTo(JsonOps.INSTANCE, entry.getSecond()));
				});
				return DataResult.success(fromJson(json));
			} catch (RuntimeException e)
			{
				return DataResult.error(() -> "Failed to parse IC2 canner enrich recipe: " + e.getMessage());
			}
		}

		@Override
		public <T> RecordBuilder<T> encode(RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> recipe, DynamicOps<T> ops, RecordBuilder<T> prefix)
		{
			return prefix.withErrorsFrom(DataResult.error(() -> "IC2 canner enrich recipes cannot be encoded"));
		}
	};
	private final StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>> streamCodec =
		StreamCodec.of(this::toNetwork, this::fromNetwork);

	private RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> fromJson(JsonObject json)
	{
		Ic2FluidStack input = RecipeIo.parseFluidStack(GsonHelper.getAsJsonObject(json, "input_ingredient"));
		IRecipeInput additive = RecipeIo.parseInput(json.get("additive_ingredient"));
		Ic2FluidStack result = RecipeIo.parseFluidStack(GsonHelper.getAsJsonObject(json, "result"));
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerEnrichRecipeManager.Input(input, additive), result), null, this, Ic2RecipeTypes.CANNER_ENRICH);
	}

	private RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> fromNetwork(RegistryFriendlyByteBuf buf)
	{
		Ic2FluidStack input = RecipeIo.readFluidStack(buf);
		IRecipeInput additive = RecipeIo.readInput(buf);
		Ic2FluidStack result = RecipeIo.readFluidStack(buf);
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerEnrichRecipeManager.Input(input, additive), result), null, this, Ic2RecipeTypes.CANNER_ENRICH);
	}

	private void toNetwork(RegistryFriendlyByteBuf buf, RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> recipe)
	{
		RecipeIo.writeFluidStack(buf, recipe.recipe().getInput().fluid());
		RecipeIo.writeInput(buf, recipe.recipe().getInput().additive());
		RecipeIo.writeFluidStack(buf, recipe.recipe().getOutput());
	}

	@Override
	public MapCodec<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>> codec()
	{
		return this.codec;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>> streamCodec()
	{
		return this.streamCodec;
	}
}
