package me.halfcooler.ic2r.core.block.transport.cover;

import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;

import java.util.Set;

import net.minecraft.world.item.ItemStack;

public interface ICoverItem
{
	boolean isSuitableFor(ItemStack var1, Set<CoverProperty> var2);

	boolean onTick(ItemStack var1, ICoverHolder var2);

	boolean allowsInput(ItemStack var1);

	boolean allowsInput(Ic2rFluidStack var1);

	boolean allowsOutput(ItemStack var1);

	boolean allowsOutput(Ic2rFluidStack var1);
}
