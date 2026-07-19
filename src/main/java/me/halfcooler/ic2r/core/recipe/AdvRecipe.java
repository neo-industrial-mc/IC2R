package me.halfcooler.ic2r.core.recipe;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.compat.Ic2rCraftingRecipe;
import me.halfcooler.ic2r.core.init.IC2RClientConfig;
import me.halfcooler.ic2r.core.recipe.v2.JsonRecipeCodecs;
import me.halfcooler.ic2r.core.recipe.v2.RecipeIo;
import me.halfcooler.ic2r.core.ref.Ic2rRecipeSerializers;

import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
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

public class AdvRecipe implements Ic2rCraftingRecipe
{
	private static final ResourceLocation RUNTIME_ID = ResourceLocation.fromNamespaceAndPath("ic2r", "shaped");

	public final ItemStack output;
	public final IRecipeInput[] input;
	public final IRecipeInput[] inputMirrored;
	public final int[] masks;
	public final int[] masksMirrored;
	public final int inputWidth;
	public final int inputHeight;
	public final boolean hidden;
	public final boolean consuming;
	/** Internal id only; vanilla wraps recipes in RecipeHolder. */
	private final ResourceLocation id;

	private AdvRecipe(ResourceLocation id, int width, int height, boolean mirror, int mask, IRecipeInput[] input, ItemStack result, boolean isConsuming, boolean isHidden)
	{
		this.id = id;
		this.inputWidth = width;
		this.inputHeight = height;
		this.output = result;
		this.consuming = isConsuming;
		this.hidden = isHidden;
		this.input = input;
		if (!mirror)
		{
			this.inputMirrored = null;
		} else
		{
			IRecipeInput[] tmp = new IRecipeInput[9];
			int i = 0;
			int j = 0;

			while (i < 9)
			{
				if ((mask & 1 << 8 - i) != 0)
				{
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

			while (j < 9)
			{
				if (tmp[j] != null)
				{
					this.inputMirrored[jx] = tmp[j];
					jx++;
				}

				j++;
			}
		}

		int xMasks = -width + 4;
		int yMasks = -height + 4;
		this.masks = new int[xMasks * yMasks];
		if (!mirror)
		{
			this.masksMirrored = null;
		} else
		{
			this.masksMirrored = new int[this.masks.length];
		}

		for (int y = 0; y < yMasks; y++)
		{
			int yMask = mask >>> y * 3;

			for (int x = 0; x < xMasks; x++)
			{
				int xyMask = yMask >>> x;
				this.masks[x + y * xMasks] = xyMask;
				if (mirror)
				{
					this.masksMirrored[x + y * xMasks] = xyMask << 2 & 292 | xyMask & 146 | xyMask >>> 2 & 73;
				}
			}
		}
	}

	private static AdvRecipe create(ResourceLocation id, int width, int height, IRecipeInput[] ingredients, ItemStack result, boolean isConsuming, boolean isHidden)
	{
		int mask = 0;
		List<IRecipeInput> inputs = new ArrayList<>();

		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				mask <<= 1;
				if (x < width && y < height && ingredients[x + y * width] != null)
				{
					mask |= 1;
					inputs.add(ingredients[x + y * width]);
				}
			}
		}

		IRecipeInput[] input = inputs.toArray(new IRecipeInput[0]);
		boolean mirror = false;
		if (width != 1)
		{
			for (int y = 0; y < height; y++)
			{
				if (ingredients[y * width] != ingredients[width - 1 + y * width])
				{
					mirror = true;
					break;
				}
			}
		}

		return new AdvRecipe(id, width, height, mirror, mask, input, result, isConsuming, isHidden);
	}

	public static boolean canShow(Object[] input, ItemStack output, boolean hidden)
	{
		return !hidden || !IC2RClientConfig.misc.hideSecretRecipes.get();
	}

