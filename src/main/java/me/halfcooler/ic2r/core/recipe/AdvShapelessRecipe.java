package me.halfcooler.ic2r.core.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.MapCodec;
import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.item.tool.ItemToolCrafting;
import me.halfcooler.ic2r.core.recipe.v2.JsonRecipeCodecs;
import me.halfcooler.ic2r.core.recipe.v2.RecipeIo;
import me.halfcooler.ic2r.core.ref.Ic2rRecipeSerializers;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AdvShapelessRecipe implements CraftingRecipe
{
	private static final ResourceLocation RUNTIME_ID = ResourceLocation.fromNamespaceAndPath("ic2r", "shapeless");

	public final ItemStack output;
	public final IRecipeInput[] input;
	public final boolean hidden;
	public final boolean consuming;
	/** Internal id only; vanilla wraps recipes in RecipeHolder. */
	private final ResourceLocation id;

	public AdvShapelessRecipe(ResourceLocation id, IRecipeInput[] input, ItemStack output, boolean hidden, boolean consuming)
	{
		this.id = id;
		this.input = input;
		this.output = output;
		this.hidden = hidden;
		this.consuming = consuming;
	}

	@Override
	public boolean matches(@NotNull CraftingInput inventorycrafting, @NotNull Level world)
	{
		return this.assemble(inventorycrafting) != StackUtil.emptyStack;
	}

	public ItemStack assemble(CraftingInput inventorycrafting)
	{
		int offerSize = inventorycrafting.size();
		if (offerSize < this.input.length)
		{
			return StackUtil.emptyStack;
		}

		List<IRecipeInput> unmatched = new ArrayList<>(Arrays.asList(this.input));
		double outputCharge = 0.0;

		label36:
		for (int i = 0; i < offerSize; i++)
		{
			ItemStack offer = inventorycrafting.getItem(i);
			if (!StackUtil.isEmpty(offer))
			{
				for (int j = 0; j < unmatched.size(); j++)
				{
					if (unmatched.get(j).matches(offer))
					{
						outputCharge += ElectricItem.manager.getCharge(StackUtil.copyWithSize(offer, 1));
						unmatched.remove(j);
						continue label36;
					}
				}

				return StackUtil.emptyStack;
			}
		}

		if (!unmatched.isEmpty())
		{
			return StackUtil.emptyStack;
		}

		ItemStack ret = this.output.copy();
		ElectricItem.manager.charge(ret, outputCharge, Integer.MAX_VALUE, true, false);
		return ret;
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
		return AdvRecipe.canShow(this.input, this.output, this.hidden);
	}

	@Override
	public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput inv)
	{
		if (this.consuming)
		{
			return NonNullList.withSize(inv.size(), StackUtil.emptyStack);
		}

		NonNullList<ItemStack> defaultedList = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

		for (int i = 0; i < defaultedList.size(); i++)
		{
			ItemStack stack = inv.getItem(i);
			ItemStack remainder = IC2R.envProxy.getRecipeRemainder(stack);
			if (stack.getItem() instanceof ItemToolCrafting)
			{
				remainder = stack.copy();
				remainder.setDamageValue(remainder.getDamageValue() + 1);
				if (remainder.getDamageValue() == remainder.getMaxDamage())
				{
					remainder = ItemStack.EMPTY;
				}
			}

			if (!remainder.isEmpty())
			{
				defaultedList.set(i, remainder);
			}
		}

		return defaultedList;
	}

	@Override
	public boolean canCraftInDimensions(int x, int y)
	{
		return x * y >= this.input.length;
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients()
	{
		NonNullList<Ingredient> list = NonNullList.create();
		if (!this.hidden)
		{
			for (IRecipeInput input : this.input)
			{
				list.add(input.getIngredient());
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
		return Ic2rRecipeSerializers.SHAPELESS;
	}

	public static class Serializer implements RecipeSerializer<AdvShapelessRecipe>
	{
		private final MapCodec<AdvShapelessRecipe> codec = JsonRecipeCodecs.mapCodec(this::fromJsonObject);
		private final StreamCodec<RegistryFriendlyByteBuf, AdvShapelessRecipe> streamCodec =
			JsonRecipeCodecs.streamCodec(this::fromNetworkBuf, this::toNetworkBuf);

		private static IRecipeInput[] getIngredients(JsonArray json)
		{
			IRecipeInput[] inputs = new IRecipeInput[json.size()];

			for (int i = 0; i < json.size(); i++)
			{
				inputs[i] = RecipeIo.parseInput(json.get(i));
			}

			return inputs;
		}

		private AdvShapelessRecipe fromJsonObject(JsonObject json)
		{
			IRecipeInput[] ingredients = getIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
			if (ingredients.length == 0)
			{
				throw new JsonParseException("No ingredients for IC2R shapeless recipe");
			}

			if (ingredients.length > 9)
			{
				throw new JsonParseException("Too many ingredients for IC2R shapeless recipe");
			}

			ItemStack result = RecipeIo.parseOutput(GsonHelper.getAsJsonObject(json, "result"));
			boolean consuming = GsonHelper.getAsBoolean(json, "consuming", false);
			boolean hidden = GsonHelper.getAsBoolean(json, "hidden", false);
			return new AdvShapelessRecipe(RUNTIME_ID, ingredients, result, hidden, consuming);
		}

		private AdvShapelessRecipe fromNetworkBuf(RegistryFriendlyByteBuf buf)
		{
			IRecipeInput[] inputs = new IRecipeInput[buf.readVarInt()];

			for (int i = 0; i < inputs.length; i++)
			{
				inputs[i] = RecipeIo.readInput(buf);
			}

			return new AdvShapelessRecipe(
				RUNTIME_ID,
				inputs,
				RecipeIo.readItemStack(buf),
				buf.readBoolean(),
				buf.readBoolean()
			);
		}

		private void toNetworkBuf(RegistryFriendlyByteBuf buf, AdvShapelessRecipe recipe)
		{
			buf.writeVarInt(recipe.input.length);

			for (IRecipeInput input : recipe.input)
			{
				RecipeIo.writeInput(buf, input);
			}

			RecipeIo.writeItemStack(buf, recipe.output);
			buf.writeBoolean(recipe.hidden);
			buf.writeBoolean(recipe.consuming);
		}

		@Override
		public @NotNull MapCodec<AdvShapelessRecipe> codec()
		{
			return this.codec;
		}

		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf, AdvShapelessRecipe> streamCodec()
		{
			return this.streamCodec;
		}
	}
}
