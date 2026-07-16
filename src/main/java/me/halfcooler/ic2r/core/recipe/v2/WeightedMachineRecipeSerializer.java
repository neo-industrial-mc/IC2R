package me.halfcooler.ic2r.core.recipe.v2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import me.halfcooler.ic2r.api.recipe.MachineRecipeWeighted;
import me.halfcooler.ic2r.api.recipe.RecipeOutputWeighted;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

public class WeightedMachineRecipeSerializer implements RecipeSerializer<RecipeHolder<IRecipeInput, Collection<ItemStack>>>
{
	private static final ResourceLocation RUNTIME_ID = ResourceLocation.fromNamespaceAndPath("ic2r", "runtime");

	private final RecipeType<?> recipeType;
	@Nullable
	private final Function<JsonObject, CompoundTag> metaProcessor;
	private final MapCodec<RecipeHolder<IRecipeInput, Collection<ItemStack>>> codec;
	private final StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<IRecipeInput, Collection<ItemStack>>> streamCodec;

	public WeightedMachineRecipeSerializer(RecipeType<?> recipeType, @Nullable Function<JsonObject, CompoundTag> metaProcessor)
	{
		this.recipeType = recipeType;
		this.metaProcessor = metaProcessor;
		this.codec = JsonRecipeCodecs.mapCodec(this::fromJsonObject);
		this.streamCodec = JsonRecipeCodecs.streamCodec(this::fromNetworkBuf, this::toNetworkBuf);
	}

	private RecipeHolder<IRecipeInput, Collection<ItemStack>> fromJsonObject(JsonObject json)
	{
		IRecipeInput input = RecipeIo.parseInput(json.get("ingredient"));
		RecipeOutputWeighted output = new RecipeOutputWeighted();
		CompoundTag meta = this.metaProcessor != null ? this.metaProcessor.apply(json) : null;
		JsonElement resultJsonObj = json.get("result");
		boolean weighted = GsonHelper.getAsBoolean(json, "weighted", false);
		MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe;
		if (weighted)
		{
			RecipeIo.parseWeightedOutputs(resultJsonObj, "result", output);
			machineRecipe = new MachineRecipeWeighted<>(input, output, meta);
		} else
		{
			machineRecipe = new MachineRecipe<>(input, RecipeIo.parseOutputs(resultJsonObj, "result"));
		}
		return new RecipeHolder<>(machineRecipe, RUNTIME_ID, this, this.recipeType);
	}

	private RecipeHolder<IRecipeInput, Collection<ItemStack>> fromNetworkBuf(RegistryFriendlyByteBuf buf)
	{
		byte type = buf.readByte();
		MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe;
		if (type == 1)
		{
			machineRecipe = new MachineRecipeWeighted<>(RecipeIo.readInput(buf), RecipeIo.readWeightedOutput(buf, new RecipeOutputWeighted()), buf.readNbt());
		} else
		{
			machineRecipe = new MachineRecipe<>(RecipeIo.readInput(buf), RecipeIo.readOutput(buf), buf.readNbt());
		}
		return new RecipeHolder<>(machineRecipe, RUNTIME_ID, this, this.recipeType);
	}

	private void toNetworkBuf(RegistryFriendlyByteBuf buf, RecipeHolder<IRecipeInput, Collection<ItemStack>> recipe)
	{
		MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe = recipe.recipe();
		if (machineRecipe instanceof MachineRecipeWeighted<?> machineRecipeWeighted)
		{
			buf.writeByte(1);
			RecipeIo.writeInput(buf, (IRecipeInput) machineRecipeWeighted.getInput());
			RecipeIo.writeWeightedOutput(buf, machineRecipeWeighted.getOutputWeighted());
		} else
		{
			buf.writeByte(0);
			RecipeIo.writeInput(buf, machineRecipe.getInput());
			RecipeIo.writeOutput(buf, machineRecipe.getOutput());
		}
		buf.writeNbt(machineRecipe.getMetaData());
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
