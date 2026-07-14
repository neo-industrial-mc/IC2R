package me.halfcooler.ic2r.core.item.armor;

import me.halfcooler.ic2r.core.ref.Ic2rArmorMaterials;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemArmorCFPack extends ItemArmorFluidTank
{
	public ItemArmorCFPack(Properties settings)
	{
		super(Ic2rArmorMaterials.CF_PACK, settings, Ic2rFluids.CONSTRUCTION_FOAM.still(), 80000);
	}

	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems)
	{
		ItemStack stack = new ItemStack(this);
		this.fillTank(stack);
		subItems.add(stack);
		subItems.add(new ItemStack(this));
	}
}
