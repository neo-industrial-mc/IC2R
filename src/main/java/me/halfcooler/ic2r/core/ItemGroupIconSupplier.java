package me.halfcooler.ic2r.core;

import me.halfcooler.ic2r.core.ref.Ic2rItems;

import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class ItemGroupIconSupplier implements Supplier<ItemStack>
{
	private static ItemStack a;
	private static ItemStack b;
	private static ItemStack z;
	private int ticker;
	private final Ic2rItemGroupType groupType;

	public ItemGroupIconSupplier(Ic2rItemGroupType groupType)
	{
		this.groupType = groupType;
	}

	public ItemStack get()
	{
		if (this.groupType == Ic2rItemGroupType.GENERAL || this.groupType == Ic2rItemGroupType.TOOLS_AND_UTILITIES && IC2R.seasonal)
		{
			if (a == null)
			{
				a = new ItemStack(Items.ZOMBIE_HEAD);
			}

			if (b == null)
			{
				b = new ItemStack(Items.SKELETON_SKULL);
			}

			if (z == null)
			{
				z = Ic2rItems.NANO_CHESTPLATE.getDefaultInstance();
			}

			if (++this.ticker >= 5000)
			{
				this.ticker = 0;
			}

			if (this.ticker >= 2500)
			{
				return this.ticker < 3000 ? a : (this.ticker < 4500 ? b : z);
			}
		}
		return switch (this.groupType)
		{
			case GENERAL -> new ItemStack(Ic2rItems.MACHINE);
			case REACTOR -> new ItemStack(Ic2rItems.NUCLEAR_REACTOR);
			case MACHINES -> new ItemStack(Ic2rItems.MACERATOR);
			case GENERATORS_AND_WIRING -> new ItemStack(Ic2rItems.GENERATOR);
			case TOOLS_AND_UTILITIES -> new ItemStack(Ic2rItems.WRENCH);
			case FLUID_CELLS -> new ItemStack(Ic2rItems.FACADE_CELL);
			case COMBAT -> new ItemStack(Ic2rItems.NANO_SABER).setHoverName(Component.nullToEmpty("ic2r:tab_icon"));
			case MATERIALS -> new ItemStack(Ic2rItems.RUBBER);
			case FARMING -> new ItemStack(Ic2rItems.CROP_SEED_BACK);
		};
	}
}
