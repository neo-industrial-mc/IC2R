package me.halfcooler.ic2r.api.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import java.util.List;

@NonExtendable
public interface IRecipeInput
{
	boolean matches(ItemStack var1);

	int getAmount();

	List<ItemStack> getInputs();

	default Ingredient getIngredient()
	{
		return Ingredient.of(this.getInputs().stream());
	}
}
