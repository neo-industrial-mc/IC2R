package ic2.core.entity;

import ic2.core.PointExplosion;
import ic2.core.ref.Ic2Entities;
import ic2.core.ref.Ic2Items;
import ic2.core.util.Util;
import ic2.core.util.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DynamiteEntity extends Entity implements ItemSupplier
{
	public boolean sticky;
	public BlockPos stickPos;
	public int fuse = 100;
	public LivingEntity owner;
	private boolean inGround;
	private int ticksInGround;

	public DynamiteEntity(EntityType<? extends DynamiteEntity> type, Level world)
	{
		super(type, world);
		this.sticky = type == Ic2Entities.STICKY_DYNAMITE;
	}

	public DynamiteEntity(Level world, double x, double y, double z)
	{
		this(Ic2Entities.DYNAMITE, world);
		this.setPos(x, y, z);
	}

	public DynamiteEntity(Level world, LivingEntity owner)
	{
		this(Ic2Entities.DYNAMITE, world);
		this.owner = owner;
		Vector3 eyePos = Util.getEyePosition(owner);
		this.setPos(
			eyePos.x - Math.cos(Math.toRadians(owner.getYRot())) * 0.16,
			eyePos.y - 0.1,
			eyePos.z - Math.sin(Math.toRadians(owner.getYRot())) * 0.16
		);
		this.setYRot(owner.getYRot());
		this.setXRot(owner.getXRot());
		double motionX = -Math.sin(Math.toRadians(this.getYRot())) * Math.cos(Math.toRadians(this.getXRot()));
		double motionZ = Math.cos(Math.toRadians(this.getYRot())) * Math.cos(Math.toRadians(this.getXRot()));
		double motionY = -Math.sin(Math.toRadians(this.getXRot()));
		this.shoot(motionX, motionY, motionZ, 1.0F, 1.0F);
	}

	protected void defineSynchedData()
	{
	}

	public void shoot(double x, double y, double z, float velocity, float inaccuracy)
	{
		double len = Math.sqrt(x * x + y * y + z * z);
		x /= len;
		y /= len;
		z /= len;
		x += this.random.nextGaussian() * 0.0075 * inaccuracy;
		y += this.random.nextGaussian() * 0.0075 * inaccuracy;
		z += this.random.nextGaussian() * 0.0075 * inaccuracy;
		x *= velocity;
		y *= velocity;
		z *= velocity;
		this.setDeltaMovement(x, y, z);
		double hLen = Math.sqrt(x * x + z * z);
		this.setYRot((float) (Math.atan2(x, z) * 180.0 / Math.PI));
		this.setXRot((float) (Math.atan2(y, hLen) * 180.0 / Math.PI));
		this.yRotO = this.getYRot();
		this.xRotO = this.getXRot();
		this.ticksInGround = 0;
	}

	public void tick()
	{
		super.tick();
		Level world = this.level();
		Vec3 motion = this.getDeltaMovement();

		if (this.xRotO == 0.0F && this.yRotO == 0.0F)
		{
			double hLen = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
			this.setYRot((float) Math.toDegrees(Math.atan2(motion.x, motion.z)));
			this.setXRot((float) Math.toDegrees(Math.atan2(motion.y, hLen)));
			this.yRotO = this.getYRot();
			this.xRotO = this.getXRot();
		}

		if (this.fuse-- <= 0)
		{
			this.discard();
			if (!world.isClientSide)
			{
				this.explode();
			}

			return;
		}

		if (this.fuse < 100 && this.fuse % 2 == 0)
		{
			world.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
		}

		if (this.inGround)
		{
			this.ticksInGround++;
			if (this.ticksInGround >= 200)
			{
				this.discard();
				return;
			}

			if (this.sticky)
			{
				this.fuse -= 3;
				this.setDeltaMovement(Vec3.ZERO);
				if (this.stickPos != null && !world.isEmptyBlock(this.stickPos))
				{
					return;
				}
			}
		}

		Vec3 start = this.position();
		Vec3 end = start.add(motion);
		BlockHitResult blockHit = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
		if (blockHit.getType() != HitResult.Type.MISS)
		{
			end = blockHit.getLocation();
		}

		if (blockHit.getType() != HitResult.Type.MISS)
		{
			float remainX = (float) (end.x - this.getX());
			float remainY = (float) (end.y - this.getY());
			float remainZ = (float) (end.z - this.getZ());
			double remDist = Math.sqrt(remainX * remainX + remainY * remainY + remainZ * remainZ);
			this.stickPos = blockHit.getBlockPos();
			if (remDist > 0.0)
			{
				this.setPos(
					this.getX() + remainX - remainX / remDist * 0.05,
					this.getY() + remainY - remainY / remDist * 0.05,
					this.getZ() + remainZ - remainZ / remDist * 0.05
				);
			}

			this.setDeltaMovement(
				motion.x * (0.75F - this.random.nextFloat()),
				motion.y * -0.3F,
				motion.z * (0.75F - this.random.nextFloat())
			);
			this.inGround = true;
		}
		else
		{
			this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
			this.inGround = false;
		}

		motion = this.getDeltaMovement();
		double hMotion = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
		this.setYRot((float) Math.toDegrees(Math.atan2(motion.x, motion.z)));
		this.setXRot((float) Math.toDegrees(Math.atan2(motion.y, hMotion)));

		while (this.getXRot() - this.xRotO < -180.0F)
		{
			this.xRotO -= 360.0F;
		}

		while (this.getXRot() - this.xRotO >= 180.0F)
		{
			this.xRotO += 360.0F;
		}

		while (this.getYRot() - this.yRotO < -180.0F)
		{
			this.yRotO -= 360.0F;
		}

		while (this.getYRot() - this.yRotO >= 180.0F)
		{
			this.yRotO += 360.0F;
		}

		this.setXRot(this.xRotO + (this.getXRot() - this.xRotO) * 0.2F);
		this.setYRot(this.yRotO + (this.getYRot() - this.yRotO) * 0.2F);

		float drag = 0.98F;
		float gravity = 0.04F;
		if (this.isInWater())
		{
			this.fuse += 2000;

			for (int i = 0; i < 4; i++)
			{
				float f6 = 0.25F;
				world.addParticle(
					ParticleTypes.BUBBLE,
					this.getX() - motion.x * f6,
					this.getY() - motion.y * f6,
					this.getZ() - motion.z * f6,
					motion.x,
					motion.y,
					motion.z
				);
			}

			drag = 0.75F;
		}

		this.setDeltaMovement(motion.x * drag, motion.y * drag - gravity, motion.z * drag);
		this.setPos(this.getX(), this.getY(), this.getZ());
	}

	protected void addAdditionalSaveData(CompoundTag tag)
	{
		tag.putBoolean("inGround", this.inGround);
		tag.putBoolean("sticky", this.sticky);
		tag.putInt("fuse", this.fuse);
		if (this.stickPos != null)
		{
			tag.putInt("stickX", this.stickPos.getX());
			tag.putInt("stickY", this.stickPos.getY());
			tag.putInt("stickZ", this.stickPos.getZ());
		}
	}

	protected void readAdditionalSaveData(CompoundTag tag)
	{
		this.inGround = tag.getBoolean("inGround");
		this.sticky = tag.getBoolean("sticky");
		if (tag.contains("fuse"))
		{
			this.fuse = tag.getInt("fuse");
		}

		if (tag.contains("stickX"))
		{
			this.stickPos = new BlockPos(tag.getInt("stickX"), tag.getInt("stickY"), tag.getInt("stickZ"));
		}
	}

	public void explode()
	{
		PointExplosion explosion = new PointExplosion(
			this.level(), this, this.owner, this.getX(), this.getY(), this.getZ(), 1.0F, 1.0F, 20
		);
		explosion.doExplosion();
	}

	@NotNull
	public ItemStack getItem()
	{
		return new ItemStack(this.sticky ? Ic2Items.DYNAMITE_STICKY : Ic2Items.DYNAMITE);
	}

	@NotNull
	public Packet<ClientGamePacketListener> getAddEntityPacket()
	{
		return new ClientboundAddEntityPacket(this);
	}
}
