package me.halfcooler.ic2r.core.block.personal;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.Ic2rGui;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiEnergyOMatClosed extends Ic2rGui<ContainerEnergyOMatClosed>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guienergyomatclosed.png");

	public GuiEnergyOMatClosed(ContainerEnergyOMatClosed container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(guiGraphics, 8, this.imageHeight - 96 + 2, Component.translatable("container.inventory").getString(), 4210752);
		this.drawString(guiGraphics, 12, 21, Component.translatable("ic2r.container.personalTrader.want").getString(), 4210752);
		this.drawString(guiGraphics, 12, 39, Component.translatable("ic2r.container.personalTrader.offer").getString(), 4210752);
		this.drawString(guiGraphics, 50, 39, this.menu.base.euOffer + " EU", 4210752);
		this.drawString(guiGraphics, 12, 57, Component.translatable("ic2r.container.personalTraderEnergy.paidFor", this.menu.base.paidFor).getString(), 4210752);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
