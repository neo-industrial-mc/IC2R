package ic2.core.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import ic2.api.item.ElectricItem;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import ic2.core.item.tool.ItemToolCrafting;
import ic2.core.recipe.v2.RecipeIo;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AdvShapelessRecipe implements CraftingRecipe {
  public final ItemStack output;
  public final IRecipeInput[] input;
  public final boolean hidden;
  public final boolean consuming;
  private final ResourceLocation id;

  public AdvShapelessRecipe(
      ResourceLocation id,
      IRecipeInput[] input,
      ItemStack output,
      boolean hidden,
      boolean consuming) {
    this.id = id;
    this.input = input;
    this.output = output;
    this.hidden = hidden;
    this.consuming = consuming;
  }

  @Override
  public boolean matches(CraftingInput inventorycrafting, Level world) {
    return this.assemble(inventorycrafting) != StackUtil.emptyStack;
  }

  public ItemStack assemble(CraftingInput inventorycrafting) {
    int offerSize = inventorycrafting.size();
    if (offerSize < this.input.length) {
      return StackUtil.emptyStack;
    }

    List<IRecipeInput> unmatched = new ArrayList<>(Arrays.asList(this.input));
    double outputCharge = 0.0;

    label36:
    for (int i = 0; i < offerSize; i++) {
      ItemStack offer = inventorycrafting.getItem(i);
      if (!StackUtil.isEmpty(offer)) {
        for (int j = 0; j < unmatched.size(); j++) {
          if (unmatched.get(j).matches(offer)) {
            outputCharge += ElectricItem.manager.getCharge(StackUtil.copyWithSize(offer, 1));
            unmatched.remove(j);
            continue label36;
          }
        }

        return StackUtil.emptyStack;
      }
    }

    if (!unmatched.isEmpty()) {
      return StackUtil.emptyStack;
    }

    ItemStack ret = this.output.copy();
    ElectricItem.manager.charge(ret, outputCharge, Integer.MAX_VALUE, true, false);
    return ret;
  }

  public ItemStack getResultItem() {
    return this.output;
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return this.output;
  }

  public boolean canShow() {
    return AdvRecipe.canShow(this.input, this.output, this.hidden);
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
    if (this.consuming) {
      return NonNullList.withSize(inv.size(), StackUtil.emptyStack);
    }

    NonNullList<ItemStack> defaultedList = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

    for (int i = 0; i < defaultedList.size(); i++) {
      ItemStack stack = inv.getItem(i);
      ItemStack remainder = IC2.envProxy.getRecipeRemainder(stack);
      if (stack.getItem() instanceof ItemToolCrafting) {
        remainder = stack.copy();
        remainder.setDamageValue(remainder.getDamageValue() + 1);
        if (remainder.getDamageValue() == remainder.getMaxDamage()) {
          remainder = ItemStack.EMPTY;
        }
      }

      if (!remainder.isEmpty()) {
        defaultedList.set(i, remainder);
      }
    }

    return defaultedList;
  }

  @Override
  public boolean canCraftInDimensions(int x, int y) {
    return x * y >= this.input.length;
  }

  @Override
  public NonNullList<Ingredient> getIngredients() {
    NonNullList<Ingredient> list = NonNullList.create();
    if (!this.hidden) {
      for (IRecipeInput input : this.input) {
        list.add(input.getIngredient());
      }
    }

    return list;
  }

  @Override
  public boolean isSpecial() {
    return this.hidden;
  }

  public ResourceLocation getId() {
    return this.id;
  }

  @Override
  public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider registries) {
    return this.assemble(inventory);
  }

  @Override
  public net.minecraft.world.item.crafting.CraftingBookCategory category() {
    return net.minecraft.world.item.crafting.CraftingBookCategory.MISC;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Ic2RecipeSerializers.SHAPELESS;
  }

  public static class Serializer implements RecipeSerializer<AdvShapelessRecipe> {
    private final MapCodec<AdvShapelessRecipe> codec =
        new MapCodec<>() {
          @Override
          public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of("ingredients", "result", "consuming", "hidden").map(ops::createString);
          }

          @Override
          public <T> DataResult<AdvShapelessRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
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
                  () -> "Failed to parse IC2 shapeless recipe: " + e.getMessage());
            }
          }

          @Override
          public <T> RecordBuilder<T> encode(
              AdvShapelessRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return prefix.withErrorsFrom(
                DataResult.error(() -> "IC2 shapeless recipes cannot be encoded"));
          }
        };
    private final StreamCodec<RegistryFriendlyByteBuf, AdvShapelessRecipe> streamCodec =
        StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

    private static IRecipeInput[] getIngredients(JsonArray json) {
      IRecipeInput[] inputs = new IRecipeInput[json.size()];

      for (int i = 0; i < json.size(); i++) {
        inputs[i] = RecipeIo.parseInput(json.get(i));
      }

      return inputs;
    }

    private static AdvShapelessRecipe fromJson(JsonObject json) {
      IRecipeInput[] ingredients = getIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
      if (ingredients.length == 0) {
        throw new JsonParseException("No ingredients for IC2 shapeless recipe");
      }

      if (ingredients.length > 9) {
        throw new JsonParseException("Too many ingredients for IC2 shapeless recipe");
      }

      ItemStack result = RecipeIo.parseOutput(GsonHelper.getAsJsonObject(json, "result"));
      boolean consuming = GsonHelper.getAsBoolean(json, "consuming", false);
      boolean hidden = GsonHelper.getAsBoolean(json, "hidden", false);
      return new AdvShapelessRecipe(null, ingredients, result, hidden, consuming);
    }

    private static AdvShapelessRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
      IRecipeInput[] inputs = new IRecipeInput[buf.readVarInt()];

      for (int i = 0; i < inputs.length; i++) {
        inputs[i] = RecipeIo.readInput(buf);
      }

      return new AdvShapelessRecipe(
          null, inputs, ItemStack.STREAM_CODEC.decode(buf), buf.readBoolean(), buf.readBoolean());
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, AdvShapelessRecipe recipe) {
      buf.writeVarInt(recipe.input.length);

      for (IRecipeInput input : recipe.input) {
        RecipeIo.writeInput(buf, input);
      }

      ItemStack.STREAM_CODEC.encode(buf, recipe.output);
      buf.writeBoolean(recipe.hidden);
      buf.writeBoolean(recipe.consuming);
    }

    @Override
    public MapCodec<AdvShapelessRecipe> codec() {
      return this.codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AdvShapelessRecipe> streamCodec() {
      return this.streamCodec;
    }
  }
}
