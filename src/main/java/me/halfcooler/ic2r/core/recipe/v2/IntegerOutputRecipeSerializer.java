package me.halfcooler.ic2r.core.recipe.v2;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class IntegerOutputRecipeSerializer implements RecipeSerializer<RecipeHolder<IRecipeInput, Integer>>
{
	private static final ResourceLocation RUNTIME_ID = ResourceLocation.fromNamespaceAndPath("ic2r", "runtime");

	private final RecipeType<?> recipeType;
	@Nullable
	private final Function<JsonObject, CompoundTag> metaProcessor;
	private final MapCodec<RecipeHolder<IRecipeInput, Integer>> codec;
	private final StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<IRecipeInput, Integer>> streamCodec;

	public IntegerOutputRecipeSerializer(RecipeType<?> recipeType, @Nullable Function<JsonObject, CompoundTag> metaProcessor)
	{
		this.recipeType = recipeType;
		this.metaProcessor = metaProcessor;
		this.codec = JsonRecipeCodecs.mapCodec(this::fromJsonObject);
		this.streamCodec = JsonRecipeCodecs.streamCodec(this::fromNetworkBuf, this::toNetworkBuf);
	}

	private RecipeHolder<IRecipeInput, Integer> fromJsonObject(JsonObject json)
	{
		IRecipeInput input = RecipeIo.parseInput(json.get("ingredient"));
		int output = json.get("result").getAsInt();
		CompoundTag meta = this.metaProcessor != null ? this.metaProcessor.apply(json) : null;
		return new RecipeHolder<>(new MachineRecipe<>(input, output, meta), RUNTIME_ID, this, this.recipeType);
	}

	private RecipeHolder<IRecipeInput, Integer> fromNetworkBuf(RegistryFriendlyByteBuf buf)
	{
		byte type = buf.readByte();
		if (type != 0)
		{
			throw new IllegalStateException("Reading recipe error! Wrong integer-output network marker");
		}
		return new RecipeHolder<>(new MachineRecipe<>(RecipeIo.readInput(buf), RecipeIo.readIntegerOutput(buf), buf.readNbt()), RUNTIME_ID, this, this.recipeType);
	}

	private void toNetworkBuf(RegistryFriendlyByteBuf buf, RecipeHolder<IRecipeInput, Integer> recipe)
	{
		buf.writeByte(0);
		RecipeIo.writeInput(buf, recipe.recipe().getInput());
		RecipeIo.writeIntegerOutput(buf, recipe.recipe().getOutput());
		buf.writeNbt(recipe.recipe().getMetaData());
	}

	@Override
	public @NotNull MapCodec<RecipeHolder<IRecipeInput, Integer>> codec()
	{
		return this.codec;
	}

	@Override
	public @NotNull StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<IRecipeInput, Integer>> streamCodec()
	{
		return this.streamCodec;
	}
}
