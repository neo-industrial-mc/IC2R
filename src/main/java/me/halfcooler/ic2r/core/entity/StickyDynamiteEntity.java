package me.halfcooler.ic2r.core.entity;

import me.halfcooler.ic2r.core.ref.Ic2rEntities;
import me.halfcooler.ic2r.core.util.Util;
import me.halfcooler.ic2r.core.util.Vector3;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StickyDynamiteEntity extends DynamiteEntity
{
	public StickyDynamiteEntity(EntityType<? extends StickyDynamiteEntity> type, Level world)
	{
		super(type, world);
		this.sticky = true;
	}

	public StickyDynamiteEntity(Level world, LivingEntity owner)
	{
		super(Ic2rEntities.STICKY_DYNAMITE, world);
		this.sticky = true;
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

	public StickyDynamiteEntity(Level world, double x, double y, double z)
	{
		super(Ic2rEntities.STICKY_DYNAMITE, world);
		this.sticky = true;
		this.setPos(x, y, z);
	}
}
