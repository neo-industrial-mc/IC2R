package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.api.item.ElectricItem;

import java.util.List;

import me.halfcooler.ic2r.api.energy.profile.VoltageTier;
import me.halfcooler.ic2r.core.energy.profile.ElectricalDisplay;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ElectricItemTooltipHandler
{
	public static void addTooltip(ItemStack stack, List<Component> out)
	{
		if (stack != null && ElectricItem.manager.getMaxCharge(stack) > 0.0)
		{
			String tooltip = ElectricItem.manager.getToolTip(stack);
			if (tooltip != null && !tooltip.trim().isEmpty())
			{
				Ic2rTooltip.add(out, Component.literal(tooltip));
				if (Screen.hasShiftDown())
				{
					Ic2rTooltip.add(out, ElectricalDisplay.formatVoltage(VoltageTier.fromIcTier(ElectricItem.manager.getTier(stack))));
				}
			}
		}
	}
}