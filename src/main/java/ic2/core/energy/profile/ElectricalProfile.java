package ic2.core.energy.profile;

import ic2.api.energy.profile.VoltageTier;

public class ElectricalProfile
{
	private VoltageTier workingVoltage;
	private int recipePower;

	public ElectricalProfile(VoltageTier workingVoltage)
	{
		this.workingVoltage = workingVoltage;
	}

	public VoltageTier getWorkingVoltage()
	{
		return this.workingVoltage;
	}

	public void setWorkingVoltage(VoltageTier workingVoltage)
	{
		this.workingVoltage = workingVoltage;
	}

	public int getRecipePower()
	{
		return this.recipePower;
	}

	public void setRecipePower(int recipePower)
	{
		this.recipePower = Math.max(0, recipePower);
	}

	public double getDisplayCurrent()
	{
		int voltage = this.workingVoltage.getVoltage();
		return voltage > 0 ? (double) this.recipePower / voltage : 0.0;
	}

	public int getWorkingCurrent()
	{
		if (this.recipePower <= 0)
		{
			return 0;
		}

		int voltage = this.workingVoltage.getVoltage();
		return voltage > 0 ? Math.max(1, (int) Math.ceil((double) this.recipePower / voltage)) : 0;
	}

	public int getMaxSinkAmperage()
	{
		if (this.recipePower <= 0)
		{
			return 1;
		}

		int voltage = this.workingVoltage.getVoltage();
		return voltage > 0 ? (int) Math.floor(2.0 * this.recipePower / voltage) + 1 : 1;
	}
}