package me.halfcooler.ic2r.core.item.armor;

import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.item.armor.jetpack.IJetpack;
import me.halfcooler.ic2r.core.ref.Ic2rArmorMaterials;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemArmorJetpack extends ItemArmorFluidTank implements IJetpack
{
	public ItemArmorJetpack(Properties settings)
	{
		super(Ic2rArmorMaterials.JET_PACK, settings, Ic2rFluids.BIOGAS.still(), 30000);
	}

	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> stacks)
	{
		ItemStack stack = new ItemStack(this);
		this.fillTank(stack);
		stacks.add(stack);
		stacks.add(new ItemStack(this));
	}

	@Override
	public void drainEnergy(ItemStack pack, int amount)
	{
		if (this.isEmpty(pack))
		{
			return;
		}

		Ic2rFluidStack fs = this.drainMb(pack, amount, true, null);
		if (fs.getAmountMb() < amount)
		{
			return;
		}

		this.drainMb(pack, amount, false, null);
	}

	@Override
	public float getPower(ItemStack stack)
	{
		return 1.0F;
	}

	@Override
	public float getDropPercentage(ItemStack stack)
	{
		return 0.2F;
	}

	@Override
	public boolean isJetpackActive(ItemStack stack)
	{
		return true;
	}

	@Override
	public double getChargeLevel(ItemStack stack)
	{
		return this.getCharge(stack) / this.getMaxCharge();
	}

	@Override
	public float getHoverMultiplier(ItemStack stack, boolean upwards)
	{
		return 0.2F;
	}

	@Override
	public float getWorldHeightDivisor(ItemStack stack)
	{
		return 1.0F;
	}
}
