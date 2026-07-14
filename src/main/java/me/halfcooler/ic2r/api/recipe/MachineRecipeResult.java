package me.halfcooler.ic2r.api.recipe;

public record MachineRecipeResult<RI, RO, I>(MachineRecipe<RI, RO> recipe, I adjustedInput)
{


	public RO getOutput()
	{
		return this.recipe.getOutput();
	}


}
