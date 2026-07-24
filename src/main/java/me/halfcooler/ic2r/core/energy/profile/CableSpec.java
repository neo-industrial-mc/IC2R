package me.halfcooler.ic2r.core.energy.profile;

import me.halfcooler.ic2r.api.energy.profile.ICableSpec;
import me.halfcooler.ic2r.api.energy.profile.VoltageTier;
import me.halfcooler.ic2r.api.energy.tile.IEnergyConductor;
import me.halfcooler.ic2r.api.info.ILocatable;
import me.halfcooler.ic2r.core.block.wiring.AbstractCableBlock;
import me.halfcooler.ic2r.core.block.wiring.CableType;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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
		return switch (type)
		{
			case tin -> new CableSpec(type, VoltageTier.LV, 1, 1);
			case copper -> new CableSpec(type, VoltageTier.MV, 2, 1);
			case gold -> new CableSpec(type, VoltageTier.HV, 3, 2);
			case iron -> new CableSpec(type, VoltageTier.EV, 4, 3);
			case glass -> new CableSpec(type, VoltageTier.IV, 8, 0);
			case detector, splitter -> new CableSpec(type, VoltageTier.IV, DETECTOR_SPLITTER_MAX_AMPERAGE, 0);
		};
	}

	public static CableSpec fromConductor(IEnergyConductor conductor)
	{
		CableType type = resolveCableType(conductor);
		CableSpec spec = forType(type);
		if (resolveInsulation(conductor) == 0 && spec.lossPerMeterPerAmp > 0)
		{
			return new CableSpec(type, spec.maxVoltage, spec.maxAmperage, spec.lossPerMeterPerAmp * 2);
		}

		return spec;
	}

	private static CableType resolveCableType(IEnergyConductor conductor)
	{
		int capacity = (int) Math.round(conductor.getConductorBreakdownEnergy() - 1.0);
		CableType fallback = null;
		for (CableType type : CableType.values)
		{
			if (type.capacity == capacity)
			{
				if (fallback == null || type.loss == conductor.getConductionLoss())
				{
					fallback = type;
				}
			}
		}

		if (fallback != null)
		{
			return fallback;
		}

		if (conductor instanceof ILocatable locatable)
		{
			BlockState state = locatable.getWorldObj().getBlockState(locatable.getPosition());
			Block block = state.getBlock();
			if (block instanceof AbstractCableBlock cableBlock)
			{
				return cableBlock.type;
			}
		}

		throw new IllegalArgumentException("unknown conductor capacity: " + capacity);
	}

	private static int resolveInsulation(IEnergyConductor conductor)
	{
		if (conductor instanceof ILocatable locatable)
		{
			Level world = locatable.getWorldObj();
			if (world != null && world.getServer() != null && world.getServer().isSameThread())
			{
				BlockState state = world.getBlockState(locatable.getPosition());
				Block block = state.getBlock();
				if (block instanceof AbstractCableBlock cableBlock)
				{
					return cableBlock.getCableInsulation();
				}
			}
		}

		return 1;
	}
}