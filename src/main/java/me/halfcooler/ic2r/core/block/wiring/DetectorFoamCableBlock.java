package me.halfcooler.ic2r.core.block.wiring;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DetectorFoamCableBlock extends AbstractDetectorCableBlock
{
    public static final com.mojang.serialization.MapCodec<DetectorFoamCableBlock> CODEC = simpleCodec(DetectorFoamCableBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.Block> codec() {
        return CODEC;
    }

	protected DetectorFoamCableBlock(Properties settings)
	{
		super(settings);
	}

	public static DetectorFoamCableBlock create(Properties settings)
	{
		prepareCreate(CableType.detector, 0);
		return new DetectorFoamCableBlock(settings.randomTicks());
	}

	@Override
	public boolean isFoam()
	{
		return true;
	}

	@Override
	public boolean isHardFoam(BlockState state)
	{
		return state.getValue(foamProperty).isHard();
	}
}
