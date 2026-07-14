package me.halfcooler.ic2r.core.item.tool;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.gui.TextLabel;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiToolbox extends Ic2rGui<ContainerToolbox>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guitoolbox.png");

	public GuiToolbox(ContainerToolbox container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(TextLabel.create(this, 65, 11, Ic2rItems.TOOL_BOX.getDescription().getString(), 0, false));
	}

	@Override
	protected void drawBackgroundAndTitle(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY)
	{
		this.bindTexture();
		guiGraphics.blit(this.getTextureLocation(), this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
