package me.halfcooler.ic2r.core.block.invslot;

import me.halfcooler.ic2r.core.block.IInventorySlotHolder;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;

import java.util.Collections;

import net.minecraft.world.level.material.Fluid;

public class InvSlotConsumableLiquidByTank extends InvSlotConsumableLiquid
{
	public final Ic2rFluidTank tank;

	public InvSlotConsumableLiquidByTank(
		IInventorySlotHolder<?> base1,
		String name1,
		InvSlot.Access access1,
		int count,
		InvSlot.InvSide preferredSide1,
		InvSlotConsumableLiquid.OpType opType,
		Ic2rFluidTank tank1
	)
	{
		super(base1, name1, access1, count, preferredSide1, opType);
		this.tank = tank1;
	}

	@Override
	protected boolean acceptsLiquid(Fluid fluid)
	{
		return this.tank.isEmpty() || this.tank.hasExactFluid(fluid);
	}

	@Override
	protected Iterable<Fluid> getPossibleFluids()
	{
		Ic2rFluidStack fs = this.tank.getFluidStack();
		return fs != null ? Collections.singletonList(fs.getFluid()) : null;
	}
}
