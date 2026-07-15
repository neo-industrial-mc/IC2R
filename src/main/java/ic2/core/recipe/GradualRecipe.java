package ic2.core.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import ic2.core.item.reactor.AbstractDamageableReactorComponent;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.util.StackUtil;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Shapeless recipe that repairs a damageable reactor component by reducing its custom use counter
 * for each supplied charge material.
 */
public class GradualRecipe implements CraftingRecipe {
  private static final ResourceLocation FALLBACK_ID =
      ResourceLocation.fromNamespaceAndPath("ic2", "gradual_recipe");

  private final ResourceLocation id;
  private final AbstractDamageableReactorComponent item;
  private final ItemStack chargeMaterial;
  private final int amount;
  private final boolean hidden;

  public GradualRecipe(
      ResourceLocation id,
      AbstractDamageableReactorComponent item,
      ItemStack chargeMaterial,
      int amount,
      boolean hidden) {
    this.id = id;
    this.item = item;
    this.chargeMaterial = chargeMaterial;
    this.amount = amount;
    this.hidden = hidden;
  }

  @Override
  public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
    return !this.assemble(input).isEmpty();
  }

  public @NotNull ItemStack assemble(@NotNull CraftingInput input) {
    ItemStack gridItem = ItemStack.EMPTY;
    int chargeMaterials = 0;

    for (int slot = 0; slot < input.size(); slot++) {
      ItemStack stack = input.getItem(slot);
      if (StackUtil.isEmpty(stack)) {
        continue;
      }

      if (StackUtil.isEmpty(gridItem) && stack.getItem() == this.item) {
        gridItem = stack;
      } else if (StackUtil.checkItemEquality(stack, this.chargeMaterial)) {
        chargeMaterials++;
      } else {
        return ItemStack.EMPTY;
      }
    }

    if (StackUtil.isEmpty(gridItem) || chargeMaterials <= 0) {
      return ItemStack.EMPTY;
    }

    int currentUse = this.item.getUse(gridItem);
    if (currentUse <= 0) {
      return ItemStack.EMPTY;
    }

    ItemStack result = gridItem.copy();
    result.setCount(1);
    int use = currentUse - this.amount * chargeMaterials;
    if (use > this.item.getMaxUse()) {
      use = this.item.getMaxUse();
    } else if (use < 0) {
      use = 0;
    }

    this.item.setUse(result, use);
    return result;
  }

  @Override
  public @NotNull ItemStack assemble(
      @NotNull CraftingInput input, HolderLookup.Provider registryAccess) {
    return this.assemble(input);
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width * height >= 2;
  }

  @Override
  public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registryAccess) {
    return new ItemStack(this.item);
  }

  @Override
  public @NotNull NonNullList<Ingredient> getIngredients() {
    NonNullList<Ingredient> ingredients = NonNullList.create();
    if (!this.hidden) {
      ingredients.add(Ingredient.of(this.item));
      ingredients.add(Ingredient.of(this.chargeMaterial));
    }

    return ingredients;
  }

  @Override
  public boolean isSpecial() {
    return true;
  }

  public ResourceLocation getId() {
    return this.id;
  }

  @Override
  public @NotNull RecipeSerializer<?> getSerializer() {
    return Ic2RecipeSerializers.GRADUAL;
  }

  @Override
  public @NotNull CraftingBookCategory category() {
    return CraftingBookCategory.MISC;
  }

  public static class Serializer implements RecipeSerializer<GradualRecipe> {
    private static final MapCodec<GradualRecipe> CODEC =
        new MapCodec<>() {
          @Override
          public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of("item", "charge_material", "amount", "hidden").map(ops::createString);
          }

          @Override
          public <T> DataResult<GradualRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
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
                  () -> "Failed to parse IC2 gradual recipe: " + e.getMessage());
            }
          }

          @Override
          public <T> RecordBuilder<T> encode(
              GradualRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return prefix.withErrorsFrom(
                DataResult.error(() -> "IC2 gradual recipes cannot be encoded"));
          }
        };

    private static final StreamCodec<RegistryFriendlyByteBuf, GradualRecipe> STREAM_CODEC =
        StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

    @Override
    public MapCodec<GradualRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, GradualRecipe> streamCodec() {
      return STREAM_CODEC;
    }

    private static GradualRecipe fromJson(JsonObject json) {
      Item item = GsonHelper.getAsItem(json, "item").value();
      if (!(item instanceof AbstractDamageableReactorComponent component)) {
        throw new IllegalArgumentException(
            "Gradual recipe item must be a damageable reactor component: " + item);
      }

      ItemStack chargeMaterial =
          parseItemStack(GsonHelper.getAsJsonObject(json, "charge_material"));
      int amount = GsonHelper.getAsInt(json, "amount");
      boolean hidden = GsonHelper.getAsBoolean(json, "hidden", false);
      return new GradualRecipe(FALLBACK_ID, component, chargeMaterial, amount, hidden);
    }

    private static GradualRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
      Item item = BuiltInRegistries.ITEM.byId(buffer.readVarInt());
      if (!(item instanceof AbstractDamageableReactorComponent component)) {
        throw new IllegalStateException(
            "Gradual recipe item is not a damageable reactor component: " + item);
      }

      ItemStack chargeMaterial = ItemStack.STREAM_CODEC.decode(buffer);
      int amount = buffer.readVarInt();
      boolean hidden = buffer.readBoolean();
      return new GradualRecipe(FALLBACK_ID, component, chargeMaterial, amount, hidden);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, GradualRecipe recipe) {
      buffer.writeVarInt(BuiltInRegistries.ITEM.getId(recipe.item));
      ItemStack.STREAM_CODEC.encode(buffer, recipe.chargeMaterial);
      buffer.writeVarInt(recipe.amount);
      buffer.writeBoolean(recipe.hidden);
    }

    private static ItemStack parseItemStack(JsonObject json) {
      Item item = GsonHelper.getAsItem(json, "item").value();
      int count = GsonHelper.getAsInt(json, "count", 1);
      return new ItemStack(item, count);
    }
  }
}
