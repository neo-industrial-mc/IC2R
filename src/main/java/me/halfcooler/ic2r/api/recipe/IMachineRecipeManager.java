package me.halfcooler.ic2r.api.recipe;

public interface IMachineRecipeManager<RI, RO, I>
{
	MachineRecipeResult<RI, RO, I> apply(I var1, boolean var2);

	Iterable<? extends MachineRecipe<RI, RO>> getRecipes();

	boolean isIterable();
}
