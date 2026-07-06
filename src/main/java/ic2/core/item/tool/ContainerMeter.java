package ic2.core.item.tool;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.profile.IElectricalNode;
import ic2.api.energy.tile.IEnergyTile;

import ic2.api.network.ClientModifiable;
import ic2.core.IC2;
import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.ref.Ic2ScreenHandlers;

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
		super(Ic2ScreenHandlers.METER, syncId, inventory);
	}

	@Override
	public void broadcastChanges()
	{
		super.broadcastChanges();
		if (!IC2.sideProxy.isSimulating() || this.uut == null)
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
			IC2.network.get(true).sendContainerFields(this, "resultAvg", "resultMin", "resultMax", "resultCount");
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
		IC2.network.get(false).sendContainerField(this, "mode");
		this.reset();
	}

	public void reset()
	{
		if (IC2.sideProxy.isSimulating())
		{
			this.resultAvg = 0;
			this.resultMin = 0;
			this.resultMax = 0;
			this.resultCount = 0;
			IC2.network.get(true).sendContainerFields(this, "resultAvg", "resultMin", "resultMax", "resultCount");
		} else
		{
			IC2.network.get(false).sendContainerEvent(this, "reset");
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