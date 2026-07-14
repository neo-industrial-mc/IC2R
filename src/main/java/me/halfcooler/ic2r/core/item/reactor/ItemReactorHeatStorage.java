package me.halfcooler.ic2r.core.item.reactor;

import me.halfcooler.ic2r.api.reactor.IReactor;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemReactorHeatStorage extends AbstractDamageableReactorComponent
{
	public ItemReactorHeatStorage(Properties settings, int heatStorage)
	{
		super(settings, heatStorage);
	}

	@Override
	public boolean canStoreHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return true;
	}

	@Override
	public int getMaxHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return this.getMaxUse();
	}

	@Override
	public int getCurrentHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return this.getUse(stack);
	}

	@Override
	public int alterHeat(ItemStack stack, IReactor reactor, int x, int y, int heat)
	{
		int myHeat = this.getCurrentHeat(stack, reactor, x, y);
		myHeat += heat;
		int max = this.getMaxHeat(stack, reactor, x, y);
		if (myHeat > max)
		{
			reactor.setItemAt(x, y, null);
			heat = max - myHeat + 1;
		} else
		{
			if (myHeat < 0)
			{
				heat = myHeat;
				myHeat = 0;
			} else
			{
				heat = 0;
			}

			this.setUse(stack, myHeat);
		}

		return heat;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced)
	{
		super.appendHoverText(stack, world, tooltip, advanced);
		if (this.getUse(stack) > 0)
		{
			Ic2rTooltip.add(tooltip, Component.translatable("ic2r.reactoritem.heatwarning.line1"));
			Ic2rTooltip.add(tooltip, Component.translatable("ic2r.reactoritem.heatwarning.line2"));
		}
	}
}
