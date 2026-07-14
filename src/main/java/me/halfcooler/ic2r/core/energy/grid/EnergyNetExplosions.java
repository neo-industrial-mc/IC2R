package me.halfcooler.ic2r.core.energy.grid;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.tile.IEnergySink;
import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;
import me.halfcooler.ic2r.api.energy.tile.IExplosionPowerOverride;
import me.halfcooler.ic2r.api.energy.tile.IOverloadHandler;
import me.halfcooler.ic2r.core.Ic2rExplosion;
import me.halfcooler.ic2r.core.init.IC2RConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EnergyNetExplosions
{
	private EnergyNetExplosions()
	{
	}

	public static void explodeTile(Level world, Tile tile, double maxPower)
	{
		if (!IC2RConfig.misc.enableEnetExplosions.get())
		{
			return;
		}

		int tier = getExplosionTier(maxPower);

		for (IEnergyTile subTile : tile.getSubTiles())
		{
			IEnergySink mainTile = (IEnergySink) tile.getMainTile();
			BlockPos pos = EnergyNet.instance.getPos(subTile);
			BlockEntity realTe = world.getBlockEntity(pos);
			if (!(mainTile instanceof IOverloadHandler handler && handler.onOverload(tier)) && !(realTe instanceof IOverloadHandler handler2 && handler2.onOverload(tier)))
			{
				float power = 2.5F;
				if (mainTile instanceof IExplosionPowerOverride override)
				{
					if (!override.shouldExplode())
					{
						continue;
					}

					power = override.getExplosionPower(tier, power);
				} else if (realTe instanceof IExplosionPowerOverride override)
				{
					if (!override.shouldExplode())
					{
						continue;
					}

					power = override.getExplosionPower(tier, power);
				}

				world.removeBlock(pos, false);
				Ic2rExplosion explosion = new Ic2rExplosion(world, null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, power, 0.75F, Ic2rExplosion.Type.Electrical);
				explosion.doExplosion();
			}
		}
	}

	public static boolean isOverVoltage(IEnergySink sink, double packetVoltage)
	{
		return EnergyTransferMath.icSinkOverVoltage(packetVoltage, EnergyNet.instance.getPowerFromTier(sink.getSinkTier()));
	}

	public static int getExplosionTier(double maxPower)
	{
		return EnergyTransferMath.icTierFromPower(maxPower);
	}
}