package me.halfcooler.ic2r.mixin;

import me.halfcooler.ic2r.core.item.armor.ItemArmorIC2R;
import me.halfcooler.ic2r.core.item.armor.ItemArmorUtility;
import me.halfcooler.ic2r.core.item.armor.jetpack.JetpackHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEquipSoundMixin
{
	@Inject(method = "onEquipItem", at = @At("HEAD"), cancellable = true)
	private void ic2r$suppressComponentOnlyArmorEquip(
		EquipmentSlot slot,
		ItemStack oldItem,
		ItemStack newItem,
		CallbackInfo ci)
	{
		if (oldItem.isEmpty() || newItem.isEmpty())
		{
			return;
		}

		if (!ItemStack.isSameItem(oldItem, newItem))
		{
			return;
		}

		if (!isIc2rComponentDrivenWearable(newItem))
		{
			return;
		}

		ci.cancel();
	}

	private static boolean isIc2rComponentDrivenWearable(ItemStack stack)
	{
		Item item = stack.getItem();
		if (item instanceof ItemArmorIC2R || item instanceof ItemArmorUtility)
		{
			return true;
		}
		return JetpackHandler.hasJetpackAttached(stack);
	}
}
