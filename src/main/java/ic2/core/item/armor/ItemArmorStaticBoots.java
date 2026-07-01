package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.ref.Ic2ArmorMaterials;
import ic2.core.util.StackUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemArmorStaticBoots extends ItemArmorUtility
{
	public ItemArmorStaticBoots(Properties settings)
	{
		super(Ic2ArmorMaterials.STATIC_BOOTS, settings, EquipmentSlot.FEET);
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if (!(entity instanceof Player player) || slot != this.getEquipmentSlot().getIndex() || !IC2.sideProxy.isSimulating())
		{
			return;
		}

		ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
		if (StackUtil.isEmpty(chestArmor))
		{
			return;
		}

		CompoundTag compound = StackUtil.getOrCreateNbtData(stack);
		boolean isNotWalking = player.isPassenger() || player.isInWater();
		if (!compound.contains("x") || isNotWalking)
		{
			compound.putInt("x", player.blockPosition().getX());
		}

		if (!compound.contains("z") || isNotWalking)
		{
			compound.putInt("z", player.blockPosition().getZ());
		}

		int lastX = compound.getInt("x");
		int lastZ = compound.getInt("z");
		int currentX = player.blockPosition().getX();
		int currentZ = player.blockPosition().getZ();
		double distance = Math.sqrt((lastX - currentX) * (lastX - currentX) + (lastZ - currentZ) * (lastZ - currentZ));
		if (distance >= 5.0)
		{
			compound.putInt("x", currentX);
			compound.putInt("z", currentZ);
			ElectricItem.manager.charge(chestArmor, Math.min(3.0, distance / 5.0), Integer.MAX_VALUE, true, false);
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