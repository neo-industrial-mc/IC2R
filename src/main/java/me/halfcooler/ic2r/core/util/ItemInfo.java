package me.halfcooler.ic2r.core.util;

import me.halfcooler.ic2r.api.info.IInfoProvider;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class ItemInfo implements IInfoProvider
{
	@Override
	public double getEnergyValue(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return 0.0;
		} else if (StackUtil.checkItemEquality(stack, Items.REDSTONE))
		{
			return 800.0;
		} else if (StackUtil.checkItemEquality(stack, Ic2rItems.SINGLE_USE_BATTERY))
		{
			return 1200.0;
		} else
		{
			return StackUtil.checkItemEquality(stack, Ic2rItems.ENERGIUM_DUST) ? 16000.0 : 0.0;
		}
	}

	@Override
	public int getFuelValue(ItemStack stack, boolean allowLava)
	{
		if (StackUtil.isEmpty(stack))
		{
			return 0;
		}

		if ((StackUtil.checkItemEquality(stack, Ic2rItems.SCRAP) || StackUtil.checkItemEquality(stack, Ic2rItems.SCRAP_BOX))
			&& !IC2RConfig.misc.allowBurningScrap.get())
		{
			return 0;
		}

		Ic2rFluidStack liquid = Ic2rFluidStack.get(stack);
		boolean isLava = liquid != null && !liquid.isEmpty() && liquid.getFluid() == Fluids.LAVA;
		if (isLava && !allowLava)
		{
			return 0;
		}

		int ret = IC2R.envProxy.getBurnTime(stack);
		return isLava ? ret / 10 : ret;
	}
}
