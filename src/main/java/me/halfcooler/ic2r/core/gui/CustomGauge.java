package me.halfcooler.ic2r.core.gui;

import me.halfcooler.ic2r.core.Ic2rGui;

public class CustomGauge extends Gauge<CustomGauge>
{
	private final CustomGauge.IGaugeRatioProvider provider;

	public CustomGauge(Ic2rGui<?> gui, int x, int y, CustomGauge.IGaugeRatioProvider provider, Gauge.GaugeProperties properties)
	{
		super(gui, properties.attributes(createAttributes(gui, x, y)));
		this.provider = provider;
	}

	public static CustomGauge asFuel(Ic2rGui<?> gui, int x, int y, CustomGauge.IGaugeRatioProvider provider)
	{
		return new CustomGauge(gui, x, y, provider, Gauge.GaugeStyle.Fuel.properties);
	}

	public static CustomGauge create(Ic2rGui<?> gui, int x, int y, CustomGauge.IGaugeRatioProvider provider, Gauge.GaugeStyle style)
	{
		return new CustomGauge(gui, x, y, provider, style.properties);
	}

	@Override
	protected double getRatio()
	{
		return this.provider.getRatio();
	}

	public interface IGaugeRatioProvider
	{
		double getRatio();
	}
}
