package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerCondenser;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.gui.Gauge;
import me.halfcooler.ic2r.core.gui.LinkedGauge;
import me.halfcooler.ic2r.core.gui.SlotGrid;
import me.halfcooler.ic2r.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.Supplier;

public class GuiCondenser extends Ic2rGui<ContainerCondenser>
{
	private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guicondenser.png");

	public GuiCondenser(ContainerCondenser container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		Supplier<String> ventTooltipSupplier = () -> Component.translatable("ic2r.Condenser.gui.tooltipvent", (short) 2).getString();
		this.addElement(new SlotGrid(this, 25, 25, 1, 2, SlotGrid.SlotStyle.Normal).withTooltip(ventTooltipSupplier));
		this.addElement(new SlotGrid(this, 133, 25, 1, 2, SlotGrid.SlotStyle.Normal).withTooltip(ventTooltipSupplier));
		this.addElement(EnergyGauge.asBolt(this, 12, 26, container.base));
		this.addElement(TankGauge.createPlain(this, 46, 27, 84, 33, container.base.getInputTank()));
		this.addElement(TankGauge.createPlain(this, 46, 74, 84, 15, container.base.getOutputTank()));
		this.addElement(new LinkedGauge(this, 47, 63, container.base, "progress", Gauge.GaugeStyle.ProgressCondenser));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return BACKGROUND;
	}
}
