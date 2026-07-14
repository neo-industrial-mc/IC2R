package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.core.Ic2rGui;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiContainmentbox extends Ic2rGui<ContainerContainmentbox>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guicontainmentbox.png");

	public GuiContainmentbox(ContainerContainmentbox container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
