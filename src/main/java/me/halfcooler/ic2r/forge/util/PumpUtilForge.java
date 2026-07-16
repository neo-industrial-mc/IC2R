package me.halfcooler.ic2r.forge.util;

import me.halfcooler.ic2r.core.util.PumpUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge-side {@link PumpUtil.PumpFluidAccess}.
 * {@code IFluidBlock} was removed; liquid handling falls through to vanilla {@code LiquidBlock}.
 */
public final class PumpUtilForge implements PumpUtil.PumpFluidAccess
{
	private static final PumpUtilForge INSTANCE = new PumpUtilForge();

	private PumpUtilForge() {}

	public static void install()
	{
		PumpUtil.setFluidAccess(INSTANCE);
	}

	@Override
	@Nullable
	public Integer getForgeFluidDecay(BlockState state, Level world, BlockPos pos)
	{
		// No NeoForge IFluidBlock equivalent; caller uses LiquidBlock path.
		return null;
	}
}
