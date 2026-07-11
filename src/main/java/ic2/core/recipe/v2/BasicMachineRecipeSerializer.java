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
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class BasicMachineRecipeSerializer
    implements RecipeSerializer<RecipeHolder<IRecipeInput, Collection<ItemStack>>> {
  private final RecipeType<?> recipeType;
  @Nullable private final Function<JsonObject, CompoundTag> metaProcessor;
  private final MapCodec<RecipeHolder<IRecipeInput, Collection<ItemStack>>> codec =
      new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
          return Stream.of("ingredient", "result").map(ops::createString);
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
            return DataResult.error(() -> "Failed to parse IC2 machine recipe: " + e.getMessage());
          }
        }

        @Override
        public <T> RecordBuilder<T> encode(
            RecipeHolder<IRecipeInput, Collection<ItemStack>> recipe,
            DynamicOps<T> ops,
            RecordBuilder<T> prefix) {
          return prefix.withErrorsFrom(
              DataResult.error(() -> "IC2 machine recipes cannot be encoded"));
        }
      };
  private final StreamCodec<
          RegistryFriendlyByteBuf, RecipeHolder<IRecipeInput, Collection<ItemStack>>>
      streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);

  public BasicMachineRecipeSerializer(
      RecipeType<?> recipeType, @Nullable Function<JsonObject, CompoundTag> metaProcessor) {
    this.recipeType = recipeType;
    this.metaProcessor = metaProcessor;
  }

  private RecipeHolder<IRecipeInput, Collection<ItemStack>> fromJson(JsonObject json) {
    IRecipeInput input = RecipeIo.parseInput(json.get("ingredient"));
    Collection<ItemStack> output = RecipeIo.parseOutputs(json.get("result"), "result");
    CompoundTag meta = this.metaProcessor != null ? this.metaProcessor.apply(json) : null;
    return new RecipeHolder<>(
        new MachineRecipe<>(input, output, meta), null, this, this.recipeType);
  }

  private RecipeHolder<IRecipeInput, Collection<ItemStack>> fromNetwork(
      RegistryFriendlyByteBuf buf) {
    byte type = buf.readByte();
    if (type != 0) {
      throw new Error("Reading recipe error! The type of machine recipe is wrong!");
    } else {
      return new RecipeHolder<>(
          new MachineRecipe<>(RecipeIo.readInput(buf), RecipeIo.readOutput(buf), buf.readNbt()),
          null,
          this,
          this.recipeType);
    }
  }

  private void toNetwork(
      RegistryFriendlyByteBuf buf, RecipeHolder<IRecipeInput, Collection<ItemStack>> recipe) {
    buf.writeByte(0);
    RecipeIo.writeInput(buf, recipe.recipe().getInput());
    RecipeIo.writeOutput(buf, recipe.recipe().getOutput());
    buf.writeNbt(recipe.recipe().getMetaData());
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
