package me.halfcooler.ic2r.core.block.inherit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class Ic2rGlassBlock extends TransparentBlock
{
    public static final com.mojang.serialization.MapCodec<Ic2rGlassBlock> CODEC = simpleCodec(Ic2rGlassBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.TransparentBlock> codec() {
        return CODEC;
    }

	public Ic2rGlassBlock(Properties settings)
	{
		super(settings);
	}

	public @NotNull VoxelShape getBlockSupportShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos)
	{
		return Shapes.empty();
	}
}
