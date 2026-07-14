package me.halfcooler.ic2r.platform.services;

import me.halfcooler.ic2r.core.item.EnvItemHandler;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Cross-block item transfer / inventory capability bridge.
 * <p>
 * Draft SPI. Corresponds to {@code EnvProxy#createItemHandler()} and
 * {@link EnvItemHandler} (Forge: {@code EnvItemHandlerForge} / {@code IItemHandler}).
 * Common machine logic should call this SPI (or domain helpers backed by it) instead of
 * Forge capabilities directly.
 */
public interface PlatformItemTransfer
{
	/** Factory for the domain item-transfer environment (Forge today: {@code EnvItemHandlerForge}). */
	EnvItemHandler createHandler();

	/**
	 * Insert into a neighbour inventory via the platform item capability.
	 *
	 * @return count inserted
	 */
	int insert(BlockEntity be, @Nullable Direction side, ItemStack stack, boolean simulate);

	/**
	 * Extract from a neighbour inventory via the platform item capability.
	 *
	 * @return extracted stack (may be empty)
	 */
	ItemStack extract(BlockEntity be, @Nullable Direction side, int maxCount, boolean simulate);
}
