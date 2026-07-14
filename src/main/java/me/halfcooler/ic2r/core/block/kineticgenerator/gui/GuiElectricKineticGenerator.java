package me.halfcooler.ic2r.core.block.kineticgenerator.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.kineticgenerator.container.ContainerElectricKineticGenerator;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.gui.SlotGrid;
import me.halfcooler.ic2r.core.gui.TextLabel;
import me.halfcooler.ic2r.core.gui.dynamic.TextProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiElectricKineticGenerator extends Ic2rGui<ContainerElectricKineticGenerator>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guielectrickineticgenerator.png");

	public GuiElectricKineticGenerator(ContainerElectricKineticGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(new SlotGrid(this, 43, 26, 5, 2, SlotGrid.SlotStyle.Normal).withTooltip("ic2r.ElectricKineticGenerator.gui.motors"));
		this.addElement(EnergyGauge.asBolt(this, 12, 44, container.base));
		this.addElement(TextLabel.create(this, 29, 66, 119, 13, TextProvider.of(() -> Component.translatable("ic2r.ElectricKineticGenerator.gui.kUmax", GuiElectricKineticGenerator.this.menu.base.getMaxKU(), GuiElectricKineticGenerator.this.menu.base.getMaxKUForGUI()).getString()), 5752026, false, true, true).withTooltip("ic2r.ElectricKineticGenerator.gui.tooltipkin"));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
