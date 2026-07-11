package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBoxable;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.api.tile.IWrenchAble;
import ic2.core.item.PriorityUsableItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemToolWrenchElectric extends ItemElectricTool implements PriorityUsableItem, IBoxable, IEnhancedOverlayProvider
{
	public ItemToolWrenchElectric(Properties settings)
	{
		super(settings, 100);
		this.tier = 1;
		this.maxCharge = 12000;
		this.transferLimit = 250;
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		if (!this.canTakeDamage(stack, 1.0))
		{
			return InteractionResult.FAIL;
		}

		Player player = context.getPlayer();
		if (player == null)
		{
			return InteractionResult.PASS;
		}

		int useResult = ItemToolWrench.onWrenchUse(player, context);
		return switch (useResult)
		{
			case -2 -> InteractionResult.PASS;
			case -1 -> InteractionResult.FAIL;
			default ->
			{
				this.consumeEnergy(stack, useResult, player);
				yield InteractionResult.SUCCESS;
			}
		};
	}

	public boolean canTakeDamage(ItemStack stack, double amount)
	{
		amount *= 100.0;
		return ElectricItem.manager.getCharge(stack) >= amount;
	}

	@Override
	public boolean consumeEnergy(ItemStack stack, double amount, LivingEntity entity)
	{
		double operationEnergyCost = 100.0 * amount;
		return super.consumeEnergy(stack, operationEnergyCost, entity);
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean providesEnhancedOverlay(Level world, BlockPos pos, Direction side, Player player, ItemStack stack)
	{
		// Still show the grid with empty charge so the player can plan the click.
		return world.getBlockState(pos).getBlock() instanceof IWrenchAble;
	}
}
