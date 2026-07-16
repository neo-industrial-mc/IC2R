package me.halfcooler.ic2r.core.item.armor;

import me.halfcooler.ic2r.api.item.IItemHudInfo;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.StandardFluidItem;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

public abstract class ItemArmorFluidTank extends ItemArmorUtility implements StandardFluidItem, IItemHudInfo
{
	protected final int capacity;
	protected final Fluid allowfluid;

	public ItemArmorFluidTank(ArmorMaterial material, Properties settings, Fluid allowfluid, int capacity)
	{
		super(material, settings, EquipmentSlot.CHEST);
		this.capacity = capacity;
		this.allowfluid = allowfluid;
	}

	private static Component getTooltipComponent(ItemStack stack)
	{
		Ic2rFluidStack fs = Ic2rFluidStack.get(stack);
		if (fs.isEmpty())
		{
			return Component.translatable("ic2r.item.fluid_container.empty");
		} else
		{
			return Component.translatable("ic2r.item.fluid_container.with_fluid", fs.getFluidDisplayName(), fs.getAmountMb());
		}
	}

	@Override
	public int getCapacityMb(ItemStack stack)
	{
		return this.capacity;
	}

	@Override
	public boolean canFill(ItemStack stack, Ic2rFluidStack fs)
	{
		return fs.getFluid() == this.allowfluid;
	}

	public void fillTank(ItemStack stack)
	{
		this.fillMb(stack, Ic2rFluidStack.create(this.allowfluid, Integer.MAX_VALUE), false, null);
	}

	public double getCharge(ItemStack stack)
	{
		return Ic2rFluidStack.get(stack).getAmountMb();
	}

	private double getChargeLevel(ItemStack stack)
	{
		return this.getCharge(stack) / this.capacity;
	}

	public double getMaxCharge()
	{
		return this.capacity;
	}

	public boolean isEmpty(ItemStack stack)
	{
		return Ic2rFluidStack.get(stack).isEmpty();
	}

	public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced)
	{
		super.appendHoverText(stack, world, tooltip, advanced);
		Ic2rTooltip.add(tooltip, getTooltipComponent(stack));
	}

	public boolean isBarVisible(@NotNull ItemStack stack)
	{
		return true;
	}

	public int getBarWidth(@NotNull ItemStack stack)
	{
		return (int) Math.round(this.getChargeLevel(stack) * 13.0);
	}

	public int getBarColor(@NotNull ItemStack stack)
	{
		return Mth.hsvToRgb((float) (this.getChargeLevel(stack) / 3.0), 1.0F, 1.0F);
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new ArrayList<>();
		info.add(getTooltipComponent(stack).getString());
		return info;
	}

	public boolean isValidRepairItem(@NotNull ItemStack par1ItemStack, @NotNull ItemStack par2ItemStack)
	{
		return false;
	}
}
