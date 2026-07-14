package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerFluidRegulator;
import me.halfcooler.ic2r.core.gui.CustomButton;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.gui.TankGauge;
import me.halfcooler.ic2r.core.gui.TextLabel;
import me.halfcooler.ic2r.core.gui.dynamic.TextProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiFluidRegulator extends Ic2rGui<ContainerFluidRegulator>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guifluidregulator.png");

	public GuiFluidRegulator(ContainerFluidRegulator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		this.addElement(EnergyGauge.asBolt(this, 12, 39, container.base));
		this.addElement(TankGauge.createNormal(this, 78, 34, container.base.getFluidTank()));

		for (int i = 0; i < 4; i++)
		{
			int val = (int) Math.pow(10.0, 3 - i);
			this.addElement(new CustomButton(this, 102 + i * 10, 44, 9, 9, this.createEventSender(val)));
			this.addElement(new CustomButton(this, 102 + i * 10, 68, 9, 9, this.createEventSender(-val)));
		}

		this.addElement(new CustomButton(this, 152, 44, 9, 9, this.createEventSender(1001)));
		this.addElement(new CustomButton(this, 152, 68, 9, 9, this.createEventSender(1002)));
		this.addElement(TextLabel.create(this, 105, 57, TextProvider.of(() -> container.base.getOutputMb() + Component.translatable("ic2r.generic.text.mb").getString()), 2157374, false));
		this.addElement(TextLabel.create(this, 145, 57, TextProvider.of(container.base::getModeGui), 2157374, false));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
