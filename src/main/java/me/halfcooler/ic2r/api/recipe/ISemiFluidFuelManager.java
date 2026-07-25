package me.halfcooler.ic2r.api.recipe;

import net.minecraft.world.level.material.Fluid;

import java.util.Map;

public interface ISemiFluidFuelManager extends ILiquidAcceptManager
{
	void addFluid(Fluid var1, int var2, double var3);

	ISemiFluidFuelManager.BurnProperty getBurnProperty(Fluid var1);

	Map<Fluid, ISemiFluidFuelManager.BurnProperty> getBurnProperties();

	record BurnProperty(int amount, double power)
	{
	}
}
