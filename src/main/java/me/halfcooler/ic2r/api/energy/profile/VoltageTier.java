package me.halfcooler.ic2r.api.energy.profile;

public enum VoltageTier
{
	ULV(8, 0),
	LV(32, 1),
	MV(128, 2),
	HV(512, 3),
	EV(2048, 4),
	IV(8192, 5);

	private static final VoltageTier[] BY_IC_TIER = values();
	public final int voltage;
	public final int icTier;

	VoltageTier(int voltage, int icTier)
	{
		this.voltage = voltage;
		this.icTier = icTier;
	}

	public int getVoltage()
	{
		return this.voltage;
	}

	public int getIcTier()
	{
		return this.icTier;
	}

	public String getTranslationKey()
	{
		return switch (this)
		{
			case ULV -> "ic2r.voltage.ulv";
			case LV -> "ic2r.voltage.lv";
			case MV -> "ic2r.voltage.mv";
			case HV -> "ic2r.voltage.hv";
			case EV -> "ic2r.voltage.ev";
			case IV -> "ic2r.voltage.iv";
		};
	}

	public static VoltageTier fromIcTier(int tier)
	{
		if (tier <= ULV.icTier)
		{
			return ULV;
		}

		if (tier >= IV.icTier)
		{
			return IV;
		}

		return BY_IC_TIER[tier];
	}

	public static VoltageTier fromPower(double power)
	{
		if (power <= 0.0)
		{
			return ULV;
		}

		return fromIcTier((int) Math.ceil(Math.log(power / 8.0) / Math.log(4.0)));
	}
}