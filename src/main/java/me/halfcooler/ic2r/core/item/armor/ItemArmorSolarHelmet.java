package me.halfcooler.ic2r.core.item.armor;

import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.generator.tileentity.TileEntitySolarGenerator;
import me.halfcooler.ic2r.core.ref.Ic2rArmorMaterials;
import me.halfcooler.ic2r.core.util.StackUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemArmorSolarHelmet extends ItemArmorUtility
{
	public ItemArmorSolarHelmet(Properties settings)
	{
		super(Ic2rArmorMaterials.SOLAR_HELMET, settings, EquipmentSlot.HEAD);
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if (!(entity instanceof Player player) || slot != this.getEquipmentSlot().getIndex() || !IC2R.sideProxy.isSimulating())
		{
			return;
		}

		ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
		if (StackUtil.isEmpty(chestArmor))
		{
			return;
		}

		float chargeAmount = TileEntitySolarGenerator.getSkyLight(world, player.blockPosition());
		if (chargeAmount > 0.0F)
		{
			ElectricItem.manager.charge(chestArmor, chargeAmount, Integer.MAX_VALUE, true, false);
		}
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