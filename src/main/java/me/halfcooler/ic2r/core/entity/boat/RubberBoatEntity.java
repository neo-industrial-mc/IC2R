package me.halfcooler.ic2r.core.entity.boat;

import me.halfcooler.ic2r.api.entity.boat.AbstractBoatEntity;
import me.halfcooler.ic2r.api.entity.boat.BoatType;
import me.halfcooler.ic2r.core.ref.Ic2rBoatTypes;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class RubberBoatEntity extends AbstractBoatEntity
{
	public RubberBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level world)
	{
		super(entityType, world);
	}

	@SuppressWarnings("unused")
	public RubberBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level world, double x, double y, double z)
	{
		/* This method are used by reflection. */
		super(entityType, world, x, y, z);
	}

	@Override
	public @NotNull Item getDropItem()
	{
		return Ic2rItems.BROKEN_RUBBER_BOAT;
	}

	@Override
	public BoatType getOverrideBoatType()
	{
		return Ic2rBoatTypes.RUBBER;
	}

	@Override
	protected float getBlockSpeedFactor()
	{
		return super.getBlockSpeedFactor() * 1.05F;
	}

	@Override
	public void tick()
	{
		super.tick();
		if (!this.level().isClientSide && this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA))
		{
			this.hurt(this.damageSources().lava(), Float.MAX_VALUE);
		}
	}
}
