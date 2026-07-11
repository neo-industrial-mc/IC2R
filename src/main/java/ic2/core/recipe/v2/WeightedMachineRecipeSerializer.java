package ic2.core.recipe.v2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeWeighted;
import ic2.api.recipe.RecipeOutputWeighted;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class WeightedMachineRecipeSerializer
    implements RecipeSerializer<RecipeHolder<IRecipeInput, Collection<ItemStack>>> {
  private final RecipeType<?> recipeType;
  @Nullable private final Function<JsonObject, CompoundTag> metaProcessor;
  private final MapCodec<RecipeHolder<IRecipeInput, Collection<ItemStack>>> codec =
      new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
          return Stream.of("ingredient", "result", "weighted").map(ops::createString);
        }

        @Override
        public <T> DataResult<RecipeHolder<IRecipeInput, Collection<ItemStack>>> decode(
            DynamicOps<T> ops, MapLike<T> input) {
          try {
            JsonObject json = new JsonObject();
            input
                .entries()
                .forEach(
                    entry -> {
                      JsonElement key = ops.convertTo(JsonOps.INSTANCE, entry.getFirst());
                      json.add(
                          key.getAsString(), ops.convertTo(JsonOps.INSTANCE, entry.getSecond()));
                    });
            return DataResult.success(fromJson(json));
          } catch (RuntimeException e) {
            return DataResult.error(
                () -> "Failed to parse IC2 weighted machine recipe: " + e.getMessage());
          }
        }

        @Override
        public <T> RecordBuilder<T> encode(
            RecipeHolder<IRecipeInput, Collection<ItemStack>> recipe,
            DynamicOps<T> ops,
            RecordBuilder<T> prefix) {
          return prefix.withErrorsFrom(
              DataResult.error(() -> "IC2 weighted machine recipes cannot be encoded"));
        }
      };
  private final StreamCodec<
          RegistryFriendlyByteBuf, RecipeHolder<IRecipeInput, Collection<ItemStack>>>
      streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);

  public WeightedMachineRecipeSerializer(
      RecipeType<?> recipeType, @Nullable Function<JsonObject, CompoundTag> metaProcessor) {
    this.recipeType = recipeType;
    this.metaProcessor = metaProcessor;
  }

  private RecipeHolder<IRecipeInput, Collection<ItemStack>> fromJson(JsonObject json) {
    IRecipeInput input = RecipeIo.parseInput(json.get("ingredient"));
    RecipeOutputWeighted output = new RecipeOutputWeighted();
    CompoundTag meta = this.metaProcessor != null ? this.metaProcessor.apply(json) : null;
    JsonElement resultJsonObj = json.get("result");
    boolean weighted = GsonHelper.getAsBoolean(json, "weighted", false);
    MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe;
    if (weighted) {
      RecipeIo.parseWeightedOutputs(resultJsonObj, "result", output);
      machineRecipe = new MachineRecipeWeighted<>(input, output, meta);
    } else {
      machineRecipe = new MachineRecipe<>(input, RecipeIo.parseOutputs(resultJsonObj, "result"));
    }

    return new RecipeHolder<>(machineRecipe, null, this, this.recipeType);
  }

  private RecipeHolder<IRecipeInput, Collection<ItemStack>> fromNetwork(
      RegistryFriendlyByteBuf buf) {
    byte type = buf.readByte();
    MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe;
    if (type == 1) {
      machineRecipe =
          new MachineRecipeWeighted<>(
              RecipeIo.readInput(buf),
              RecipeIo.readWeightedOutput(buf, new RecipeOutputWeighted()),
              buf.readNbt());
    } else {
      machineRecipe =
          new MachineRecipe<>(RecipeIo.readInput(buf), RecipeIo.readOutput(buf), buf.readNbt());
    }

    return new RecipeHolder<>(machineRecipe, null, this, this.recipeType);
  }

  private void toNetwork(
      RegistryFriendlyByteBuf buf, RecipeHolder<IRecipeInput, Collection<ItemStack>> recipe) {
    MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe = recipe.recipe();
    if (machineRecipe instanceof MachineRecipeWeighted<?> machineRecipeWeighted) {
      buf.writeByte(1);
      RecipeIo.writeInput(buf, (IRecipeInput) machineRecipeWeighted.getInput());
      RecipeIo.writeWeightedOutput(buf, machineRecipeWeighted.getOutputWeighted());
    } else {
      buf.writeByte(0);
      RecipeIo.writeInput(buf, machineRecipe.getInput());
      RecipeIo.writeOutput(buf, machineRecipe.getOutput());
    }

    buf.writeNbt(machineRecipe.getMetaData());
  }

  @Override
  public MapCodec<RecipeHolder<IRecipeInput, Collection<ItemStack>>> codec() {
    return this.codec;
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<IRecipeInput, Collection<ItemStack>>>
      streamCodec() {
    return this.streamCodec;
  }
}
