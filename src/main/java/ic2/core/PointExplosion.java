package ic2.core;

import ic2.core.block.machine.tileentity.TileEntityExplosive;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Small point explosion used by dynamite. Affects a 3×3×3 block area and damages nearby entities.
 * <p>
 * Mirrors classic IC2 PointExplosion + vanilla finalize behavior:
 * explosives (ITNT/Nuke/TNT/placed dynamite) are primed/detonated rather than dropped as items.
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

		// Collect affected positions first (immutable) so chain reactions cannot skip cells.
		List<BlockPos> affected = new ArrayList<>(27);
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
						affected.add(pos.immutable());
					}
				}
			}
		}

		// Destroy / detonate blocks. Match Ic2Explosion + vanilla finalizeExplosion:
		// - IC2 ITNT/Nuke: arm via TileEntityExplosive (never drop as an item)
		// - TNT / placed dynamite / other wasExploded handlers: onBlockExploded primes them
		// - Normal blocks: optional drops, then remove
		for (BlockPos pos : affected)
		{
			BlockState state = this.world.getBlockState(pos);
			if (state.isAir())
			{
				continue;
			}

			BlockEntity blockEntity = state.hasBlockEntity() ? this.world.getBlockEntity(pos) : null;
			if (blockEntity instanceof TileEntityExplosive explosive)
			{
				// Explicit path: prime ITNT/Nuke. Do not drop resources or fall through to
				// super.onBlockExploded which would wipe the block without arming it when the
				// BE is missing or explode() returns early.
				explosive.onExploded(this);
				continue;
			}

			// Vanilla/Forge: canDropFromExplosion is false for TNT, placed dynamite, etc.
			// Only drop when the block is meant to become loot rather than chain-detonate.
			if (state.canDropFromExplosion(this.world, pos, this) && this.world.random.nextFloat() <= this.dropRate)
			{
				Block.dropResources(
					state,
					this.world,
					pos,
					blockEntity,
					this.entity,
					net.minecraft.world.item.ItemStack.EMPTY
				);
			}

			// Removes the block and calls wasExploded / BlockDynamite.onBlockExploded, etc.
			state.onBlockExploded(this.world, pos, this);
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

		// Exclude the igniter (classic behavior) and the dynamite entity if still present.
		for (Entity target : this.world.getEntities(this.igniter, box))
		{
			if (target == this.entity || target.isRemoved())
			{
				continue;
			}

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
