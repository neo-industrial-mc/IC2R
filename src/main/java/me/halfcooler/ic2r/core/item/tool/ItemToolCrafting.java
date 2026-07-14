package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.item.IBoxable;
import me.halfcooler.ic2r.api.item.IItemHudInfo;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemToolCrafting extends Item implements IBoxable, IItemHudInfo
{
	public ItemToolCrafting(Properties settings)
	{
		super(settings);
	}

	protected static int getRemainingUses(ItemStack stack)
	{
		return stack.getMaxDamage() - stack.getDamageValue();
	}

	public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced)
	{
		Ic2rTooltip.add(tooltip, Component.translatable("ic2r.tooltip.tool.uses_left", getRemainingUses(stack)));
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new LinkedList<>();
		info.add(Component.translatable("ic2r.tooltip.tool.uses_left", getRemainingUses(stack)).getString().formatted(ChatFormatting.GRAY));
		return info;
	}
}
