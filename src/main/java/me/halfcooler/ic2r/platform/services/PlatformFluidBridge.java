package me.halfcooler.ic2r.platform.services;

import me.halfcooler.ic2r.core.fluid.EnvFluidHandler;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Fluid registration / capability / container transfer bridge.
 * <p>
 * Draft SPI. Mirrors the responsibility of {@code EnvProxy#createFluidStackHandler()} plus
 * the loader-backed ops currently on {@link EnvFluidHandler}. Full method surface of
 * {@link EnvFluidHandler} need not be duplicated here yet; W3.2+ can either:
 * <ul>
 *   <li>keep {@link EnvFluidHandler} as the domain API and have this SPI only factory it, or</li>
 *   <li>fold EnvFluidHandler methods into this SPI and retire the old interface.</li>
 * </ul>
 * Amounts are millibuckets (mB) unless noted.
 */
public interface PlatformFluidBridge
{
	/** Factory for the domain fluid environment handler (Forge today: {@code EnvFluidHandlerForge}). */
	EnvFluidHandler createHandler();

	/**
	 * Drain up to {@code maxMb} from a world fluid / tank at the given face.
	 *
	 * @return drained stack (may be empty)
	 */
	Ic2rFluidStack drainWorld(
		BlockState state, Level level, BlockPos pos, @Nullable BlockEntity be,
		Direction side, int maxMb, boolean simulate
	);

	/**
	 * Fill a world tank / fluid handler at the given face.
	 *
	 * @return mB accepted
	 */
	int fillWorld(
		BlockState state, Level level, BlockPos pos, @Nullable BlockEntity be,
		Direction side, Ic2rFluidStack stack, boolean simulate
	);

	/** Read contained fluid from an item stack (bucket / cell / capability item). */
	Ic2rFluidStack getContained(ItemStack stack);
}
