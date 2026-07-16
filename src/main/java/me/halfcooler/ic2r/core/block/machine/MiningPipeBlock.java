package me.halfcooler.ic2r.core.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class MiningPipeBlock extends Block
{
    public static final com.mojang.serialization.MapCodec<MiningPipeBlock> CODEC = simpleCodec(MiningPipeBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.Block> codec() {
        return CODEC;
    }

	private static final VoxelShape SHAPE = Shapes.box(0.375, 0.0, 0.375, 0.625, 1.0, 0.625);

	public MiningPipeBlock(Properties settings)
	{
		super(settings);
	}

	public boolean useShapeForLightOcclusion(@NotNull BlockState state)
	{
		return true;
	}

	public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context)
	{
		return SHAPE;
	}
}
