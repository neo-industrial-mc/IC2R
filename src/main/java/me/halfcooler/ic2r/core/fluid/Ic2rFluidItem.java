package me.halfcooler.ic2r.core.fluid;

import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.Mutable;

public interface Ic2rFluidItem
{
	Ic2rFluidStack getFluidStack(ItemStack var1);

	int getCapacityMb(ItemStack var1);

	Ic2rFluidStack drainMb(ItemStack var1, int var2, boolean var3, Mutable<ItemStack> var4);

	int drainMb(ItemStack var1, Ic2rFluidStack var2, boolean var3, Mutable<ItemStack> var4);

	int fillMb(ItemStack var1, Ic2rFluidStack var2, boolean var3, Mutable<ItemStack> var4);
}
