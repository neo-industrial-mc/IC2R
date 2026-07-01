package ic2.core.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;

public final class Ic2Tooltip
{
	private Ic2Tooltip()
	{
	}

	public static void add(List<Component> tooltip, Component line)
	{
		tooltip.add(withDefaultStyle(line));
	}

	public static Component withDefaultStyle(Component component)
	{
		if (hasExplicitColor(component))
		{
			return component;
		}

		MutableComponent copy = component.copy();
		copy.withStyle(ChatFormatting.GRAY);
		return copy;
	}

	private static boolean hasExplicitColor(Component component)
	{
		if (component.getStyle().getColor() != null)
		{
			return true;
		}

		if (component.getContents() instanceof LiteralContents literal && literal.text().indexOf('§') >= 0)
		{
			return true;
		}

		if (component.getContents() instanceof TranslatableContents translatable)
		{
			for (Object arg : translatable.getArgs())
			{
				if (arg instanceof Component argComponent && hasExplicitColor(argComponent))
				{
					return true;
				}
			}
		}

		for (Component sibling : component.getSiblings())
		{
			if (hasExplicitColor(sibling))
			{
				return true;
			}
		}

		return false;
	}
}