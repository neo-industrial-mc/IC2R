package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.fluid.EnvFluidHandler;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.proxy.EnvProxy;
import me.halfcooler.ic2r.platform.services.PlatformFluidBridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Forge implementation of {@link PlatformFluidBridge} (G3.6).
 * <p>
 * {@link #createHandler()} → {@link EnvProxy#createFluidStackHandler()}
 * ({@link EnvFluidHandlerForge} / client variant).
 * World / item ops thin-delegate to existing {@link EnvFluidHandler} methods:
 * <ul>
 *   <li>{@link #drainWorld} → {@link EnvFluidHandler#drainMb(BlockState, Level, BlockPos, BlockEntity, Direction, int, boolean)}</li>
 *   <li>{@link #fillWorld} → {@link EnvFluidHandler#fillMb(BlockState, Level, BlockPos, BlockEntity, Direction, Ic2rFluidStack, boolean)}</li>
 *   <li>{@link #getContained} → {@link EnvFluidHandler#getFluidStack(ItemStack)}</li>
 * </ul>
 */
public final class PlatformFluidBridgeForge implements PlatformFluidBridge
{
	private static EnvProxy proxy()
	{
		EnvProxy env = IC2R.envProxy;
		if (env == null)
		{
			throw new IllegalStateException("IC2R.envProxy not initialized; cannot use PlatformFluidBridgeForge");
		}
		return env;
	}

	private static EnvFluidHandler handler()
	{
		return proxy().createFluidStackHandler();
	}

	@Override
	public EnvFluidHandler createHandler()
	{
		return handler();
	}

	@Override
	public Ic2rFluidStack drainWorld(
		BlockState state, Level level, BlockPos pos, @Nullable BlockEntity be,
		Direction side, int maxMb, boolean simulate
	)
	{
		return handler().drainMb(state, level, pos, be, side, maxMb, simulate);
	}

	@Override
	public int fillWorld(
		BlockState state, Level level, BlockPos pos, @Nullable BlockEntity be,
		Direction side, Ic2rFluidStack stack, boolean simulate
	)
	{
		if (stack == null || stack.isEmpty())
		{
			return 0;
		}
		return handler().fillMb(state, level, pos, be, side, stack, simulate);
	}

	@Override
	public Ic2rFluidStack getContained(ItemStack stack)
	{
		if (stack == null || stack.isEmpty())
		{
			return Ic2rFluidStack.EMPTY;
		}
		return handler().getFluidStack(stack);
	}
}
