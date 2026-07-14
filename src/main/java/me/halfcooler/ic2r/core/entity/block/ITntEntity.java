package me.halfcooler.ic2r.core.entity.block;

import me.halfcooler.ic2r.api.entity.block.ExplosiveEntity;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import me.halfcooler.ic2r.core.ref.Ic2rEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ITntEntity extends ExplosiveEntity
{
	public ITntEntity(Level world, double x, double y, double z)
	{
		super(Ic2rEntities.ITNT, world, x, y, z, 60, 5.5F, 0.9F, 0.3F, Ic2rBlocks.ITNT.defaultBlockState(), 0);
	}

	public ITntEntity(EntityType<? extends ITntEntity> type, Level world)
	{
		this(world, 0.0, 0.0, 0.0);
	}
}
