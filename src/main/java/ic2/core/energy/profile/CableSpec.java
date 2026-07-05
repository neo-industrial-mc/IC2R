package ic2.core.energy.profile;

import ic2.api.energy.profile.ICableSpec;
import ic2.api.energy.profile.VoltageTier;
import ic2.core.block.wiring.CableType;

public final class CableSpec implements ICableSpec
{
	private static final int DETECTOR_SPLITTER_MAX_AMPERAGE = 64;
	private final CableType type;
	private final VoltageTier maxVoltage;
	private final int maxAmperage;
	private final int lossPerMeterPerAmp;

	private CableSpec(CableType type, VoltageTier maxVoltage, int maxAmperage, int lossPerMeterPerAmp)
	{
		this.type = type;
		this.maxVoltage = maxVoltage;
		this.maxAmperage = maxAmperage;
		this.lossPerMeterPerAmp = lossPerMeterPerAmp;
	}

	public CableType getCableType()
	{
		return this.type;
	}

	@Override
	public VoltageTier getMaxVoltage()
	{
		return this.maxVoltage;
	}

	@Override
	public int getMaxAmperage()
	{
		return this.maxAmperage;
	}

	@Override
	public int getLossPerMeterPerAmp()
	{
		return this.lossPerMeterPerAmp;
	}

	public static CableSpec forType(CableType type)
	{
		switch (type)
		{
			case tin:
				return new CableSpec(type, VoltageTier.LV, 1, 1);
			case copper:
				return new CableSpec(type, VoltageTier.MV, 2, 1);
			case gold:
				return new CableSpec(type, VoltageTier.HV, 3, 2);
			case iron:
				return new CableSpec(type, VoltageTier.EV, 4, 3);
			case glass:
				return new CableSpec(type, VoltageTier.IV, 8, 0);
			case detector:
			case splitter:
				return new CableSpec(type, VoltageTier.IV, DETECTOR_SPLITTER_MAX_AMPERAGE, 0);
			default:
				throw new IllegalArgumentException("unknown cable type: " + type);
		}
	}
}