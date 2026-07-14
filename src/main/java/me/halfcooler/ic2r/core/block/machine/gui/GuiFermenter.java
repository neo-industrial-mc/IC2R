package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerFermenter;
import me.halfcooler.ic2r.core.gui.Gauge;
import me.halfcooler.ic2r.core.gui.LinkedGauge;
import me.halfcooler.ic2r.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiFermenter extends Ic2rGui<ContainerFermenter>
{
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guifermenter.png");

	public GuiFermenter(ContainerFermenter container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		this.addElement(TankGauge.createPlain(this, 38, 49, 48, 30, container.base.getInputTank()));
		this.addElement(TankGauge.createNormal(this, 125, 22, container.base.getOutputTank()));
		this.addElement(new LinkedGauge(this, 42, 41, container.base, "heat", Gauge.GaugeStyle.HeatFermenter).withTooltip(() -> Component.translatable("ic2r.Fermenter.gui.info.conversion") + " " + (int) (container.base.getGuiValue("heat") * 100.0) + "%"));
		this.addElement(new LinkedGauge(this, 38, 88, container.base, "progress", Gauge.GaugeStyle.ProgressFermenter).withTooltip("ic2r.Fermenter.gui.info.waste"));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return TEXTURE;
	}
}
