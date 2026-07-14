package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.NodeStats;
import me.halfcooler.ic2r.api.energy.profile.IElectricalNode;
import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;

import me.halfcooler.ic2r.api.network.ClientModifiable;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.item.ContainerHandHeldInventory;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;

public class ContainerMeter extends ContainerHandHeldInventory<HandHeldMeter>
{
	private static final int SCALE = 1000;

	private IEnergyTile uut;
	private int resultAvg;
	private int resultMin;
	private int resultMax;
	private int resultCount;
	@ClientModifiable
	private ContainerMeter.Mode mode = ContainerMeter.Mode.EnergyIn;

	public ContainerMeter(int syncId, HandHeldMeter inventory)
	{
		super(Ic2rScreenHandlers.METER, syncId, inventory);
	}

	@Override
	public void broadcastChanges()
	{
		super.broadcastChanges();
		if (!IC2R.sideProxy.isSimulating() || this.uut == null)
		{
			return;
		}

		NodeStats stats = EnergyNet.instance.getNodeStats(this.uut);
		if (stats == null)
		{
			this.base.closeGUI();
		} else
		{
			double result = switch (this.mode)
			{
				case EnergyIn -> stats.getEnergyIn();
				case EnergyOut -> stats.getEnergyOut();
				case EnergyGain -> stats.getEnergyIn() - stats.getEnergyOut();
				case Voltage -> stats.getVoltage();
				case Amperage -> this.getAmperageReading();
			};
			int scaled = (int) Math.round(result * SCALE);

			if (this.resultCount == 0)
			{
				this.resultAvg = this.resultMin = this.resultMax = scaled;
			} else
			{
				if (scaled < this.resultMin)
				{
					this.resultMin = scaled;
				}

				if (scaled > this.resultMax)
				{
					this.resultMax = scaled;
				}

				this.resultAvg = (int) (((long) this.resultAvg * this.resultCount + scaled) / (this.resultCount + 1));
			}

			this.resultCount++;
			IC2R.network.get(true).sendContainerFields(this, "resultAvg", "resultMin", "resultMax", "resultCount");
		}
	}

	public double getResultAvg()
	{
		return this.resultAvg / (double) SCALE;
	}

	public double getResultMin()
	{
		return this.resultMin / (double) SCALE;
	}

	public double getResultMax()
	{
		return this.resultMax / (double) SCALE;
	}

	public int getResultCount()
	{
		return this.resultCount;
	}

	public ContainerMeter.Mode getMode()
	{
		return this.mode;
	}

	public void setMode(ContainerMeter.Mode mode)
	{
		this.mode = mode;
		IC2R.network.get(false).sendContainerField(this, "mode");
		this.reset();
	}

	public void reset()
	{
		if (IC2R.sideProxy.isSimulating())
		{
			this.resultAvg = 0;
			this.resultMin = 0;
			this.resultMax = 0;
			this.resultCount = 0;
			IC2R.network.get(true).sendContainerFields(this, "resultAvg", "resultMin", "resultMax", "resultCount");
		} else
		{
			IC2R.network.get(false).sendContainerEvent(this, "reset");
		}
	}

	public void setUut(IEnergyTile uut)
	{
		if (this.uut == null)
		{
			this.uut = uut;
		}
	}

	@Override
	public void onContainerEvent(String event)
	{
		super.onContainerEvent(event);
		if ("reset".equals(event))
		{
			this.reset();
		}
	}

	private double getAmperageReading()
	{
		if (this.uut instanceof IElectricalNode node)
		{
			int workingCurrent = node.getWorkingCurrent();
			return workingCurrent > 0 ? workingCurrent : node.getAverageCurrent();
		}

		return 0.0;
	}

	public enum Mode
	{
		EnergyIn,
		EnergyOut,
		EnergyGain,
		Voltage,
		Amperage
	}
}