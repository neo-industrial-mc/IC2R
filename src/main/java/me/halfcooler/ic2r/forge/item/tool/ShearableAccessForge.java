package me.halfcooler.ic2r.forge.item.tool;

import me.halfcooler.ic2r.core.item.tool.ItemElectricToolChainsaw;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.IForgeShearable;

import java.util.Collections;
import java.util.List;

/**
 * Forge-side {@link ItemElectricToolChainsaw.ShearableAccess} that delegates to {@link IForgeShearable}.
 */
public final class ShearableAccessForge implements ItemElectricToolChainsaw.ShearableAccess
{
	private static final ShearableAccessForge INSTANCE = new ShearableAccessForge();

	private ShearableAccessForge() {}

	public static void install()
	{
		ItemElectricToolChainsaw.setShearableAccess(INSTANCE);
	}

	@Override
	public boolean isShearable(Entity entity, ItemStack stack, Level level, BlockPos pos)
	{
		return entity instanceof IForgeShearable shearable && shearable.isShearable(stack, level, pos);
	}

	@Override
	public List<ItemStack> onSheared(Entity entity, Player player, ItemStack stack, Level level, BlockPos pos)
	{
		if (entity instanceof IForgeShearable shearable)
		{
			return shearable.onSheared(player, stack, level, pos, 0);
		}
		return Collections.emptyList();
	}
}
