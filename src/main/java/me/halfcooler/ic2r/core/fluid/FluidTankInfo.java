package me.halfcooler.ic2r.core.fluid;

public record FluidTankInfo(int drainSideMask, int fillSideMask, int capacity, Ic2rFluidStack content)
{
	public static final int ALL_SIDES = 63;


}
