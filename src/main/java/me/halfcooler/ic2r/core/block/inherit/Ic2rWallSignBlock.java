package me.halfcooler.ic2r.core.block.inherit;

import me.halfcooler.ic2r.core.block.tileentity.Ic2rSignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.WoodType;

public class Ic2rWallSignBlock extends WallSignBlock
{
	public Ic2rWallSignBlock(Properties settings, WoodType signType)
	{
		super(settings, signType);
	}

	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new Ic2rSignBlockEntity(pos, state);
	}
}
