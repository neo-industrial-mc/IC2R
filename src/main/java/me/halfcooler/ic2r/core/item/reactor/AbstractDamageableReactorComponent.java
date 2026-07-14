package me.halfcooler.ic2r.core.item.reactor;

import me.halfcooler.ic2r.api.reactor.IReactor;
import me.halfcooler.ic2r.api.reactor.IReactorComponent;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.Util;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDamageableReactorComponent extends Item implements IReactorComponent
{
	private final int maxUse;

	protected AbstractDamageableReactorComponent(Properties settings, int maxUse)
	{
		super(settings);
		this.maxUse = maxUse;
	}

	@Override
	public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatRun)
	{
	}

	@Override
	public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun)
	{
		return false;
	}

	@Override
	public boolean canStoreHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return false;
	}

	@Override
	public int getMaxHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return 0;
	}

	@Override
	public int getCurrentHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return 0;
	}

	@Override
	public int alterHeat(ItemStack stack, IReactor reactor, int x, int y, int heat)
	{
		return heat;
	}

	@Override
	public float influenceExplosion(ItemStack stack, IReactor reactor)
	{
		return 0.0F;
	}

	public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced)
	{
		super.appendHoverText(stack, world, tooltip, advanced);
		Ic2rTooltip.add(tooltip, Component.translatable("ic2r.reactoritem.durability", this.getMaxUse() - this.getUse(stack), this.getMaxUse()));
	}

	@Override
	public boolean canBePlacedIn(ItemStack stack, IReactor reactor)
	{
		return true;
	}

	public int getUse(ItemStack stack)
	{
		CompoundTag nbt = stack.getTag();
		return nbt != null ? nbt.getInt("use") : 0;
	}

	public void setUse(ItemStack stack, int use)
	{
		stack.getOrCreateTag().putInt("use", use);
	}

	protected void incrementUse(ItemStack stack)
	{
		stack.getOrCreateTag().putInt("use", Math.min(this.getUse(stack) + 1, this.maxUse));
	}

	public int getMaxUse()
	{
		return this.maxUse;
	}

	public double getUseFraction(ItemStack stack)
	{
		return Util.limit((double) this.getUse(stack) / this.maxUse, 0.0, 1.0);
	}

	public boolean isBarVisible(@NotNull ItemStack stack)
	{
		return true;
	}

	public int getBarWidth(@NotNull ItemStack stack)
	{
		return (int) Math.round((1.0 - this.getUseFraction(stack)) * 13.0);
	}

	public int getBarColor(@NotNull ItemStack stack)
	{
		return Mth.hsvToRgb((float) ((1.0 - this.getUseFraction(stack)) / 3.0), 1.0F, 1.0F);
	}
}
