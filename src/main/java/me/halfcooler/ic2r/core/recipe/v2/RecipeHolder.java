package me.halfcooler.ic2r.core.recipe.v2;

import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.RegistryAccess;

public record RecipeHolder<I, O>(MachineRecipe<I, O> recipe, ResourceLocation id, RecipeSerializer<?> serializer,
                                 RecipeType<?> type)
	implements Recipe<Container>
{
	public boolean matches(@NotNull Container inventory, @NotNull Level world)
	{
		throw new UnsupportedOperationException("Not supported for IC2R machine recipes.");
	}

	public ItemStack assemble(Container inventory)
	{
		throw new UnsupportedOperationException("Not supported for IC2R machine recipes.");
	}

	public @NotNull ItemStack assemble(@NotNull Container inventory, @NotNull RegistryAccess registryAccess)
	{
		throw new UnsupportedOperationException("Not supported for IC2R machine recipes.");
	}

	public boolean canCraftInDimensions(int width, int height)
	{
		throw new UnsupportedOperationException("Not supported for IC2R machine recipes.");
	}

	public ItemStack getResultItem()
	{
		return ItemStack.EMPTY;
	}

	public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess)
	{
		return ItemStack.EMPTY;
	}

	public @NotNull ResourceLocation getId()
	{
		return this.id;
	}

	public @NotNull RecipeSerializer<?> getSerializer()
	{
		return this.serializer;
	}

	public @NotNull RecipeType<?> getType()
	{
		return this.type;
	}
}
