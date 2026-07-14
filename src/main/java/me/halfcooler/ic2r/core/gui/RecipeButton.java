package me.halfcooler.ic2r.core.gui;

import com.google.common.base.Function;
import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.Ic2rGui;

import java.util.List;

import net.minecraft.network.chat.Component;

public class RecipeButton extends Button<RecipeButton>
{
	public static Function<String[], IClickHandler> jeiRecipeListOpener;

	public RecipeButton(GuiElement<?> wrapping, String[] categories)
	{
		this(wrapping.gui, wrapping.x, wrapping.y, wrapping.width, wrapping.height, categories);
	}

	public RecipeButton(Ic2rGui<?> gui, int x, int y, int width, int height, String[] categories)
	{
		super(gui, x, y, width, height, jeiRecipeListOpener.apply(categories));
	}

	public static boolean canUse()
	{
		return jeiRecipeListOpener != null;
	}

	@Override
	protected List<Component> getToolTip()
	{
		List<Component> ret = super.getToolTip();
		ret.add(Component.translatable("ic2r.jei.recipes"));
		return ret;
	}

	@GuiElement.SkippedMethod
	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
	}
}
