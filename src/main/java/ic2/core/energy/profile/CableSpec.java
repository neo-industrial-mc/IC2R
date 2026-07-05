package ic2.core.energy.profile;

import ic2.api.energy.profile.ICableSpec;
import ic2.api.energy.profile.VoltageTier;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.info.ILocatable;
import ic2.core.block.wiring.AbstractCableBlock;
import ic2.core.block.wiring.CableType;

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