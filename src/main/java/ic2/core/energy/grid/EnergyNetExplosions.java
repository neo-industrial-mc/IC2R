package ic2.core.energy.grid;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.api.energy.tile.IOverloadHandler;
import ic2.core.Ic2Explosion;
import ic2.core.init.IC2Config;

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
		if (!IC2Config.misc.enableEnetExplosions.get())
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
				Ic2Explosion explosion = new Ic2Explosion(world, null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, power, 0.75F, Ic2Explosion.Type.Electrical);
				explosion.doExplosion();
			}
		}
	}

	public static boolean isOverVoltage(IEnergySink sink, double packetVoltage)
	{
		return packetVoltage > EnergyNet.instance.getPowerFromTier(sink.getSinkTier());
	}

	public static int getExplosionTier(double maxPower)
	{
		return EnergyNet.instance.getTierFromPower(maxPower);
	}
}