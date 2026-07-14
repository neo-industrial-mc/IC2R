package me.halfcooler.ic2r.core.ref;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.entity.DynamiteEntity;
import me.halfcooler.ic2r.core.entity.LaserBulletEntity;
import me.halfcooler.ic2r.core.entity.StickyDynamiteEntity;
import me.halfcooler.ic2r.core.entity.block.ITntEntity;
import me.halfcooler.ic2r.core.entity.block.NukeEntity;
import me.halfcooler.ic2r.core.entity.boat.CarbonBoatEntity;
import me.halfcooler.ic2r.core.entity.boat.ElectricBoatEntity;
import me.halfcooler.ic2r.core.entity.boat.RubberBoatEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public final class Ic2rEntities
{
	public static final EntityType<LaserBulletEntity> LASER_BULLET = register("laser_bullet", Builder.<LaserBulletEntity>of(LaserBulletEntity::new, MobCategory.MISC).fireImmune().sized(0.8F, 0.8F).clientTrackingRange(8).updateInterval(8));	public static final EntityType<ITntEntity> ITNT = register("itnt", Builder.<ITntEntity>of(ITntEntity::new, MobCategory.MISC).fireImmune().sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(10));
	public static final EntityType<RubberBoatEntity> RUBBER_BOAT = register("rubber_boat", Builder.<RubberBoatEntity>of(RubberBoatEntity::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10));	public static final EntityType<NukeEntity> NUKE = register("nuke", Builder.<NukeEntity>of(NukeEntity::new, MobCategory.MISC).fireImmune().sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(10));
	public static final EntityType<ElectricBoatEntity> ELECTRIC_BOAT = register("electric_boat", Builder.<ElectricBoatEntity>of(ElectricBoatEntity::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10));
	public static final EntityType<CarbonBoatEntity> CARBON_BOAT = register("carbon_boat", Builder.<CarbonBoatEntity>of(CarbonBoatEntity::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10));
	public static final EntityType<DynamiteEntity> DYNAMITE = register("dynamite", Builder.<DynamiteEntity>of(DynamiteEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(5));
	public static final EntityType<StickyDynamiteEntity> STICKY_DYNAMITE = register("sticky_dynamite", Builder.<StickyDynamiteEntity>of(StickyDynamiteEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(5));

	public static void init()
	{
	}

	private static <T extends Entity> EntityType<T> register(String name, Builder<T> builder)
	{
		EntityType<T> ret = builder.build(IC2R.getIdentifier(name).toString());
		IC2R.envProxy.registerEntity(IC2R.getIdentifier(name), ret);
		return ret;
	}




}
