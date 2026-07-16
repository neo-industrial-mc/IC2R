package me.halfcooler.ic2r.core.recipe.v2;

import me.halfcooler.ic2r.api.recipe.MachineRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * IC2R machine recipe wrapper implementing the vanilla {@link Recipe} SPI so it can be
 * loaded via datapack RecipeManager. Matching for machines uses {@link #recipe()} directly.
 * <p>
 * Note: in 1.21 vanilla introduced its own {@code net.minecraft.world.item.crafting.RecipeHolder}
 * which wraps this type when returned from {@link net.minecraft.world.item.crafting.RecipeManager#getAllRecipesFor}.
 */
public record RecipeHolder<I, O>(MachineRecipe<I, O> recipe, ResourceLocation id, RecipeSerializer<?> serializer,
                                 RecipeType<?> type)
	implements Recipe<RecipeInput>
{
	@Override
	public boolean matches(@NotNull RecipeInput input, @NotNull Level world)
	{
		return false;
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull RecipeInput input, @NotNull HolderLookup.Provider registries)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return false;
	}

	@Override
	public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries)
	{
		return ItemStack.EMPTY;
	}

	public @NotNull ResourceLocation getId()
	{
		return this.id;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer()
	{
		return this.serializer;
	}

	@Override
	public @NotNull RecipeType<?> getType()
	{
		return this.type;
	}
}
