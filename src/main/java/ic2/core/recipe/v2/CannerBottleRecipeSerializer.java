package ic2.core.recipe.v2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.ref.Ic2RecipeTypes;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CannerBottleRecipeSerializer
    implements RecipeSerializer<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> {
  private final MapCodec<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> codec =
      new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
          return Stream.of("container_ingredient", "fill_ingredient", "result")
              .map(ops::createString);
        }

        @Override
        public <T> DataResult<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> decode(
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
                () -> "Failed to parse IC2 canner bottle recipe: " + e.getMessage());
          }
        }

        @Override
        public <T> RecordBuilder<T> encode(
            RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> recipe,
            DynamicOps<T> ops,
            RecordBuilder<T> prefix) {
          return prefix.withErrorsFrom(
              DataResult.error(() -> "IC2 canner bottle recipes cannot be encoded"));
        }
      };
  private final StreamCodec<
          RegistryFriendlyByteBuf, RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>>
      streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);

  private RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> fromJson(JsonObject json) {
    IRecipeInput container = RecipeIo.parseInput(json.get("container_ingredient"));
    IRecipeInput fill = RecipeIo.parseInput(json.get("fill_ingredient"));
    ItemStack output = RecipeIo.parseOutput(GsonHelper.getAsJsonObject(json, "result"));
    return new RecipeHolder<>(
        new MachineRecipe<>(new ICannerBottleRecipeManager.Input(container, fill), output),
        null,
        this,
        Ic2RecipeTypes.CANNER_BOTTLE);
  }

  private RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> fromNetwork(
      RegistryFriendlyByteBuf buf) {
    IRecipeInput container = RecipeIo.readInput(buf);
    IRecipeInput fill = RecipeIo.readInput(buf);
    ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
    return new RecipeHolder<>(
        new MachineRecipe<>(new ICannerBottleRecipeManager.Input(container, fill), output),
        null,
        this,
        Ic2RecipeTypes.CANNER_BOTTLE);
  }

  private void toNetwork(
      RegistryFriendlyByteBuf buf,
      RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> recipe) {
    RecipeIo.writeInput(buf, recipe.recipe().getInput().container());
    RecipeIo.writeInput(buf, recipe.recipe().getInput().fill());
    ItemStack.STREAM_CODEC.encode(buf, recipe.recipe().getOutput());
  }

  @Override
  public MapCodec<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> codec() {
    return this.codec;
  }

  @Override
  public StreamCodec<
          RegistryFriendlyByteBuf, RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>>
      streamCodec() {
    return this.streamCodec;
  }
}
