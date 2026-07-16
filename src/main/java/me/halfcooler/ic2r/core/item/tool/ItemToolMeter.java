package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.tile.IEnergyConductor;
import me.halfcooler.ic2r.api.energy.tile.IEnergySink;
import me.halfcooler.ic2r.api.energy.tile.IEnergySource;
import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;
import me.halfcooler.ic2r.api.item.IBoxable;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.item.IHandHeldInventory;
import me.halfcooler.ic2r.core.item.PriorityUsableItem;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemToolMeter extends Item implements IBoxable, IHandHeldInventory, PriorityUsableItem
{
	public ItemToolMeter(Properties settings)
	{
		super(settings);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		InteractionHand hand = context.getHand();

		if (player == null)
		{
			return InteractionResult.PASS;
		}

		if (world.isClientSide)
		{
			return InteractionResult.PASS;
		}

		IEnergyTile tile = EnergyNet.instance.getTile(world, pos);
		if (!(tile instanceof IEnergySource) && !(tile instanceof IEnergyConductor) && !(tile instanceof IEnergySink))
		{
			IC2R.sideProxy.messagePlayer(player, "Not an energy net tile");
		} else if (this.getInventory(player, hand, StackUtil.get(player, hand)).openManagedItem(player, hand, null))
		{
			ContainerMeter container = (ContainerMeter) player.containerMenu;
			container.setUut(tile);
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.SUCCESS;
	}

	public boolean onDroppedByPlayer(ItemStack stack, Player player)
	{
		if (!player.getCommandSenderWorld().isClientSide && !StackUtil.isEmpty(stack) && player.containerMenu instanceof ContainerMeter)
		{
			HandHeldMeter euReader = ((ContainerMeter) player.containerMenu).base;
			if (euReader.isThisContainer(stack))
			{
				euReader.saveAsThrown(stack);
				player.closeContainer();
			}
		}

		return true;
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}

	@Override
	public IHasGui getInventory(Player player, InteractionHand hand, ItemStack stack)
	{
		return new HandHeldMeter(player, hand, stack);
	}
}
