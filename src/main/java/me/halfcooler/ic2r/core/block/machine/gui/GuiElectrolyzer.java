package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerElectrolyzer;
import me.halfcooler.ic2r.core.gui.CustomGauge;
import me.halfcooler.ic2r.core.gui.ElectrolyzerTank;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.gui.Gauge;
import me.halfcooler.ic2r.core.gui.GuiElement;
import me.halfcooler.ic2r.core.gui.RecipeButton;
import me.halfcooler.ic2r.core.gui.TankFluidSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiElectrolyzer extends Ic2rGui<ContainerElectrolyzer>
{
	private static final ResourceLocation background = IC2R.getIdentifier("textures/gui/guielectrolyzer.png");

	public GuiElectrolyzer(ContainerElectrolyzer container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(EnergyGauge.asBolt(this, 12, 44, container.base));
		int controllerX = 36;
		int controllerY = 16;
		this.addElement(TankFluidSlot.createFluidSlot(this, 78, 16, container.base.getInput()));
		ElectrolyzerTank[] tanks = new ElectrolyzerTank[5];

		for (int i = 0; i < tanks.length; i++)
		{
			ElectrolyzerTank controller = tanks[i] = new ElectrolyzerTank(this, 36 + 21 * i, 61, i);
			this.addElement(controller);
		}

		GuiElement<?> last = null;

		for (GuiElectrolyzer.ElectrolyzerGauges gauge : GuiElectrolyzer.ElectrolyzerGauges.values())
		{
			int id = gauge.ordinal();
			last = new CustomGauge(this, 36 + gauge.offset, 36, container.base, gauge.properties).withEnableHandler(() -> tanks[id].isActive());
			this.addElement(last);
		}

		if (RecipeButton.canUse())
		{
			assert last != null;
			this.addElement(new RecipeButton(last, new String[] { "electrolyzer" }));
		}
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}

	public enum ElectrolyzerGauges
	{
		ONE_TANK(new Gauge.GaugePropertyBuilder(57, 232, 12, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down).build(), 48),
		TWO_TANK(new Gauge.GaugePropertyBuilder(1, 232, 54, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down).build(), 24),
		THREE_TANK(new Gauge.GaugePropertyBuilder(41, 159, 54, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down).build(), 24),
		FOUR_TANK(new Gauge.GaugePropertyBuilder(1, 208, 96, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down).build(), 3),
		FIVE_TANK(new Gauge.GaugePropertyBuilder(1, 184, 96, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down).build(), 3);

		public final int offset;
		public final Gauge.GaugeProperties properties;

		ElectrolyzerGauges(Gauge.GaugeProperties properties, int offset)
		{
			this.properties = properties;
			this.offset = offset;
		}
	}
}
