package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.energy.EnergyBridgeMath;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * One-way EU → FE view of an {@link Energy} buffer.
 * <p>
 * External FE systems may <strong>extract</strong> only; {@link #receiveEnergy} always returns 0
 * so FE cannot flow back into the IC EnergyNet.
 */
public final class EuToFeEnergyStorage implements IEnergyStorage
{
	private final Energy energy;
	private final double fePerEu;

	public EuToFeEnergyStorage(Energy energy)
	{
		this(energy, EnergyBridgeMath.DEFAULT_FE_PER_EU);
	}

	public EuToFeEnergyStorage(Energy energy, double fePerEu)
	{
		this.energy = energy;
		this.fePerEu = fePerEu;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate)
	{
		// Hard rule: no FE → EU path.
		return 0;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate)
	{
		if (maxExtract <= 0 || !canExtract())
		{
			return 0;
		}

		int available = getEnergyStored();
		if (available <= 0)
		{
			return 0;
		}

		int feOut = Math.min(maxExtract, available);
		if (feOut <= 0)
		{
			return 0;
		}

		if (!simulate)
		{
			double euCost = EnergyBridgeMath.feToEu(feOut, this.fePerEu);
			double used = this.energy.useEnergy(euCost, false);
			// If buffer shrank under us, only report what was actually paid in FE terms.
			if (used + 1.0e-9 < euCost)
			{
				feOut = EnergyBridgeMath.clampToIntEnergy(EnergyBridgeMath.euToFeFloor(used, this.fePerEu));
			}
		}

		return feOut;
	}

	@Override
	public int getEnergyStored()
	{
		return EnergyBridgeMath.clampToIntEnergy(
			EnergyBridgeMath.euToFeFloor(this.energy.getEnergy(), this.fePerEu)
		);
	}

	@Override
	public int getMaxEnergyStored()
	{
		return EnergyBridgeMath.clampToIntEnergy(
			EnergyBridgeMath.euToFeFloor(this.energy.getCapacity(), this.fePerEu)
		);
	}

	@Override
	public boolean canExtract()
	{
		return this.energy.getEnergy() > 0.0;
	}

	@Override
	public boolean canReceive()
	{
		return false;
	}
}
