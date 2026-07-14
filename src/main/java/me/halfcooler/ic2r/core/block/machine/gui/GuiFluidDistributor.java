package me.halfcooler.ic2r.core.block.machine.gui;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerFluidDistributor;
import me.halfcooler.ic2r.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiFluidDistributor extends Ic2rGui<ContainerFluidDistributor>
{
	public GuiFluidDistributor(ContainerFluidDistributor container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		this.addElement(TankGauge.createPlain(this, 29, 38, 55, 47, container.base.fluidTank));
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(guiGraphics, 116, 47, Component.translatable("ic2r.fluid_distributor.gui.mode.info").getString(), 5752026);

		String drawnString = this.menu.base.getActive() ? Component.translatable("ic2r.fluid_distributor.gui.mode.concentrate").getString() : Component.translatable("ic2r.fluid_distributor.gui.mode.distribute").getString();
		this.drawString(guiGraphics, 99, 71, drawnString, 5752026);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		mouseX -= this.leftPos;
		mouseY -= this.topPos;
		if (mouseX >= 117.0 && mouseY >= 58.0 && mouseX <= 135.0 && mouseY <= 66.0)
		{
			IC2R.network.get(false).initiateClientTileEntityEvent(this.menu.base, 1);
			return true;
		} else
		{
			mouseX += this.leftPos;
			mouseY += this.topPos;
			return super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guifluiddistributor.png");
	}
}
