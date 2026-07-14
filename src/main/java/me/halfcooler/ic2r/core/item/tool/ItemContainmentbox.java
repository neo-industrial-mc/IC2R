package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.item.IHandHeldInventory;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemContainmentbox extends Item implements IHandHeldInventory
{
	public ItemContainmentbox(Properties settings)
	{
		super(settings);
	}

	public @NotNull InteractionResultHolder<ItemStack> use(Level world, @NotNull Player player, @NotNull InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!world.isClientSide)
		{
			this.getInventory(player, hand, stack).openManagedItem(player, hand, null);
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	public boolean onDroppedByPlayer(ItemStack stack, Player player)
	{
		if (!player.getCommandSenderWorld().isClientSide && !StackUtil.isEmpty(stack) && player.containerMenu instanceof ContainerContainmentbox)
		{
			HandHeldContainmentbox containmentBox = ((ContainerContainmentbox) player.containerMenu).base;
			if (containmentBox.isThisContainer(stack))
			{
				containmentBox.saveAsThrown(stack);
				player.closeContainer();
			}
		}

		return true;
	}

	public @NotNull Rarity getRarity(@NotNull ItemStack stack)
	{
		return Rarity.UNCOMMON;
	}

	@Override
	public IHasGui getInventory(Player player, InteractionHand hand, ItemStack stack)
	{
		return new HandHeldContainmentbox(player, hand, stack, 12);
	}
}
