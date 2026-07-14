package me.halfcooler.ic2r.core.block.machine.gui;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerMatter;
import me.halfcooler.ic2r.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiMatter extends Ic2rGui<ContainerMatter>
{
	public String progressLabel;
	public String amplifierLabel;

	public GuiMatter(ContainerMatter container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(TankGauge.createNormal(this, 96, 22, container.base.fluidTank));
		this.progressLabel = Component.translatable("ic2r.Matter.gui.info.progress").getString();
		this.amplifierLabel = Component.translatable("ic2r.Matter.gui.info.amplifier").getString();
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(guiGraphics, 8, 22, this.progressLabel, 4210752);
		this.drawString(guiGraphics, 18, 31, this.menu.base.getProgressAsString(), 4210752);
		if (this.menu.base.scrap > 0)
		{
			this.drawString(guiGraphics, 8, 46, this.amplifierLabel, 4210752);
			this.drawString(guiGraphics, 8, 58, this.menu.base.scrap + "", 4210752);
		}
	}

	@Override
	public ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guimatter.png");
	}
}
