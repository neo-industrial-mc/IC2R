package ic2.core.recipe;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import ic2.api.item.ElectricItem;
import ic2.api.recipe.IRecipeInput;
import ic2.compat.Ic2CraftingRecipe;
import ic2.core.init.IC2ClientConfig;
import ic2.core.recipe.input.RecipeInputFluidContainer;
import ic2.core.recipe.input.RecipeInputIngredient;
import ic2.core.recipe.input.RecipeInputItemStack;
import ic2.core.recipe.input.RecipeInputMultiple;
import ic2.core.recipe.v2.RecipeIo;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AdvRecipe implements Ic2CraftingRecipe {
  private static final boolean debug = Util.hasAssertions();
  public final ItemStack output;
  public final IRecipeInput[] input;
  public final IRecipeInput[] inputMirrored;
  public final int[] masks;
  public final int[] masksMirrored;
  public final int inputWidth;
  public final int inputHeight;
  public final boolean hidden;
  public final boolean consuming;

  /**
   * Recipes are no longer id-aware in 1.21 (they are keyed externally through {@code
   * RecipeHolder}), so this is always null now.
   */
  private final ResourceLocation id;

  private AdvRecipe(
      int width,
      int height,
      boolean mirror,
      int mask,
      IRecipeInput[] input,
      ItemStack result,
      boolean isConsuming,
      boolean isHidden) {
    this.id = null;
    this.inputWidth = width;
    this.inputHeight = height;
    this.output = result;
    this.consuming = isConsuming;
    this.hidden = isHidden;
    this.input = input;
    if (!mirror) {
      this.inputMirrored = null;
    } else {
      IRecipeInput[] tmp = new IRecipeInput[9];
      int i = 0;
      int j = 0;

      while (i < 9) {
        if ((mask & 1 << 8 - i) != 0) {
          tmp[i] = input[j];
          j++;
        }

        i++;
      }

      IRecipeInput old = tmp[0];
      tmp[0] = tmp[2];
      tmp[2] = old;
      IRecipeInput var18 = tmp[3];
      tmp[3] = tmp[5];
      tmp[5] = var18;
      IRecipeInput var19 = tmp[6];
      tmp[6] = tmp[8];
      tmp[8] = var19;
      this.inputMirrored = new IRecipeInput[input.length];
      j = 0;
      int jx = 0;

      while (j < 9) {
        if (tmp[j] != null) {
          this.inputMirrored[jx] = tmp[j];
          jx++;
        }

        j++;
      }
    }

    int xMasks = -width + 4;
    int yMasks = -height + 4;
    this.masks = new int[xMasks * yMasks];
    if (!mirror) {
      this.masksMirrored = null;
    } else {
      this.masksMirrored = new int[this.masks.length];
    }

    for (int y = 0; y < yMasks; y++) {
      int yMask = mask >>> y * 3;

      for (int x = 0; x < xMasks; x++) {
        int xyMask = yMask >>> x;
        this.masks[x + y * xMasks] = xyMask;
        if (mirror) {
          this.masksMirrored[x + y * xMasks] = xyMask << 2 & 292 | xyMask & 146 | xyMask >>> 2 & 73;
        }
      }
    }
  }

  private static AdvRecipe create(
      int width,
      int height,
      IRecipeInput[] ingredients,
      ItemStack result,
      boolean isConsuming,
      boolean isHidden) {
    int mask = 0;
    List<IRecipeInput> inputs = new ArrayList<>();

    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 3; x++) {
        mask <<= 1;
        if (x < width && y < height && ingredients[x + y * width] != null) {
          mask |= 1;
          inputs.add(ingredients[x + y * width]);
        }
      }
    }

    IRecipeInput[] input = inputs.toArray(new IRecipeInput[0]);
    boolean mirror = false;
    if (width != 1) {
      for (int y = 0; y < height; y++) {
        if (ingredients[y * width] != ingredients[width - 1 + y * width]) {
          mirror = true;
          break;
        }
      }
    }

    return new AdvRecipe(width, height, mirror, mask, input, result, isConsuming, isHidden);
  }

  public static boolean canShow(Object[] input, ItemStack output, boolean hidden) {
    return !hidden || !IC2ClientConfig.misc.hideSecretRecipes.get();
  }

  private static boolean checkMask(int mask, int[] request) {
    for (int cmpMask : request) {
      if (mask == cmpMask) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean matches(@NotNull CraftingInput input, @NotNull Level world) {
    return this.assemble(input) != StackUtil.emptyStack;
  }

  public ItemStack assemble(CraftingInput input) {
    int width = input.width();
    int height = input.height();
    if (width > 3 || height > 3) {
      return StackUtil.emptyStack;
    }

    // Build the 3x3 occupancy mask. CraftingInput is trimmed to the bounding box of the placed
    // items, so re-expand it with the 3-slots-per-row stride the precomputed masks use.
    int mask = 0;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (!StackUtil.isEmpty(input.getItem(x + y * width))) {
          mask |= 1 << 8 - (x + y * 3);
        }
      }
    }

    if (checkMask(mask, this.masks)) {
      ItemStack ret = this.checkItems(input, this.input);
      if (!StackUtil.isEmpty(ret)) {
        return ret;
      }
    }

    if (this.masksMirrored != null && checkMask(mask, this.masksMirrored)) {
      ItemStack ret = this.checkItems(input, this.inputMirrored);
      if (!StackUtil.isEmpty(ret)) {
        return ret;
      }
    }

    return StackUtil.emptyStack;
  }

  public ItemStack getResultItem() {
    return this.output;
  }

  @Override
  public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
    return this.output;
  }

  public boolean canShow() {
    return canShow(this.input, this.output, this.hidden);
  }

  private ItemStack checkItems(CraftingInput inventory, IRecipeInput[] request) {
    int size = inventory.size();
    double outputCharge = 0.0;
    int i = 0;
    int j = 0;

    while (i < size) {
      ItemStack offer = inventory.getItem(i);
      if (!StackUtil.isEmpty(offer)) {
        if (!request[j++].matches(offer)) {
          return StackUtil.emptyStack;
        }

        outputCharge += ElectricItem.manager.getCharge(StackUtil.copyWithSize(offer, 1));
      }

      i++;
    }

    ItemStack ret = this.output.copy();
    ElectricItem.manager.charge(ret, outputCharge, Integer.MAX_VALUE, true, false);
    return ret;
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
    return this.consuming
        ? NonNullList.withSize(input.size(), StackUtil.emptyStack)
        : Ic2CraftingRecipe.super.getRemainingItems(input);
  }

  @Override
  public boolean canCraftInDimensions(int x, int y) {
    return this.inputWidth <= x && this.inputHeight <= y;
  }

  @Override
  public int getIc2RecipeWidth() {
    return this.inputWidth;
  }

  @Override
  public int getIc2RecipeHeight() {
    return this.inputHeight;
  }

  @Override
  public @NotNull NonNullList<Ingredient> getIngredients() {
    NonNullList<Ingredient> list = NonNullList.create();
    if (!this.hidden) {
      int mask = this.masks[0];
      int actualIngredient = 0;

      for (int y = 0; y < this.inputHeight; y++) {
        for (int x = 0; x < this.inputWidth; x++) {
          if ((mask >>> 8 - (x + y * 3) & 1) != 0) {
            list.add(this.input[actualIngredient++].getIngredient());
          } else {
            list.add(Ingredient.EMPTY);
          }
        }
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
  public @NotNull ItemStack assemble(
      @NotNull CraftingInput inventory, @NotNull HolderLookup.Provider registries) {
    return this.assemble(inventory);
  }

  @Override
  public @NotNull CraftingBookCategory category() {
    return CraftingBookCategory.MISC;
  }

  @Override
  public @NotNull RecipeSerializer<?> getSerializer() {
    return Ic2RecipeSerializers.SHAPED;
  }

  public static final class Serializer implements RecipeSerializer<AdvRecipe> {
    /**
     * Decodes the historic IC2 shaped recipe JSON format (pattern/key/result plus the optional
     * consuming/hidden flags) by rebuilding the map as a {@link JsonObject} and reusing the
     * pre-1.21 parsing code. Encoding back to JSON is not supported (it never was).
     */
    private static final MapCodec<AdvRecipe> CODEC =
        new MapCodec<>() {
          @Override
          public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of("pattern", "key", "result", "consuming", "hidden")
                .map(ops::createString);
          }

          @Override
          public <T> DataResult<AdvRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
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
              return DataResult.error(() -> "Failed to parse IC2 shaped recipe: " + e.getMessage());
            }
          }

          @Override
          public <T> RecordBuilder<T> encode(
              AdvRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return prefix.withErrorsFrom(
                DataResult.error(() -> "IC2 shaped recipes cannot be encoded"));
          }
        };

    private static final StreamCodec<RegistryFriendlyByteBuf, AdvRecipe> STREAM_CODEC =
        StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

    private static Map<String, IRecipeInput> readSymbols(JsonObject json) {
      HashMap<String, IRecipeInput> map = Maps.newHashMap();

      for (Entry<String, JsonElement> entry : json.entrySet()) {
        if (entry.getKey().length() != 1) {
          throw new JsonSyntaxException(
              "Invalid key entry: '"
                  + entry.getKey()
                  + "' is an invalid symbol (must be 1 character only).");
        }

        if (" ".equals(entry.getKey())) {
          throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
        }

        map.put(entry.getKey(), RecipeIo.parseInput(entry.getValue()));
      }

      return map;
    }

    private static String[] getPattern(JsonArray json) {
      String[] strings = new String[json.size()];
      if (strings.length > 3) {
        throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
      }

      if (strings.length == 0) {
        throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
      }

      for (int i = 0; i < strings.length; i++) {
        String string = GsonHelper.convertToString(json.get(i), "pattern[" + i + "]");
        if (string.length() > 3) {
          throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
        }

        if (i > 0 && strings[0].length() != string.length()) {
          throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
        }

        strings[i] = string;
      }

      return strings;
    }

    private static IRecipeInput[] createPatternMatrix(
        String[] pattern, Map<String, IRecipeInput> symbols, int width, int height) {
      IRecipeInput[] ingredients = new IRecipeInput[width * height];
      HashSet<String> remainingKeys = new HashSet<>(symbols.keySet());

      for (int i = 0; i < pattern.length; i++) {
        for (int j = 0; j < pattern[i].length(); j++) {
          String string = pattern[i].substring(j, j + 1);
          if (!string.equals(" ")) {
            IRecipeInput ingredient = symbols.get(string);
            if (ingredient == null) {
              throw new JsonSyntaxException(
                  "Pattern references symbol '" + string + "' but it's not defined in the key");
            }

            remainingKeys.remove(string);
            ingredients[j + width * i] = ingredient;
          }
        }
      }

      if (!remainingKeys.isEmpty()) {
        throw new JsonSyntaxException(
            "Key defines symbols that aren't used in pattern: " + remainingKeys);
      } else {
        return ingredients;
      }
    }

    private static AdvRecipe fromJson(@NotNull JsonObject json) {
      Map<String, IRecipeInput> symbols = readSymbols(GsonHelper.getAsJsonObject(json, "key"));
      String[] pattern = getPattern(GsonHelper.getAsJsonArray(json, "pattern"));
      int width = pattern[0].length();
      int height = pattern.length;
      IRecipeInput[] ingredients = createPatternMatrix(pattern, symbols, width, height);
      ItemStack result = RecipeIo.parseOutput(GsonHelper.getAsJsonObject(json, "result"));
      boolean consuming = GsonHelper.getAsBoolean(json, "consuming", false);
      boolean hidden = GsonHelper.getAsBoolean(json, "hidden", false);
      return AdvRecipe.create(width, height, ingredients, result, consuming, hidden);
    }

    private static AdvRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
      IRecipeInput[] ingredients = new IRecipeInput[buf.readVarInt()];

      for (int i = 0; i < ingredients.length; i++) {
        ingredients[i] = readRecipeInput(buf);
      }

      int width = buf.readVarInt();
      int height = buf.readVarInt();
      boolean mirror = buf.readBoolean();
      int mask = buf.readInt();
      ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
      boolean consuming = buf.readBoolean();
      boolean hidden = buf.readBoolean();
      return new AdvRecipe(width, height, mirror, mask, ingredients, result, consuming, hidden);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, AdvRecipe recipe) {
      buf.writeVarInt(recipe.input.length);

      for (IRecipeInput ing : recipe.input) {
        writeRecipeInput(buf, ing);
      }

      buf.writeVarInt(recipe.inputWidth);
      buf.writeVarInt(recipe.inputHeight);
      buf.writeBoolean(recipe.inputMirrored != null);
      buf.writeInt(recipe.masks[0]);
      ItemStack.STREAM_CODEC.encode(buf, recipe.output);
      buf.writeBoolean(recipe.consuming);
      buf.writeBoolean(recipe.hidden);
    }

    // The IRecipeInput wire format below mirrors RecipeIo.writeInput/readInput, rewritten
    // against the 1.21 stream codecs (registry-aware buffers).
    private static void writeRecipeInput(RegistryFriendlyByteBuf buf, IRecipeInput input) {
      if (input instanceof RecipeInputFluidContainer fluidContainer) {
        buf.writeByte(0);
        buf.writeVarInt(BuiltInRegistries.FLUID.getId(fluidContainer.fluid));
        buf.writeVarInt(fluidContainer.amount);
      } else if (input instanceof RecipeInputIngredient ingredient) {
        buf.writeByte(1);
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient.getIngredient());
        buf.writeVarInt(ingredient.getAmount());
      } else if (input instanceof RecipeInputItemStack stack) {
        buf.writeByte(2);
        ItemStack.STREAM_CODEC.encode(buf, stack.input);
      } else {
        if (!(input instanceof RecipeInputMultiple mult)) {
          throw new IllegalArgumentException(
              "Unkown RecipeInput type: " + input.getClass().getName());
        }

        buf.writeByte(3);
        buf.writeVarInt(mult.inputs.length);

        for (IRecipeInput i : mult.inputs) {
          writeRecipeInput(buf, i);
        }

        buf.writeVarInt(mult.getAmount());
      }
    }

    private static IRecipeInput readRecipeInput(RegistryFriendlyByteBuf buf) {
      return switch (buf.readByte()) {
        case 0 ->
            new RecipeInputFluidContainer(
                BuiltInRegistries.FLUID.byId(buf.readVarInt()), buf.readVarInt());
        case 1 ->
            new RecipeInputIngredient(
                Ingredient.CONTENTS_STREAM_CODEC.decode(buf), buf.readVarInt());
        case 2 -> new RecipeInputItemStack(ItemStack.STREAM_CODEC.decode(buf));
        case 3 -> {
          IRecipeInput[] inputs = new IRecipeInput[buf.readVarInt()];

          for (int i = 0; i < inputs.length; i++) {
            inputs[i] = readRecipeInput(buf);
          }

          yield new RecipeInputMultiple(buf.readVarInt(), inputs);
        }
        default -> throw new IllegalArgumentException("Unkown RecipeInput type.");
      };
    }

    @Override
    public @NotNull MapCodec<AdvRecipe> codec() {
      return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, AdvRecipe> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
