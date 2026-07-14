package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerCropmatron;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiCropmatron extends Ic2rGui<ContainerCropmatron>
{
	public GuiCropmatron(ContainerCropmatron container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 192);
		this.addElement(EnergyGauge.asBolt(this, 138, 82, container.base));
		this.addElement(TankGauge.createPlain(this, 11, 26, 24, 47, container.base.getWaterTank()));
		this.addElement(TankGauge.createPlain(this, 105, 26, 24, 47, container.base.getExTank()));
	}

	@Override
	public ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guicropmatron.png");
	}
}
