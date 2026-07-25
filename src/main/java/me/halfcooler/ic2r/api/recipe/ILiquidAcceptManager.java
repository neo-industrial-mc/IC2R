package me.halfcooler.ic2r.api.recipe;

import net.minecraft.world.level.material.Fluid;

import java.util.Set;

public interface ILiquidAcceptManager
{
	boolean acceptsFluid(Fluid var1);

	Set<Fluid> getAcceptedFluids();
}
