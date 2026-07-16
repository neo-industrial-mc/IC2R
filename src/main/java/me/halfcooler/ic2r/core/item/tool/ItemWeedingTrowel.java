package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.crops.Crops;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.crop.TileEntityCrop;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.StackUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemWeedingTrowel extends Item
{
	public ItemWeedingTrowel(Properties settings)
	{
		super(settings);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();

		if (!IC2R.sideProxy.isSimulating())
		{
			return InteractionResult.PASS;
		}

		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity instanceof TileEntityCrop tileEntityCrop)
		{
			if (tileEntityCrop.getCrop() == Crops.weed)
			{
				StackUtil.dropAsEntity(world, pos, StackUtil.copyWithSize(new ItemStack(Ic2rItems.WEED), tileEntityCrop.getCurrentAge() + 1));
				tileEntityCrop.reset();
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}
}
