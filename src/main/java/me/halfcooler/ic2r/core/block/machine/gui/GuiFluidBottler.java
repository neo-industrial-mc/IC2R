package me.halfcooler.ic2r.core.block.machine.gui;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerFluidBottler;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiFluidBottler extends Ic2rGui<ContainerFluidBottler>
{
	public GuiFluidBottler(ContainerFluidBottler container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		this.addElement(EnergyGauge.asBolt(this, 12, 35, container.base));
		this.addElement(TankGauge.createNormal(this, 78, 34, container.base.fluidTank));
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY)
	{
		super.renderBg(guiGraphics, delta, mouseX, mouseY);
		this.bindTexture();
		int progressSize = Math.round(this.menu.base.getProgress() * 16.0F);
		if (progressSize > 0)
		{
			this.drawTexturedRect(guiGraphics.pose(), 61, 36, 198.0, 0.0, progressSize, 13.0);
			this.drawTexturedRect(guiGraphics.pose(), 61, 73, 198.0, 0.0, progressSize, 13.0);
			this.drawTexturedRect(guiGraphics.pose(), 99, 55, 198.0, 0.0, progressSize, 13.0);
		}
	}

	@Override
	public ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guibottler.png");
	}
}
