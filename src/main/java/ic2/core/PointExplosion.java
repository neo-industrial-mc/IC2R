package ic2.core;

import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Small point explosion used by dynamite. Affects a 3×3×3 block area and damages nearby entities.
 */
public class PointExplosion extends Explosion
{
	private final Level world;
	private final Entity entity;
	private final LivingEntity igniter;
	private final float dropRate;
	private final int entityDamage;
	private final float power;
	private final double explosionX;
	private final double explosionY;
	private final double explosionZ;

	public PointExplosion(
		Level world,
		Entity entity,
		LivingEntity igniter,
		double x,
		double y,
		double z,
		float power,
		float dropRate,
		int entityDamage
	)
	{
		super(world, igniter, x, y, z, power, false, BlockInteraction.DESTROY);
		this.world = world;
		this.entity = entity;
		this.igniter = igniter;
		this.dropRate = dropRate;
		this.entityDamage = entityDamage;
		this.power = power;
		this.explosionX = x;
		this.explosionY = y;
		this.explosionZ = z;
	}

	public void doExplosion()
	{
		if (this.world.isClientSide)
		{
			return;
		}

		Vec3 position = new Vec3(this.explosionX, this.explosionY, this.explosionZ);
		if (!IC2.envProxy.announceExplosion(this.world, this.entity, position, this.power, this.igniter, 0, 1.0))
		{
			return;
		}

		int cx = Util.roundToNegInf(this.explosionX);
		int cy = Util.roundToNegInf(this.explosionY);
		int cz = Util.roundToNegInf(this.explosionZ);

		for (int x = cx - 1; x <= cx + 1; x++)
		{
			for (int y = cy - 1; y <= cy + 1; y++)
			{
				for (int z = cz - 1; z <= cz + 1; z++)
				{
					BlockPos pos = new BlockPos(x, y, z);
					BlockState state = this.world.getBlockState(pos);
					if (state.isAir())
					{
						continue;
					}

					float resistance = state.getBlock().getExplosionResistance(state, this.world, pos, this);
					if (resistance < this.power * 10.0F)
					{
						// Match vanilla/Forge explosion handling:
						// - TNT / other mods: wasExploded() primes the block instead of dropping it
						// - IC2 ITNT/Nuke: Ic2TileEntityBlock.onBlockExploded() arms the explosive TE
						// - Dynamite sticks: BlockDynamite.onBlockExploded() arms with a short fuse
						if (state.canDropFromExplosion(this.world, pos, this) && this.world.random.nextFloat() <= this.dropRate)
						{
							Block.dropResources(state, this.world, pos, this.world.getBlockEntity(pos), this.entity, net.minecraft.world.item.ItemStack.EMPTY);
						}

						state.onBlockExploded(this.world, pos, this);
					}
				}
			}
		}

		DamageSource damageSource = this.world.damageSources().explosion(this);
		AABB box = new AABB(
			this.explosionX - 2.0,
			this.explosionY - 2.0,
			this.explosionZ - 2.0,
			this.explosionX + 2.0,
			this.explosionY + 2.0,
			this.explosionZ + 2.0
		);

		for (Entity target : this.world.getEntities(this.igniter, box))
		{
			target.hurt(damageSource, this.entityDamage);
		}

		this.world.playSound(
			null,
			this.explosionX,
			this.explosionY,
			this.explosionZ,
			SoundEvents.GENERIC_EXPLODE,
			SoundSource.BLOCKS,
			4.0F,
			(1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F
		);

		if (this.world instanceof ServerLevel serverLevel)
		{
			serverLevel.sendParticles(
				ParticleTypes.EXPLOSION,
				this.explosionX,
				this.explosionY,
				this.explosionZ,
				1,
				0.0,
				0.0,
				0.0,
				0.0
			);
		}
	}
}
