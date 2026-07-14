package me.halfcooler.ic2r.core.block.machine.tileentity;

import org.joml.Vector3f;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.Ic2rDamageSource;
import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.comp.Redstone;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.item.armor.ItemArmorHazmat;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;

import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class TileEntityTesla extends Ic2rTileEntity implements ServerTicker
{
	private static final DustParticleOptions effect = new DustParticleOptions(new Vector3f(0.1F, 0.1F, 1.0F), 1.0F);
	protected final Redstone redstone;
	protected final Energy energy;
	private int ticker = IC2R.random.nextInt(32);

	public TileEntityTesla(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.TESLA_COIL, pos, state);
		this.redstone = this.addComponent(new Redstone(this));
		this.energy = this.addComponent(Energy.asBasicSink(this, 10000.0, 2));
		this.energy.syncConsumerProfile(1);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.redstone.hasRedstoneInput())
		{
			if (this.energy.useEnergy(1.0) && ++this.ticker % 32 == 0)
			{
				int damage = (int) this.energy.getEnergy() / 400;
				if (damage > 0 && this.shock(damage))
				{
					System.out.println(damage);
					this.energy.useEnergy(damage * 400);
				}
			}
		}
	}

	protected boolean shock(int totalDamage)
	{
		int r = 4;
		Level world = this.getLevel();
		if (world == null)
		{
			return false;
		}

		List<LivingEntity> entities = world.getEntitiesOfClass(
			LivingEntity.class,
			new AABB(
				this.worldPosition.getX() - 4,
				this.worldPosition.getY() - 4,
				this.worldPosition.getZ() - 4,
				this.worldPosition.getX() + 4 + 1,
				this.worldPosition.getY() + 4 + 1,
				this.worldPosition.getZ() + 4 + 1
			),
			EntitySelector.NO_CREATIVE_OR_SPECTATOR
		);
		if (entities.isEmpty())
		{
			return false;
		}

		boolean isShocked = false;
		int damage = totalDamage / entities.size();

		for (LivingEntity entity : entities)
		{
			if (!ItemArmorHazmat.hasCompleteHazmat(entity) && entity.hurt(Ic2rDamageSource.electricity, damage))
			{
				if (world instanceof ServerLevel worldServer)
				{
					RandomSource rnd = worldServer.random;

					for (int i = 0; i < damage; i++)
					{
						worldServer.addParticle(
							effect,
							entity.getX() + rnd.nextFloat() - 0.5,
							entity.getY() + rnd.nextFloat() * 2.0F - 1.0,
							entity.getZ() + rnd.nextFloat() - 0.5,
							0.0,
							0.0,
							0.0
						);
					}
				}

				isShocked = true;
			}
		}

		return isShocked;
	}
}
