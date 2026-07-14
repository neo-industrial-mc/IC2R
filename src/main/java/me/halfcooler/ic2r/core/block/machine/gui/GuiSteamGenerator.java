package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerSteamGenerator;
import me.halfcooler.ic2r.core.gui.CustomButton;
import me.halfcooler.ic2r.core.gui.Gauge;
import me.halfcooler.ic2r.core.gui.LinkedGauge;
import me.halfcooler.ic2r.core.gui.MouseButton;
import me.halfcooler.ic2r.core.gui.TankGauge;
import me.halfcooler.ic2r.core.gui.TextLabel;
import me.halfcooler.ic2r.core.gui.dynamic.TextProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiSteamGenerator extends Ic2rGui<ContainerSteamGenerator>
{
	private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guisteamgenerator.png");

	public GuiSteamGenerator(ContainerSteamGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 220);
		this.addElement(TankGauge.createPlain(this, 10, 155, 75, 47, container.base.waterTank));
		this.addElement(new LinkedGauge(this, 13, 70, container.base, "heat", Gauge.GaugeStyle.HeatSteamGenerator).withTooltip(() -> Component.translatable("ic2r.steam_generator.gui.systemheat", GuiSteamGenerator.this.menu.base.getSystemHeat()).getString()));
		this.addElement(new LinkedGauge(this, 155, 61, container.base, "calcification", Gauge.GaugeStyle.CalcificationSteamGenerator).withTooltip(() -> Component.translatable("ic2r.steam_generator.gui.calcification", GuiSteamGenerator.this.menu.base.getCalcification()).getString() + "%"));
		this.addElement(TextLabel.create(this, 91, 172, 59, 13, TextProvider.of(() -> GuiSteamGenerator.this.menu.base.getInputMB() + Component.translatable("ic2r.generic.text.mb").getString() + Component.translatable("ic2r.generic.text.tick").getString()), 2157374, false, true, true).withTooltip("ic2r.steam_generator.gui.info.waterinput"));
		this.addElement(TextLabel.create(this, 31, 133, 111, 13, TextProvider.of(() -> Component.translatable("ic2r.steam_generator.gui.heatInput", GuiSteamGenerator.this.menu.base.getHeatInput()).getString()), 2157374, false, 4, 0, false, true).withTooltip("ic2r.steam_generator.gui.info.heatinput"));
		this.addElement(TextLabel.create(this, 22, 35, 42, 13, TextProvider.of(() -> Component.translatable("ic2r.steam_generator.gui.pressurevalve", GuiSteamGenerator.this.menu.base.getPressure()).getString()), 2157374, false, 4, 0, false, true).withTooltip("ic2r.steam_generator.gui.info.pressvalve"));
		this.addElement(TextLabel.create(this, 66, 25, 81, 13, TextProvider.of(() -> GuiSteamGenerator.this.menu.base.getOutputMB() + Component.translatable("ic2r.generic.text.mb").getString() + Component.translatable("ic2r.generic.text.tick").getString()), 2157374, false, 4, 0, false, true).withTooltip("ic2r.steam_generator.gui.info.fluidoutput"));
		this.addElement(TextLabel.create(this, 66, 45, 100, 13, TextProvider.of(() -> Component.translatable(GuiSteamGenerator.this.menu.base.getOutputFluidName()).getString()), 2157374, false, 4, 0, false, true));

		for (byte i = 0; i < 4; i++)
		{
			int event = (int) Math.pow(10.0, 3 - i);
			int xShift = 10 * i;
			this.addElement(new GuiSteamGenerator.SteamBoilerButton(92 + xShift, 186, 9, 9, -event));
			this.addElement(new GuiSteamGenerator.SteamBoilerButton(92 + xShift, 162, 9, 9, event));
			if (i != 3)
			{
				event = (int) Math.pow(10.0, 2 - i);
				this.addElement(new GuiSteamGenerator.SteamBoilerButton(23 + xShift, 49, 9, 9, -(2000 + event)));
				this.addElement(new GuiSteamGenerator.SteamBoilerButton(23 + xShift, 25, 9, 9, 2000 + event));
			}
		}
	}

	@Override
	public ResourceLocation getTextureLocation()
	{
		return BACKGROUND;
	}

	private class SteamBoilerButton extends CustomButton
	{
		public SteamBoilerButton(int x, int y, int width, int height, final int event)
		{
			super(GuiSteamGenerator.this, x, y, width, height, button ->
			{
				if (button == MouseButton.left)
				{
					IC2R.network.get(false).initiateClientTileEntityEvent(GuiSteamGenerator.this.menu.base, event);
				}
			});
		}
	}
}
