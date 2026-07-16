package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.api.network.INetworkItemEventListener;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBattery extends BaseElectricItem implements INetworkItemEventListener
{
	public ItemBattery(Properties settings, double maxCharge, double transferLimit, int tier)
	{
		super(settings, maxCharge, transferLimit, tier);
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return true;
	}

	public @NotNull InteractionResultHolder<ItemStack> use(Level world, @NotNull Player player, @NotNull InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!world.isClientSide && StackUtil.getSize(stack) == 1)
		{
			if (ElectricItem.manager.getCharge(stack) > 0.0)
			{
				boolean transferred = false;

				for (int i = 0; i < 9; i++)
				{
					ItemStack target = player.getInventory().items.get(i);
					if (target != null
						&& target != stack
						&& !(ElectricItem.manager.discharge(target, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true, true) > 0.0))
					{
						double transfer = ElectricItem.manager.discharge(stack, 2.0 * this.transferLimit, Integer.MAX_VALUE, true, true, true);
						if (!(transfer <= 0.0))
						{
							transfer = ElectricItem.manager.charge(target, transfer, this.tier, true, false);
							if (!(transfer <= 0.0))
							{
								ElectricItem.manager.discharge(stack, transfer, Integer.MAX_VALUE, true, true, false);
								transferred = true;
							}
						}
					}
				}

				if (transferred && !world.isClientSide)
				{
					player.containerMenu.broadcastChanges();
					IC2R.network.get(true).initiateItemEvent(player, stack, 0, true);
				}
			}

			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		} else
		{
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);
		}
	}

	@Override
	public void onNetworkEvent(ItemStack stack, Player player, int event)
	{
		player.playSound(Ic2rSoundEvents.ITEM_BATTERY_USE.value(), 1.0F, 1.0F);
	}
}
