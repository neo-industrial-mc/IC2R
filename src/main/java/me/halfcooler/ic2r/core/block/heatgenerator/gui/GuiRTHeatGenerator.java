package me.halfcooler.ic2r.core.block.heatgenerator.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.heatgenerator.container.ContainerRTHeatGenerator;
import me.halfcooler.ic2r.core.gui.TextLabel;
import me.halfcooler.ic2r.core.gui.dynamic.TextProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiRTHeatGenerator extends Ic2rGui<ContainerRTHeatGenerator>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guirtheatgenerator.png");

	public GuiRTHeatGenerator(ContainerRTHeatGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(TextLabel.create(this, 49, 66, 79, 13, TextProvider.of(() -> container.base.gettransmitHeat() + " / " + container.base.getMaxHeatEmittedPerTick()), 5752026, false, 0, 0, true, true).withTooltip("ic2r.RTHeatGenerator.gui.tooltipheat"));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
