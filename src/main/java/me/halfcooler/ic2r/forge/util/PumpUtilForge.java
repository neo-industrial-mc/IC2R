package me.halfcooler.ic2r.forge.util;

import me.halfcooler.ic2r.core.util.PumpUtil;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.IFluidBlock;
import org.jetbrains.annotations.Nullable;

/**
 * Forge-side {@link PumpUtil.PumpFluidAccess} that delegates to {@link IFluidBlock}.
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
		Block block = state.getBlock();
		if (block instanceof IFluidBlock fb)
		{
			if (fb.canDrain(world, pos))
			{
				return 0;
			}

			float level = Math.abs(fb.getFilledPercentage(world, pos));
			return 7 - Util.limit(Math.round(6.0F * level), 0, 6);
		}

		return null; // not a forge fluid block; caller falls through to LiquidBlock check
	}
}
