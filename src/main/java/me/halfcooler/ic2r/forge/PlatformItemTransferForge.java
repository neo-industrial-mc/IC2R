package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.item.EnvItemHandler;
import me.halfcooler.ic2r.core.proxy.EnvProxy;
import me.halfcooler.ic2r.platform.services.PlatformItemTransfer;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Forge implementation of {@link PlatformItemTransfer} (G3.6).
 * <p>
 * {@link #createHandler()} → {@link EnvProxy#createItemHandler()} ({@link EnvItemHandlerForge}).
 * <p>
 * {@link #insert} delegates to {@link EnvItemHandler#deposit(BlockEntity, Direction, ItemStack,
 * com.mojang.authlib.GameProfile, boolean)} with a null owner profile. When {@code side} is
 * null, {@link Direction#NORTH} is used as a default face (EnvItemHandler requires a face).
 * <p>
 * <strong>{@link #extract} limitation:</strong> {@link EnvItemHandler} has no public
 * {@code extract(BlockEntity, Direction, int, boolean)} surface — only private
 * {@code extractItemFrom} on the Forge handler and inv-to-inv {@link EnvItemHandler#transfer}.
 * This method therefore returns {@link ItemStack#EMPTY} (count 0). Callers needing extraction
 * should use {@link #createHandler()} and {@link EnvItemHandler#transfer} (or domain helpers
 * built on deposit/fetch) until a first-class extract API is added to the domain handler.
 */
public final class PlatformItemTransferForge implements PlatformItemTransfer
{
	private static EnvProxy proxy()
	{
		EnvProxy env = IC2R.envProxy;
		if (env == null)
		{
			throw new IllegalStateException("IC2R.envProxy not initialized; cannot use PlatformItemTransferForge");
		}
		return env;
	}

	@Override
	public EnvItemHandler createHandler()
	{
		return proxy().createItemHandler();
	}

	@Override
	public int insert(BlockEntity be, @Nullable Direction side, ItemStack stack, boolean simulate)
	{
		if (be == null || stack == null || stack.isEmpty())
		{
			return 0;
		}
		Direction face = side != null ? side : Direction.NORTH;
		return createHandler().deposit(be, face, stack, null, simulate);
	}

	@Override
	public ItemStack extract(BlockEntity be, @Nullable Direction side, int maxCount, boolean simulate)
	{
		// See class javadoc — EnvItemHandler lacks a public BE extract API.
		return ItemStack.EMPTY;
	}
}
