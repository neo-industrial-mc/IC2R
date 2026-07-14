package me.halfcooler.ic2r.core.block.heatgenerator.gui;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.heatgenerator.container.ContainerFluidHeatGenerator;
import me.halfcooler.ic2r.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiFluidHeatGenerator extends Ic2rGui<ContainerFluidHeatGenerator>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guifluidheatgenerator.png");

	public GuiFluidHeatGenerator(ContainerFluidHeatGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(TankGauge.createNormal(this, 70, 20, container.base.getFluidTank()));
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(guiGraphics, 96, 33, Component.translatable("ic2r.FluidHeatGenerator.gui.info.Emit").getString() + this.menu.base.gettransmitHeat(), 5752026);
		this.drawString(guiGraphics, 96, 52, Component.translatable("ic2r.FluidHeatGenerator.gui.info.MaxEmit").getString() + this.menu.base.getMaxHeatEmittedPerTick(), 5752026);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
