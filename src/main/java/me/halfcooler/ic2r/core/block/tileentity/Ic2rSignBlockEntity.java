package me.halfcooler.ic2r.core.block.tileentity;

import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class Ic2rSignBlockEntity extends SignBlockEntity
{
	public Ic2rSignBlockEntity(BlockPos pos, BlockState state)
	{
		super(pos, state);
	}

	public BlockEntityType<?> getType()
	{
		return Ic2rBlockEntities.SIGN;
	}
}
