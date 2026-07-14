package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerCropHarvester;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiCropHarvester extends Ic2rGui<ContainerCropHarvester>
{
	public GuiCropHarvester(ContainerCropHarvester container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(EnergyGauge.asBolt(this, 19, 37, container.base));
	}

	@Override
	public ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guicropharvester.png");
	}
}
