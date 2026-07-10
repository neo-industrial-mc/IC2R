package ic2.core.item.tool;

import ic2.core.ContainerBase;
import ic2.core.item.ItemNuclearResource;
import ic2.core.item.reactor.ItemReactorUranium;
import ic2.core.network.GrowingBuffer;
import ic2.core.util.StackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HandHeldContainmentbox extends HandHeldInventory
{
	public HandHeldContainmentbox(Player player, InteractionHand hand, ItemStack containerStack, int inventorySize)
	{
		super(player, hand, containerStack, inventorySize);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return false;
		}

		// Nuclear materials and fuel rods; nested in NBT so they do not inventoryTick-radiate the player.
		return stack.getItem() instanceof ItemNuclearResource || stack.getItem() instanceof ItemReactorUranium;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerContainmentbox(syncId, this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerContainmentbox(syncId, this);
	}
}
