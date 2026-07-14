package me.halfcooler.ic2r.core.block.personal;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rGui;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiTradeOMatClosed extends Ic2rGui<ContainerTradeOMatClosed>
{
	private static final ResourceLocation background = IC2R.getIdentifier("textures/gui/guitradeomatclosed.png");

	public GuiTradeOMatClosed(ContainerTradeOMatClosed container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(guiGraphics, 8, this.imageHeight - 96 + 2, Component.translatable("container.inventory").getString(), 4210752);
		this.drawString(guiGraphics, 12, 23, Component.translatable("ic2r.container.personalTrader.want").getString(), 4210752);
		this.drawString(guiGraphics, 12, 42, Component.translatable("ic2r.container.personalTrader.offer").getString(), 4210752);
		this.drawString(guiGraphics, 12, 60, Component.translatable("ic2r.container.personalTrader.stock").getString(), 4210752);
		this.drawString(guiGraphics, 50, 60, this.menu.base.stock < 0 ? "∞" : this.menu.base.stock + "", this.menu.base.stock != 0 ? 4210752 : 16733525);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
