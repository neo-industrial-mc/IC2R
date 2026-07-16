package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.api.crops.CropCard;
import me.halfcooler.ic2r.api.crops.Crops;
import me.halfcooler.ic2r.api.crops.ICropSeed;
import me.halfcooler.ic2r.core.crop.TileEntityCrop;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.List;


import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.component.DataComponents;

public class ItemCropSeed extends Item implements ICropSeed
{
	public ItemCropSeed(Properties settings)
	{
		super(settings);
	}

	public static ItemStack generateItemStackFromValues(CropCard crop, int statGrowth, int statGain, int statResistance, int scan)
	{
		ItemStack stack = new ItemStack(Ic2rItems.CROP_SEED_BACK);
		CompoundTag tag = new CompoundTag();
		tag.putString("owner", crop.getOwner());
		tag.putString("id", crop.getId());
		tag.putByte("growth", (byte) statGrowth);
		tag.putByte("gain", (byte) statGain);
		tag.putByte("resistance", (byte) statResistance);
		tag.putByte("scan", (byte) scan);
		stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
		return stack;
	}

	public @NotNull String getDescriptionId(@NotNull ItemStack itemstack)
	{
		if (itemstack == null)
		{
			return "ic2r.crop.unknown";
		} else
		{
			CropCard cropCard = Crops.instance.getCropCard(itemstack);
			int level = this.getScannedFromStack(itemstack);
			if (level == 0)
			{
				return "ic2r.crop.unknown";
			} else
			{
				return level >= 0 && cropCard != null ? cropCard.getUnlocalizedName() : "ic2r.crop.invalid";
			}
		}
	}

	public @NotNull Component getName(@NotNull ItemStack stack)
	{
		CropCard crop = Crops.instance.getCropCard(stack);
		return Component.translatable(crop == null ? "ic2r.crop.seeds" : crop.getSeedType(), super.getName(stack));
	}

	public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext world, @NotNull List<Component> info, @NotNull TooltipFlag debugTooltips)
	{
		if (this.getScannedFromStack(stack) >= 4)
		{
			Ic2rTooltip.add(info, Component.literal("§2Gr§7 " + this.getGrowthFromStack(stack)));
			Ic2rTooltip.add(info, Component.literal("§6Ga§7 " + this.getGainFromStack(stack)));
			Ic2rTooltip.add(info, Component.literal("§3Re§7 " + this.getResistanceFromStack(stack)));
		}
	}

	public @NotNull InteractionResult useOn(UseOnContext context)
	{
		if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof TileEntityCrop crop)
		{
			ItemStack stack = context.getItemInHand();
			if (crop.tryPlantIn(
				Crops.instance.getCropCard(stack),
				0,
				this.getGrowthFromStack(stack),
				this.getGainFromStack(stack),
				this.getResistanceFromStack(stack),
				this.getScannedFromStack(stack)
			))
			{
				Player player = context.getPlayer();
				if (!player.getAbilities().instabuild)
				{
					player.getInventory().items.set(player.getInventory().selected, StackUtil.emptyStack);
				}

				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	public void fillItemCategory(CreativeModeTab tabs, NonNullList<ItemStack> items)
	{
		if (true)
		{
			for (CropCard crop : Crops.instance.getCrops())
			{
				items.add(generateItemStackFromValues(crop, 1, 1, 1, 4));
			}
		}
	}

	@Override
	public CropCard getCropFromStack(ItemStack is)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null && nbt.contains("owner", 8) && nbt.contains("id", 8))
		{
			String owner = nbt.getString("owner");
			String id = nbt.getString("id");
			return Crops.instance.getCropCard(owner, id);
		} else
		{
			return null;
		}
	}

	@Override
	public void setCropFromStack(ItemStack is, CropCard crop)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null)
		{
			nbt.putString("owner", crop.getOwner());
			nbt.putString("id", crop.getId());
		}
	}

	@Override
	public int getGrowthFromStack(ItemStack is)
	{
		CompoundTag nbt = is.getTag();
		return nbt == null ? -1 : nbt.getByte("growth");
	}

	@Override
	public void setGrowthFromStack(ItemStack is, int value)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null)
		{
			nbt.putByte("growth", (byte) value);
		}
	}

	@Override
	public int getGainFromStack(ItemStack is)
	{
		CompoundTag nbt = is.getTag();
		return nbt == null ? -1 : nbt.getByte("gain");
	}

	@Override
	public void setGainFromStack(ItemStack is, int value)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null)
		{
			nbt.putByte("gain", (byte) value);
		}
	}

	@Override
	public int getResistanceFromStack(ItemStack is)
	{
		CompoundTag nbt = is.getTag();
		return nbt == null ? -1 : nbt.getByte("resistance");
	}

	@Override
	public void setResistanceFromStack(ItemStack is, int value)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null)
		{
			nbt.putByte("resistance", (byte) value);
		}
	}

	@Override
	public int getScannedFromStack(ItemStack is)
	{
		CompoundTag nbt = is.getTag();
		return nbt == null ? -1 : nbt.getByte("scan");
	}

	@Override
	public void setScannedFromStack(ItemStack is, int value)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null)
		{
			nbt.putByte("scan", (byte) value);
		}
	}

	@Override
	public void incrementScannedFromStack(ItemStack is)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null)
		{
			nbt.putByte("scan", (byte) (this.getScannedFromStack(is) + 1));
		}
	}
}
