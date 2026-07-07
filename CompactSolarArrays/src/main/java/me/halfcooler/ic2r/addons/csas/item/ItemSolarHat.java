package me.halfcooler.ic2r.addons.csas.item;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.init.IC2Config;
import ic2.core.item.armor.ItemArmorUtility;
import ic2.core.util.Ic2Tooltip;
import ic2.core.util.StackUtil;
import me.halfcooler.ic2r.addons.csas.common.CompactSolarType;
import me.halfcooler.ic2r.addons.csas.init.CsasConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemSolarHat extends ItemArmorUtility
{
	private final CompactSolarType type;

	public ItemSolarHat(CompactSolarType type, Properties settings)
	{
		super(me.halfcooler.ic2r.addons.csas.init.CsasArmorMaterials.SOLAR_HAT, settings, EquipmentSlot.HEAD);
		this.type = type;
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if (!(entity instanceof Player player) || slot != EquipmentSlot.HEAD.getIndex() || !IC2.sideProxy.isSimulating())
		{
			return;
		}

		if (!this.shouldProduce())
		{
			return;
		}

		float chargeAmount = (float) (TileEntitySolarGenerator.getSkyLight(world, player.blockPosition())
			* IC2Config.balance.energy.generator.solar.get()
			* this.type.getMultiplier());
		if (chargeAmount <= 0.0F)
		{
			return;
		}

		for (EquipmentSlot armorSlot : EquipmentSlot.values())
		{
			if (armorSlot.getType() != EquipmentSlot.Type.ARMOR || armorSlot == EquipmentSlot.HEAD)
			{
				continue;
			}

			ItemStack armor = player.getItemBySlot(armorSlot);
			if (!StackUtil.isEmpty(armor))
			{
				ElectricItem.manager.charge(armor, chargeAmount, this.type.getTier(), true, false);
			}
		}
	}

	private boolean shouldProduce()
	{
		int rate = CsasConfig.PRODUCTION_RATE.get();
		return rate <= 1 || IC2.random.nextInt(rate) == 0;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @org.jetbrains.annotations.Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
	{
		super.appendHoverText(stack, level, tooltip, flag);
		Ic2Tooltip.add(tooltip, Component.translatable("tooltip.ic2r_csas.power_tier", this.type.getTier()));
	}

	@Override
	public int getEnchantmentValue()
	{
		return 0;
	}

	@Override
	public boolean isValidRepairItem(@NotNull ItemStack stack, @NotNull ItemStack repairCandidate)
	{
		return false;
	}
}