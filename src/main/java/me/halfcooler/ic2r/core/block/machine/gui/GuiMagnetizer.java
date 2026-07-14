package me.halfcooler.ic2r.core.block.machine.gui;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.inherit.Ic2rFenceBlock;
import me.halfcooler.ic2r.core.block.machine.container.ContainerMagnetizer;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiMagnetizer extends Ic2rGui<ContainerMagnetizer>
{
	public GuiMagnetizer(ContainerMagnetizer container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(EnergyGauge.asBolt(this, 11, 28, container.base));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guimagnetizer.png");
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		if (Ic2rFenceBlock.hasMetalShoes(this.menu.getPlayer()))
		{
			this.drawString(guiGraphics, 18, 66, Component.translatable("ic2r.Magnetizer.gui.hasMetalShoes").getString(), 4259648);
		} else
		{
			this.drawString(guiGraphics, 18, 66, Component.translatable("ic2r.Magnetizer.gui.noMetalShoes").getString(), 16728128);
		}
	}
}