	private static boolean checkMask(int mask, int[] request)
	{
		for (int cmpMask : request)
		{
			if (mask == cmpMask)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean matches(@NotNull CraftingInput inventoryCrafting, @NotNull Level world)
	{
		return this.assemble(inventoryCrafting) != StackUtil.emptyStack;
	}

	public ItemStack assemble(CraftingInput inventoryCrafting)
	{
		int size = inventoryCrafting.size();
		int mask = 0;

		for (int i = 0; i < size; i++)
		{
			mask <<= 1;
			if (!StackUtil.isEmpty(inventoryCrafting.getItem(i)))
			{
				mask |= 1;
			}
		}

		if (size == 4)
		{
			mask = (mask & 12) << 5 | (mask & 3) << 4;
		}

		if (checkMask(mask, this.masks))
		{
			ItemStack ret = this.checkItems(inventoryCrafting, this.input);
			if (!StackUtil.isEmpty(ret))
			{
				return ret;
			}
		}

		if (this.masksMirrored != null && checkMask(mask, this.masksMirrored))
		{
			ItemStack ret = this.checkItems(inventoryCrafting, this.inputMirrored);
			if (!StackUtil.isEmpty(ret))
			{
				return ret;
			}
		}

		return StackUtil.emptyStack;
	}

	public ItemStack getResultItem()
	{
		return this.output;
	}

	@Override
	public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registryAccess)
	{
		return this.output;
	}

	public boolean canShow()
	{
		return canShow(this.input, this.output, this.hidden);
	}

	private ItemStack checkItems(CraftingInput inventory, IRecipeInput[] request)
	{
		int size = inventory.size();
		double outputCharge = 0.0;
		int i = 0;
		int j = 0;

		while (i < size)
		{
			ItemStack offer = inventory.getItem(i);
			if (!StackUtil.isEmpty(offer))
			{
				if (!request[j++].matches(offer))
				{
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
	public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput inv)
	{
		return this.consuming ? NonNullList.withSize(inv.size(), StackUtil.emptyStack) : Ic2rCraftingRecipe.super.getRemainingItems(inv);
	}

	@Override
	public boolean canCraftInDimensions(int x, int y)
	{
		return this.inputWidth <= x && this.inputHeight <= y;
	}

	@Override
	public int getIc2rRecipeWidth()
	{
		return this.inputWidth;
	}

	@Override
	public int getIc2rRecipeHeight()
	{
		return this.inputHeight;
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients()
	{
		return this.hidden ? NonNullList.create() : this.getDisplayIngredients();
	}

	/** Returns the complete shaped input layout for external recipe viewers such as JEI. */
	public @NotNull NonNullList<Ingredient> getDisplayIngredients()
	{
		NonNullList<Ingredient> list = NonNullList.create();
		int mask = this.masks[0];
		int actualIngredient = 0;

		for (int y = 0; y < this.inputHeight; y++)
		{
			for (int x = 0; x < this.inputWidth; x++)
			{
				if ((mask >>> 8 - (x + y * 3) & 1) != 0)
				{
					list.add(this.input[actualIngredient++].getIngredient());
				} else
				{
					list.add(Ingredient.EMPTY);
				}
			}
		}

		return list;
	}

	@Override
	public boolean isSpecial()
	{
		return this.hidden;
	}

	public @NotNull ResourceLocation getId()
	{
		return this.id;
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull CraftingInput inventory, @NotNull HolderLookup.Provider registryAccess)
	{
		return this.assemble(inventory);
	}

	@Override
	public @NotNull CraftingBookCategory category()
	{
		return CraftingBookCategory.MISC;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer()
	{
		return Ic2rRecipeSerializers.SHAPED;
	}

	public static final class Serializer implements RecipeSerializer<AdvRecipe>
	{
		private final MapCodec<AdvRecipe> codec = JsonRecipeCodecs.mapCodec(this::fromJsonObject);
		private final StreamCodec<RegistryFriendlyByteBuf, AdvRecipe> streamCodec =
			JsonRecipeCodecs.streamCodec(this::fromNetworkBuf, this::toNetworkBuf);

		private static Map<String, IRecipeInput> readSymbols(JsonObject json)
		{
			HashMap<String, IRecipeInput> map = Maps.newHashMap();

			for (Entry<String, JsonElement> entry : json.entrySet())
			{
				if (entry.getKey().length() != 1)
				{
					throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
				}

				if (" ".equals(entry.getKey()))
				{
					throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
				}

				map.put(entry.getKey(), RecipeIo.parseInput(entry.getValue()));
			}

			return map;
		}

		private static String[] getPattern(JsonArray json)
		{
			String[] strings = new String[json.size()];
			if (strings.length > 3)
			{
				throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
			}

			if (strings.length == 0)
			{
				throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
			}

			for (int i = 0; i < strings.length; i++)
			{
				String string = GsonHelper.convertToString(json.get(i), "pattern[" + i + "]");
				if (string.length() > 3)
				{
					throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
				}

				if (i > 0 && strings[0].length() != string.length())
				{
					throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
				}

				strings[i] = string;
			}

			return strings;
		}

		private static IRecipeInput[] createPatternMatrix(String[] pattern, Map<String, IRecipeInput> symbols, int width, int height)
		{
			IRecipeInput[] ingredients = new IRecipeInput[width * height];
			HashSet<String> remainingKeys = new HashSet<>(symbols.keySet());

			for (int i = 0; i < pattern.length; i++)
			{
				for (int j = 0; j < pattern[i].length(); j++)
				{
					String string = pattern[i].substring(j, j + 1);
					if (!string.equals(" "))
					{
						IRecipeInput ingredient = symbols.get(string);
						if (ingredient == null)
						{
							throw new JsonSyntaxException("Pattern references symbol '" + string + "' but it's not defined in the key");
						}

						remainingKeys.remove(string);
						ingredients[j + width * i] = ingredient;
					}
				}
			}

			if (!remainingKeys.isEmpty())
			{
				throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + remainingKeys);
			} else
			{
				return ingredients;
			}
		}

		private AdvRecipe fromJsonObject(JsonObject json)
		{
			Map<String, IRecipeInput> symbols = readSymbols(GsonHelper.getAsJsonObject(json, "key"));
			String[] pattern = getPattern(GsonHelper.getAsJsonArray(json, "pattern"));
			int width = pattern[0].length();
			int height = pattern.length;
			IRecipeInput[] ingredients = createPatternMatrix(pattern, symbols, width, height);
			ItemStack result = RecipeIo.parseOutput(GsonHelper.getAsJsonObject(json, "result"));
			boolean consuming = GsonHelper.getAsBoolean(json, "consuming", false);
			boolean hidden = GsonHelper.getAsBoolean(json, "hidden", false);
			return AdvRecipe.create(RUNTIME_ID, width, height, ingredients, result, consuming, hidden);
		}

		private AdvRecipe fromNetworkBuf(RegistryFriendlyByteBuf buf)
		{
			IRecipeInput[] ingredients = new IRecipeInput[buf.readVarInt()];

			for (int i = 0; i < ingredients.length; i++)
			{
				ingredients[i] = RecipeIo.readInput(buf);
			}

			return new AdvRecipe(
				RUNTIME_ID,
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readInt(),
				ingredients,
				RecipeIo.readItemStack(buf),
				buf.readBoolean(),
				buf.readBoolean()
			);
		}

		private void toNetworkBuf(RegistryFriendlyByteBuf buf, AdvRecipe recipe)
		{
			buf.writeVarInt(recipe.input.length);

			for (IRecipeInput ing : recipe.input)
			{
				RecipeIo.writeInput(buf, ing);
			}

			buf.writeVarInt(recipe.inputWidth);
			buf.writeVarInt(recipe.inputHeight);
			buf.writeBoolean(recipe.inputMirrored != null);
			buf.writeInt(recipe.masks[0]);
			RecipeIo.writeItemStack(buf, recipe.output);
			buf.writeBoolean(recipe.consuming);
			buf.writeBoolean(recipe.hidden);
		}

		@Override
		public @NotNull MapCodec<AdvRecipe> codec()
		{
			return this.codec;
		}

		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf, AdvRecipe> streamCodec()
		{
			return this.streamCodec;
		}
	}
}
