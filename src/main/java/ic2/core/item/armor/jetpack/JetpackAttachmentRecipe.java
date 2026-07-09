package ic2.core.item.armor.jetpack;

import com.mojang.serialization.MapCodec;
import ic2.api.item.ElectricItem;
import ic2.core.init.IC2Config;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;

import java.text.ParseException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class JetpackAttachmentRecipe implements CraftingRecipe
{
	public static final Set<Item> blacklistedItems = Collections.newSetFromMap(new IdentityHashMap<>());
	private final ResourceLocation id;

	public JetpackAttachmentRecipe(ResourceLocation id)
	{
		this.id = id;
	}

	public static void init()
	{
		try
		{
			for (ItemStack stack : ConfigUtil.asStackList(IC2Config.recipes.jetpackAttachmentBlacklist.get()))
			{
				blacklistedItems.add(stack.getItem());
			}
		} catch (ParseException pe)
		{
			throw new RuntimeException(pe);
		}

		blacklistedItems.add(Ic2Items.JETPACK);
		blacklistedItems.add(Ic2Items.JETPACK_ELECTRIC);
		blacklistedItems.add(Ic2Items.QUANTUM_CHESTPLATE);
		blacklistedItems.add(Items.ELYTRA);
	}

	@Override
	public boolean matches(@NotNull CraftingInput inv, @NotNull Level world)
	{
		return !this.assemble(inv, (HolderLookup.Provider) null).isEmpty();
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull CraftingInput inv, HolderLookup.Provider registryAccess)
	{
		ItemStack jetpack = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		boolean attachmentPlate = false;

		for (int i = 0; i < inv.size(); i++)
		{
			ItemStack currentStack = inv.getItem(i);
			if (!currentStack.isEmpty())
			{
				Item item = currentStack.getItem();
				if (item == Ic2Items.JETPACK_ELECTRIC)
				{
					if (!jetpack.isEmpty())
					{
						return ItemStack.EMPTY;
					}

					jetpack = currentStack;
				} else if (StackUtil.getEquipmentSlotForItem(currentStack) == EquipmentSlot.CHEST && !blacklistedItems.contains(item))
				{
					if (!armor.isEmpty())
					{
						return ItemStack.EMPTY;
					}

					armor = currentStack;
				} else
				{
					if (item != Ic2Items.JETPACK_ATTACHMENT_PLATE || attachmentPlate)
					{
						return ItemStack.EMPTY;
					}

					attachmentPlate = true;
				}
			}
		}

		if (!jetpack.isEmpty() && !armor.isEmpty() && attachmentPlate && !JetpackHandler.hasJetpackAttached(armor))
		{
			ItemStack ret = armor.copy();
			JetpackHandler.setJetpackAttached(ret, true);
			ElectricItem.manager.charge(ret, ElectricItem.manager.getCharge(jetpack), Integer.MAX_VALUE, true, false);
			return ret;
		} else
		{
			return ItemStack.EMPTY;
		}
	}

	@Override
	public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registryAccess)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int x, int y)
	{
		return x * y >= 3;
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients()
	{
		return NonNullList.create();
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer()
	{
		return Ic2RecipeSerializers.JETPACK_ATTACHMENT;
	}

	public @NotNull ResourceLocation getId()
	{
		return this.id;
	}

	@Override
	public @NotNull CraftingBookCategory category()
	{
		return CraftingBookCategory.MISC;
	}

	public static final class Serializer implements RecipeSerializer<JetpackAttachmentRecipe>
	{
		private final MapCodec<JetpackAttachmentRecipe> codec = MapCodec.unit(() -> new JetpackAttachmentRecipe(null));
		private final StreamCodec<RegistryFriendlyByteBuf, JetpackAttachmentRecipe> streamCodec =
			StreamCodec.of((buf, recipe) ->
			{
			}, buf -> new JetpackAttachmentRecipe(null));

		@Override
		public @NotNull MapCodec<JetpackAttachmentRecipe> codec()
		{
			return this.codec;
		}

		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf, JetpackAttachmentRecipe> streamCodec()
		{
			return this.streamCodec;
		}
	}
}
