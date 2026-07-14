package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerItemBuffer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiItemBuffer extends Ic2rGui<ContainerItemBuffer>
{
	public GuiItemBuffer(ContainerItemBuffer container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 176, 232);
	}

	@Override
	public ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guiitembuffer.png");
	}
}
