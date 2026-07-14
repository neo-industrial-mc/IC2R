package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.entity.block.ExplosiveEntity;
import me.halfcooler.ic2r.core.entity.block.ITntEntity;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityITnt extends TileEntityExplosive
{
	public TileEntityITnt(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.ITNT, pos, state);
	}

	@Override
	protected boolean explodeOnRemoval()
	{
		return true;
	}

	@Override
	protected ExplosiveEntity getEntity(LivingEntity igniter)
	{
		return new ITntEntity(this.getLevel(), this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5);
	}
}
