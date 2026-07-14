package me.halfcooler.ic2r.core.gui;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TankFluidSlot extends AbstractFluidSlot
{
	final Ic2rFluidTank tank;

	protected TankFluidSlot(Ic2rGui<?> gui, int x, int y, Ic2rFluidTank tank)
	{
		super(gui, x, y, 18, 18);
		if (tank == null)
		{
			throw new NullPointerException("Null FluidTank instance.");
		}

		this.tank = tank;
	}

	public static TankFluidSlot createFluidSlot(Ic2rGui<?> gui, int x, int y, Ic2rFluidTank tank)
	{
		return new TankFluidSlot(gui, x, y, tank);
	}

	@Override
	protected Ic2rFluidStack getFluidStack()
	{
		return this.tank.getFluidStack();
	}

	@Override
	protected boolean onMouseClick(int mouseX, int mouseY, MouseButton button)
	{
		if (button == MouseButton.left)
		{
			Container base = this.getBase();
			if (base instanceof BlockEntity)
			{
				IC2R.network.get(false).initiateClientTileEntityEvent((BlockEntity) base, Screen.hasShiftDown() ? 1 : 0);
				return true;
			}
		}
		return false;
	}
}
