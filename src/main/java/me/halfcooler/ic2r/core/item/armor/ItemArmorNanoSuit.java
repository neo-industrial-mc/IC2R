package me.halfcooler.ic2r.core.item.armor;

import net.minecraft.core.Holder;

import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.api.item.HudMode;
import me.halfcooler.ic2r.api.item.IItemHudProvider;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.KeyboardClient;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemArmorNanoSuit extends ItemArmorElectric implements IItemHudProvider
{
	public static final int[] CHARGED_PROTECTION = new int[] { 3, 6, 8, 3 };

	public ItemArmorNanoSuit(Holder<ArmorMaterial> material, EquipmentSlot slot, Properties settings)
	{
		super(material, slot, settings, 1000000.0, 1600.0, 3);
	}

	static void getNightVisionOrNot(@NotNull ItemStack stack, Player player, byte toggleTimer, boolean isNightVisionEnabled)
	{
		// 1.21: CUSTOM_DATA mutations must go through editTag/setTag — getOrCreateNbtData returns a copy.
		if (IC2R.sideProxy.isSimulating() && toggleTimer > 0)
		{
			byte newTimer = (byte) (toggleTimer - 1);
			StackUtil.editTag(stack, nbt -> nbt.putByte("toggle_timer", newTimer));
		}

		if (isNightVisionEnabled && IC2R.sideProxy.isSimulating() && ElectricItem.manager.use(stack, 1.0, player))
		{
			int skylight = player.getCommandSenderWorld().getMaxLocalRawBrightness(BlockPos.containing(player.position()));
			affectPlayer(player, skylight);

		}
	}

	static void affectPlayer(Player player, int skylight)
	{
		if (skylight > 8)
		{
			player.removeEffect(MobEffects.NIGHT_VISION);
			player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, true));
		} else
		{
			player.removeEffect(MobEffects.BLINDNESS);
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
		}
	}

	@Override
	public int getEnergyPerDamage()
	{
		return 5000;
	}

	@Override
	public double getDamageAbsorptionRatio()
	{
		return 0.9;
	}

	public boolean absorbFall(ItemStack stack, float distance)
	{
		int fallDamage = Math.max((int) distance - 3, 0);
		if (fallDamage >= 8)
		{
			return false;
		}

		double energyCost = this.getEnergyPerDamage() * fallDamage;
		if (energyCost > ElectricItem.manager.getCharge(stack))
		{
			return false;
		}

		ElectricItem.manager.discharge(stack, energyCost, Integer.MAX_VALUE, true, false, false);
		return true;
	}

	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if (entity instanceof Player player)
		{
			CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
			byte toggleTimer = nbtData.getByte("toggle_timer");
			if (slot == EquipmentSlot.HEAD.getIndex())
			{
				boolean isNightVisionEnabled = nbtData.getBoolean("night_vision");
				short hubmode = nbtData.getShort("hud_mode");
				if (IC2R.keyboard.isAltKeyDown(player) && IC2R.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0)
				{
					toggleTimer = 10;
					isNightVisionEnabled = !isNightVisionEnabled;
					if (IC2R.sideProxy.isSimulating())
					{
						boolean enabled = isNightVisionEnabled;
						StackUtil.editTag(stack, nbt -> nbt.putBoolean("night_vision", enabled));
						if (isNightVisionEnabled)
						{
							IC2R.sideProxy.messagePlayer(player, "ic2r.night_vision.mode.enabled");
						} else
						{
							IC2R.sideProxy.messagePlayer(player, "ic2r.night_vision.mode.disabled");
						}
					}
				}

				if (IC2R.keyboard.isAltKeyDown(player) && IC2R.keyboard.isHudModeKeyDown(player) && toggleTimer == 0)
				{
					toggleTimer = 10;
					if (hubmode == HudMode.getMaxMode())
					{
						hubmode = 0;
					} else
					{
						hubmode++;
					}

					if (IC2R.sideProxy.isSimulating())
					{
						short mode = hubmode;
						StackUtil.editTag(stack, nbt -> nbt.putShort("hud_mode", mode));
						IC2R.sideProxy.messagePlayer(player, Component.translatable(HudMode.getFromID(hubmode).getTranslationKey()).getString());
					}
				}

				getNightVisionOrNot(stack, player, toggleTimer, isNightVisionEnabled);
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag context)
	{
		super.appendHoverText(stack, world, tooltip, context);
		if (this.getEquipmentSlot() == EquipmentSlot.HEAD)
		{
			Ic2rTooltip.add(tooltip, Component.translatable("item.ic2r.tooltip.night_vision.toggle", KeyboardClient.altKey.getKey().getDisplayName(), KeyboardClient.modeSwitchKey.getKey().getDisplayName()));
		}
	}

	public @NotNull Rarity getRarity(@NotNull ItemStack stack)
	{
		return Rarity.UNCOMMON;
	}

	@Override
	public boolean doesProvideHUD(ItemStack stack)
	{
		return this.getEquipmentSlot() == EquipmentSlot.HEAD && ElectricItem.manager.getCharge(stack) > 0.0;
	}

	@Override
	public HudMode getHudMode(ItemStack stack)
	{
		return HudMode.getFromID(StackUtil.getOrCreateNbtData(stack).getShort("hud_mode"));
	}
}
