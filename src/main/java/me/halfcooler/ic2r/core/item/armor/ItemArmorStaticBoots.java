package me.halfcooler.ic2r.core.item.armor;

import me.halfcooler.ic2r.api.item.ElectricItem;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.ref.Ic2rArmorMaterials;
import me.halfcooler.ic2r.core.util.StackUtil;

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
		super(Ic2rArmorMaterials.STATIC_BOOTS, settings, EquipmentSlot.FEET);
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		// NeoForge inventoryTick uses global inventory indices (FEET armor = 36),
		// not EquipmentSlot.getIndex() (FEET = 0). Identify worn armor by stack identity.
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

		// 1.21: getOrCreateNbtData returns a copy — persist position via editTag.
		CompoundTag compound = StackUtil.getOrCreateNbtData(stack);
		boolean isNotWalking = player.isPassenger() || player.isInWater();
		int lastX = compound.contains("x") && !isNotWalking ? compound.getInt("x") : player.blockPosition().getX();
		int lastZ = compound.contains("z") && !isNotWalking ? compound.getInt("z") : player.blockPosition().getZ();
		if (!compound.contains("x") || isNotWalking || !compound.contains("z"))
		{
			int x = lastX;
			int z = lastZ;
			StackUtil.editTag(stack, nbt ->
			{
				nbt.putInt("x", x);
				nbt.putInt("z", z);
			});
		}

		int currentX = player.blockPosition().getX();
		int currentZ = player.blockPosition().getZ();
		double distance = Math.sqrt((lastX - currentX) * (lastX - currentX) + (lastZ - currentZ) * (lastZ - currentZ));
		if (distance >= 5.0)
		{
			StackUtil.editTag(stack, nbt ->
			{
				nbt.putInt("x", currentX);
				nbt.putInt("z", currentZ);
			});
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