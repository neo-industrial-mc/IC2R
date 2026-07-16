package me.halfcooler.ic2r.core.block.wiring;

import net.minecraft.world.level.block.state.BlockState;

public class SplitterCableBlock extends AbstractSplitterCableBlock
{
    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.Block> codec() {
        return simpleCodec(properties -> new SplitterCableBlock(properties, this.foamCableBlock));
    }

	private final SplitterFoamCableBlock foamCableBlock;

	protected SplitterCableBlock(Properties settings, SplitterFoamCableBlock foamCableBlock)
	{
		super(settings);
		this.foamCableBlock = foamCableBlock;
		registerFoamCounterpart(foamCableBlock, this);
	}

	public static SplitterCableBlock create(Properties settings, SplitterFoamCableBlock foamCableBlock)
	{
		prepareCreate(CableType.splitter, 0);
		return new SplitterCableBlock(settings, foamCableBlock);
	}

	@Override
	public boolean isFoam()
	{
		return false;
	}

	@Override
	public boolean isHardFoam(BlockState state)
	{
		return false;
	}

	public SplitterFoamCableBlock getFoamCableBlock()
	{
		return this.foamCableBlock;
	}
}
