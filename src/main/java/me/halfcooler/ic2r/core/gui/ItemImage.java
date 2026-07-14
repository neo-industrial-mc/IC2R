package me.halfcooler.ic2r.core.gui;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;

public class ItemImage extends GuiElement<ItemImage>
{
	private final Supplier<ItemStack> itemSupplier;

	public ItemImage(Ic2rGui<?> gui, int x, int y, Supplier<ItemStack> itemSupplier)
	{
		super(gui, x, y, 16, 16);
		this.itemSupplier = itemSupplier;
	}

	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawBackground(guiGraphics, mouseX, mouseY);
		ItemStack stack = this.itemSupplier.get();
		if (!StackUtil.isEmpty(stack))
		{
			this.gui.drawItem(this.x, this.y, stack);
		}
	}
}
