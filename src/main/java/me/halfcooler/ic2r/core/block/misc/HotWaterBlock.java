package me.halfcooler.ic2r.core.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.jetbrains.annotations.NotNull;

public class HotWaterBlock extends LiquidBlock
{
	public HotWaterBlock(FlowingFluid fluid, Properties properties)
	{
		super(fluid, properties);
	}

	@Override
	public void entityInside(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Entity entity)
	{
		if (!world.isClientSide && entity instanceof LivingEntity living)
		{
			living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
			world.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
		}
	}

	@Override
	public void onPlace(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston)
	{
		if (state.getValue(LiquidBlock.LEVEL) == 0)
		{
			world.scheduleTick(pos, this, 360);
		}
	}

	@Override
	public void tick(BlockState state, @NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource random)
	{
		if (state.getValue(LiquidBlock.LEVEL) == 0)
		{
			world.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
		}
	}
}
