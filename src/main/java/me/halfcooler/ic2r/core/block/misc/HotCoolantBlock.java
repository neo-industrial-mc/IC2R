package me.halfcooler.ic2r.core.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.jetbrains.annotations.NotNull;

public class HotCoolantBlock extends LiquidBlock
{
	public HotCoolantBlock(FlowingFluid fluid, Properties properties)
	{
		super(fluid, properties);
	}

	@Override
	public void entityInside(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Entity entity)
	{
		if (!world.isClientSide)
		{
			entity.setSecondsOnFire(30);
		}
	}
}
