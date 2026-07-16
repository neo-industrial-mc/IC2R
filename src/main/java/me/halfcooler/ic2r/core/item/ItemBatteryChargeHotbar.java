package me.halfcooler.ic2r.core.item;

import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.api.item.IBoxable;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.item.tool.GuiToolbox;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.StackUtil;

import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

@NotClassic
public class ItemBatteryChargeHotbar extends ItemBattery implements IBoxable
{
	public ItemBatteryChargeHotbar(Properties settings, double maxCharge, double transferLimit, int tier)
	{
		super(settings, maxCharge, transferLimit, tier);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext world, @NotNull List<Component> tooltip, @NotNull TooltipFlag context)
	{
		super.appendHoverText(stack, world, tooltip, context);
		Mode mode = getMode(stack);
		Ic2rTooltip.add(tooltip, Component.translatable("ic2r.tooltip.mode",
			Component.translatable("ic2r.tooltip.mode." + mode.name().toLowerCase(Locale.ENGLISH))));
		if (world != null && world.isClientSide)
		{
			showBoxableTip(tooltip, mode);
		}
	}

	private void showBoxableTip(List<Component> tooltip, Mode mode)
	{
		if (Minecraft.getInstance().screen instanceof GuiToolbox)
		{
			Ic2rTooltip.add(tooltip, Component.translatable("ic2r.tooltip.mode.boxable")
				.withStyle(mode.enabled ? ChatFormatting.RED : ChatFormatting.GREEN));
		}
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, Level world, @NotNull Entity entity, int itemSlot, boolean isSelected)
	{
		Mode mode = getMode(stack);
		if (!world.isClientSide && entity instanceof Player player
			&& world.getGameTime() % 10L < this.getTier(stack) && mode.enabled)
		{
			double limit = this.getTransferLimit(stack);
			int tier = this.getTier(stack);

			for (int i = 0; i < 9 && limit > 0.0; i++)
			{
				ItemStack toCharge = player.getInventory().items.get(i);
				if (!toCharge.isEmpty()
					&& (mode != Mode.NOT_IN_HAND || i != player.getInventory().selected)
					&& !(toCharge.getItem() instanceof ItemBatteryChargeHotbar))
				{
					double charge = ElectricItem.manager.charge(toCharge, limit, tier, false, true);
					charge = ElectricItem.manager.discharge(stack, charge, tier, true, false, false);
					ElectricItem.manager.charge(toCharge, charge, tier, true, false);
					limit -= charge;
				}
			}
		}
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (world.isClientSide)
		{
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);
		}

		Mode mode = getMode(stack);
		mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
		setMode(stack, mode);
		IC2R.sideProxy.messagePlayer(player, "ic2r.tooltip.mode",
			"ic2r.tooltip.mode." + mode.name().toLowerCase(Locale.ENGLISH));
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	public void setMode(ItemStack stack, Mode mode)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		nbt.putByte("mode", (byte) mode.ordinal());
	}

	public Mode getMode(ItemStack stack)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		if (!nbt.contains("mode")) return Mode.ENABLED;
		int mode = nbt.getByte("mode");
		if (mode < 0 || mode >= Mode.values().length) return Mode.ENABLED;
		return Mode.values()[mode];
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack stack)
	{
		return getMode(stack) == Mode.DISABLED;
	}

	public enum Mode
	{
		ENABLED(true),
		DISABLED(false),
		NOT_IN_HAND(true);

		final boolean enabled;

		Mode(boolean enabled)
		{
			this.enabled = enabled;
		}
	}
}
