package ic2.core.item.tool;

import ic2.core.Ic2Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiContainmentbox extends Ic2Gui<ContainerContainmentbox>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guicontainmentbox.png");

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
