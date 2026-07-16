package me.halfcooler.ic2r.core.block.steam;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class BlockRefractoryBricks extends Block
{
    public static final com.mojang.serialization.MapCodec<BlockRefractoryBricks> CODEC = simpleCodec(BlockRefractoryBricks::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.Block> codec() {
        return CODEC;
    }


    public BlockRefractoryBricks(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        super(properties);
    }

	public BlockRefractoryBricks()
	{
		super(Properties.of()
			.strength(2.0F, 10.0F)
			.requiresCorrectToolForDrops()
			.sound(SoundType.STONE));
	}
}
