package me.halfcooler.ic2r.core.gui;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.energy.profile.ElectricalDisplay;
import me.halfcooler.ic2r.core.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.network.chat.Component;

public class EnergyGauge extends Gauge<EnergyGauge>
{
	private final Energy energy;

	public EnergyGauge(Ic2rGui<?> gui, int x, int y, Ic2rTileEntity te, EnergyGauge.EnergyGaugeStyle style)
	{
		super(gui, x, y, style.properties);
		this.energy = te.getComponent(Energy.class);
	}

	public static EnergyGauge asBar(Ic2rGui<?> gui, int x, int y, Ic2rTileEntity te)
	{
		return new EnergyGauge(gui, x, y, te, EnergyGauge.EnergyGaugeStyle.Bar);
	}

	public static EnergyGauge asBolt(Ic2rGui<?> gui, int x, int y, Ic2rTileEntity te)
	{
		return new EnergyGauge(gui, x, y, te, EnergyGauge.EnergyGaugeStyle.Bolt);
	}

	@Override
	protected List<Component> getToolTip()
	{
		List<Component> ret = super.getToolTip();
		double amount = this.energy.getEnergy();
		double capacity = this.energy.getCapacity();
		ret.add(Component.literal(Util.toSiString(amount, 4) + "/" + Util.toSiString(capacity, 4) + " " + Component.translatable("ic2r.generic.text.EU").getString()));
		ret.add(ElectricalDisplay.formatVoltage(this.energy.getWorkingVoltage()));
		ret.add(ElectricalDisplay.formatPower(this.energy));
		return ret;
	}

	@Override
	protected double getRatio()
	{
		return this.energy.getFillRatio();
	}

	public enum EnergyGaugeStyle
	{
		Bar(new Gauge.GaugePropertyBuilder(132, 43, 24, 9, Gauge.GaugePropertyBuilder.GaugeOrientation.Right).withBackground(-4, -11, 32, 32, 128, 0).build()), Bolt(new Gauge.GaugePropertyBuilder(116, 65, 7, 13, Gauge.GaugePropertyBuilder.GaugeOrientation.Up).withBackground(-4, -1, 16, 16, 96, 64).build()), StirlingBar(new Gauge.GaugePropertyBuilder(176, 15, 58, 14, Gauge.GaugePropertyBuilder.GaugeOrientation.Right).withTexture(IC2R.getIdentifier("textures/gui/guistirlinggenerator.png")).withBackground(59, 33).build());

		private static final Map<String, EnergyGauge.EnergyGaugeStyle> map = getMap();
		public final String name = this.name().toLowerCase(Locale.ENGLISH);
		public final Gauge.GaugeProperties properties;

		EnergyGaugeStyle(Gauge.GaugeProperties properties)
		{
			this.properties = properties;
		}

		public static EnergyGauge.EnergyGaugeStyle get(String name)
		{
			return map.get(name);
		}

		private static Map<String, EnergyGauge.EnergyGaugeStyle> getMap()
		{
			EnergyGauge.EnergyGaugeStyle[] values = values();
			Map<String, EnergyGauge.EnergyGaugeStyle> ret = new HashMap<>(values.length);

			for (EnergyGauge.EnergyGaugeStyle style : values)
			{
				ret.put(style.name, style);
			}

			return ret;
		}
	}
}
