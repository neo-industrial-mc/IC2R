package me.halfcooler.ic2r.api.upgrade;

import java.util.Set;

public interface IUpgradableBlock
{
	double getEnergy();

	boolean useEnergy(double var1);

	Set<UpgradableProperty> getUpgradableProperties();
}
