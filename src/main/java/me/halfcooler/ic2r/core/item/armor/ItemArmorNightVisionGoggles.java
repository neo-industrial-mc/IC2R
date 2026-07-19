package me.halfcooler.ic2r.core.item.armor;

import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.api.item.IElectricItem;
import me.halfcooler.ic2r.api.item.IItemHudInfo;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.item.ElectricItemTooltipHandler;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.ref.Ic2rArmorMaterials;
import me.halfcooler.ic2r.core.util.KeyboardClient;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemArmorNightVisionGoggles extends ItemArmorUtility implements IElectricItem, IItemHudInfo
{
	public ItemArmorNightVisionGoggles(Properties settings)
	{
		super(Ic2rArmorMaterials.NIGHT_VISION_GOGGLES, settings, EquipmentSlot.HEAD);
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return false;
	}

	@Override
	public double getMaxCharge(ItemStack stack)
	{
		return 200000.0;
	}

	@Override
	public int getTier(ItemStack stack)
	{
		return 1;
	}

	@Override
	public double getTransferLimit(ItemStack stack)
	{
		return 200.0;
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new LinkedList<>();
		info.add(ElectricItem.manager.getToolTip(stack));
		return info;
	}

	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if (entity instanceof Player player)
		{
			if (player.getItemBySlot(this.getEquipmentSlot()) == stack)
			{
				CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
				boolean active = nbtData.getBoolean("active");
				byte toggleTimer = nbtData.getByte("toggle_timer");
				if (IC2R.keyboard.isAltKeyDown(player) && IC2R.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0)
				{
					toggleTimer = 10;
					active = !active;
					if (IC2R.sideProxy.isSimulating())
					{
						// 1.21: persist toggle state via editTag (getOrCreateNbtData returns a copy)
						boolean enabled = active;
						StackUtil.editTag(stack, nbt -> nbt.putBoolean("active", enabled));
						if (active)
						{
							IC2R.sideProxy.messagePlayer(player, "ic2r.night_vision.mode.enabled");
						} else
						{
							IC2R.sideProxy.messagePlayer(player, "ic2r.night_vision.mode.disabled");
						}
					}
				}

				ItemArmorNanoSuit.getNightVisionOrNot(stack, player, toggleTimer, active);
			}
		}
	}

	public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext world, @NotNull List<Component> tooltip, @NotNull TooltipFlag context)
	{
		ElectricItemTooltipHandler.addTooltip(stack, tooltip);
		Ic2rTooltip.add(tooltip, Component.translatable("item.ic2r.tooltip.night_vision.toggle", KeyboardClient.altKey.getKey().getDisplayName(), KeyboardClient.modeSwitchKey.getKey().getDisplayName()));
	}

	public boolean isValidRepairItem(@NotNull ItemStack par1ItemStack, @NotNull ItemStack par2ItemStack)
	{
		return false;
	}
}
