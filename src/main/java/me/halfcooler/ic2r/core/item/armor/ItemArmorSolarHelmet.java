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
		// NeoForge inventoryTick uses global inventory indices (HEAD armor = 39),
		// not EquipmentSlot.getIndex() (HEAD = 3). Identify worn armor by stack identity.
		if (!(entity instanceof Player player) || player.getItemBySlot(this.getEquipmentSlot()) != stack
			|| !IC2R.sideProxy.isSimulating())
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