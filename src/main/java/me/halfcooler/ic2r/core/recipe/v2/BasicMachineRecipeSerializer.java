package me.halfcooler.ic2r.core.recipe.v2;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

public class BasicMachineRecipeSerializer implements RecipeSerializer<RecipeHolder<IRecipeInput, Collection<ItemStack>>>
{
	private static final ResourceLocation RUNTIME_ID = ResourceLocation.fromNamespaceAndPath("ic2r", "runtime");

	private final RecipeType<?> recipeType;
	@Nullable
	private final Function<JsonObject, CompoundTag> metaProcessor;
	private final MapCodec<RecipeHolder<IRecipeInput, Collection<ItemStack>>> codec;
	private final StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<IRecipeInput, Collection<ItemStack>>> streamCodec;

	public BasicMachineRecipeSerializer(RecipeType<?> recipeType, @Nullable Function<JsonObject, CompoundTag> metaProcessor)
	{
		this.recipeType = recipeType;
		this.metaProcessor = metaProcessor;
		this.codec = JsonRecipeCodecs.mapCodec(this::fromJsonObject);
		this.streamCodec = JsonRecipeCodecs.streamCodec(this::fromNetworkBuf, this::toNetworkBuf);
	}

	private RecipeHolder<IRecipeInput, Collection<ItemStack>> fromJsonObject(JsonObject json)
	{
		IRecipeInput input = RecipeIo.parseInput(json.get("ingredient"));
		Collection<ItemStack> output = RecipeIo.parseOutputs(json.get("result"), "result");
		CompoundTag meta = this.metaProcessor != null ? this.metaProcessor.apply(json) : null;
		return new RecipeHolder<>(new MachineRecipe<>(input, output, meta), RUNTIME_ID, this, this.recipeType);
	}

	private RecipeHolder<IRecipeInput, Collection<ItemStack>> fromNetworkBuf(RegistryFriendlyByteBuf buf)
	{
		byte type = buf.readByte();
		if (!RecipeSerializerMath.isBasicNetworkMarker(type))
		{
			throw new IllegalStateException("Reading recipe error! Wrong basic-machine network marker");
		}
		return new RecipeHolder<>(new MachineRecipe<>(RecipeIo.readInput(buf), RecipeIo.readOutput(buf), buf.readNbt()), RUNTIME_ID, this, this.recipeType);
	}

	private void toNetworkBuf(RegistryFriendlyByteBuf buf, RecipeHolder<IRecipeInput, Collection<ItemStack>> recipe)
	{
		buf.writeByte(RecipeSerializerMath.BASIC_NETWORK_MARKER);
		RecipeIo.writeInput(buf, recipe.recipe().getInput());
		RecipeIo.writeOutput(buf, recipe.recipe().getOutput());
		buf.writeNbt(recipe.recipe().getMetaData());
	}

	@Override
	public @NotNull MapCodec<RecipeHolder<IRecipeInput, Collection<ItemStack>>> codec()
	{
		return this.codec;
	}

	@Override
	public @NotNull StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<IRecipeInput, Collection<ItemStack>>> streamCodec()
	{
		return this.streamCodec;
	}
}
