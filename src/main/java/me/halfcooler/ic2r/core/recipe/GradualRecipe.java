package me.halfcooler.ic2r.core.recipe;

import com.google.gson.JsonObject;
import me.halfcooler.ic2r.core.item.reactor.AbstractDamageableReactorComponent;
import me.halfcooler.ic2r.core.ref.Ic2rRecipeSerializers;
import me.halfcooler.ic2r.core.util.StackUtil;

import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.RegistryAccess;

/**
 * Shapeless crafting recipe that reduces custom durability ("use") on
 * {@link AbstractDamageableReactorComponent} items (e.g. RSH/LZH condensators)
 * by a fixed amount per charge material.
 *
 * <p>Port of IC2R experimental {@code GradualRecipe}.
 */
public class GradualRecipe implements CraftingRecipe
{
	private final ResourceLocation id;
	private final AbstractDamageableReactorComponent item;
	private final ItemStack chargeMaterial;
	private final int amount;
	private final boolean hidden;

	public GradualRecipe(ResourceLocation id, AbstractDamageableReactorComponent item, ItemStack chargeMaterial, int amount, boolean hidden)
	{
		this.id = id;
		this.item = item;
		this.chargeMaterial = chargeMaterial;
		this.amount = amount;
		this.hidden = hidden;
	}

	@Override
	public boolean matches(@NotNull CraftingContainer inv, @NotNull Level world)
	{
		return !this.assemble(inv, null).isEmpty();
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull CraftingContainer inv, @NotNull RegistryAccess registryAccess)
	{
		ItemStack gridItem = null;
		int chargeMats = 0;

		for (int slot = 0; slot < inv.getContainerSize(); slot++)
		{
			ItemStack stack = inv.getItem(slot);
			if (StackUtil.isEmpty(stack))
			{
				continue;
			}

			if (gridItem == null && stack.getItem() == this.item)
			{
				gridItem = stack;
			}
			else if (StackUtil.checkItemEquality(stack, this.chargeMaterial))
			{
				chargeMats++;
			}
			else
			{
				return ItemStack.EMPTY;
			}
		}

		if (gridItem == null || chargeMats <= 0)
		{
			return ItemStack.EMPTY;
		}

		// Only craft when repair would actually change durability
		int currentUse = this.item.getUse(gridItem);
		if (currentUse <= 0)
		{
			return ItemStack.EMPTY;
		}

		ItemStack result = gridItem.copy();
		result.setCount(1);
		int damage = currentUse - this.amount * chargeMats;
		if (damage > this.item.getMaxUse())
		{
			damage = this.item.getMaxUse();
		}
		else if (damage < 0)
		{
			damage = 0;
		}

		this.item.setUse(result, damage);
		return result;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width * height >= 2;
	}

	@Override
	public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess)
	{
		return new ItemStack(this.item);
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients()
	{
		NonNullList<Ingredient> list = NonNullList.create();
		if (!this.hidden)
		{
			list.add(Ingredient.of(this.item));
			list.add(Ingredient.of(this.chargeMaterial));
		}

		return list;
	}

	@Override
	public boolean isSpecial()
	{
		// Dynamic output (depends on input durability); hide from recipe book
		return true;
	}

	@Override
	public @NotNull ResourceLocation getId()
	{
		return this.id;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer()
	{
		return Ic2rRecipeSerializers.GRADUAL;
	}

	@Override
	public @NotNull CraftingBookCategory category()
	{
		return CraftingBookCategory.MISC;
	}

	public AbstractDamageableReactorComponent getItem()
	{
		return this.item;
	}

	public ItemStack getChargeMaterial()
	{
		return this.chargeMaterial;
	}

	public int getAmount()
	{
		return this.amount;
	}

	public static class Serializer implements RecipeSerializer<GradualRecipe>
	{
		@Override
		public @NotNull GradualRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json)
		{
			Item item = GsonHelper.getAsItem(json, "item");
			if (!(item instanceof AbstractDamageableReactorComponent component))
			{
				throw new IllegalArgumentException("Gradual recipe item must be a damageable reactor component: " + item);
			}

			ItemStack chargeMaterial = net.minecraft.world.item.crafting.ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "charge_material"));
			int amount = GsonHelper.getAsInt(json, "amount");
			boolean hidden = GsonHelper.getAsBoolean(json, "hidden", false);
			return new GradualRecipe(id, component, chargeMaterial, amount, hidden);
		}

		@Override
		public GradualRecipe fromNetwork(@NotNull ResourceLocation id, FriendlyByteBuf buf)
		{
			Item item = BuiltInRegistries.ITEM.byId(buf.readVarInt());
			if (!(item instanceof AbstractDamageableReactorComponent component))
			{
				throw new IllegalStateException("Gradual recipe item is not a damageable reactor component: " + item);
			}

			ItemStack chargeMaterial = buf.readItem();
			int amount = buf.readVarInt();
			boolean hidden = buf.readBoolean();
			return new GradualRecipe(id, component, chargeMaterial, amount, hidden);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, GradualRecipe recipe)
		{
			buf.writeVarInt(BuiltInRegistries.ITEM.getId(recipe.item));
			buf.writeItem(recipe.chargeMaterial);
			buf.writeVarInt(recipe.amount);
			buf.writeBoolean(recipe.hidden);
		}
	}
}
