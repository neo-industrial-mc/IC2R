package me.halfcooler.ic2r.core.gui;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ItemStackImage extends GuiElement<ItemStackImage>
{
	private final Supplier<ItemStack> itemSupplier;

	public ItemStackImage(Ic2rGui<?> gui, int x, int y, Supplier<ItemStack> itemSupplier)
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
			this.gui.drawItemStack(this.x, this.y, stack);
		}
	}

	@Override
	public void drawForeground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		if (this.contains(mouseX, mouseY))
		{
			ItemStack stack = this.itemSupplier.get();
			if (!StackUtil.isEmpty(stack))
			{
				this.gui.drawTooltip(guiGraphics, mouseX, mouseY, stack);
			}
		}
	}
}
