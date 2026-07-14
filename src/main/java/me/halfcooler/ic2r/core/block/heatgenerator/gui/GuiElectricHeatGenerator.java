package me.halfcooler.ic2r.core.block.heatgenerator.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.heatgenerator.container.ContainerElectricHeatGenerator;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.gui.SlotGrid;
import me.halfcooler.ic2r.core.gui.TextLabel;
import me.halfcooler.ic2r.core.gui.dynamic.TextProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiElectricHeatGenerator extends Ic2rGui<ContainerElectricHeatGenerator>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guielectricheatgenerator.png");

	public GuiElectricHeatGenerator(ContainerElectricHeatGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(new SlotGrid(this, 43, 26, 5, 2, SlotGrid.SlotStyle.Normal).withTooltip("ic2r.ElectricHeatGenerator.gui.coils"));
		this.addElement(EnergyGauge.asBolt(this, 12, 44, container.base));
		this.addElement(TextLabel.create(this, 34, 66, 109, 13, TextProvider.of(() -> Component.translatable("ic2r.ElectricHeatGenerator.gui.hUmax", GuiElectricHeatGenerator.this.menu.base.gettransmitHeat(), GuiElectricHeatGenerator.this.menu.base.getMaxHeatEmittedPerTick()).getString()), 5752026, false, true, true).withTooltip("ic2r.ElectricHeatGenerator.gui.tooltipheat"));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
