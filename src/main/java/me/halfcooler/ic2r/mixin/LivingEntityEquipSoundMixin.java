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

/**
 * Suppress armor equip sound / equip game-event when the same IC2R wearable is
 * re-applied with only component differences (EU charge, toggle flags, etc.).
 * <p>
 * Vanilla {@link LivingEntity#onEquipItem} plays the material equip sound whenever
 * {@link ItemStack#isSameItemSameComponents} fails. IC2R electric armor mutates
 * {@code CUSTOM_DATA}/damage while worn; creative inventory also re-pushes armor
 * slots via {@code ServerboundSetCreativeModeSlotPacket} → {@code setByPlayer},
 * which re-triggers equip sounds every time the player opens the inventory.
 */
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

		// Real swap between different items (or empty ↔ item) still plays equip sound.
		if (!ItemStack.isSameItem(oldItem, newItem))
		{
			return;
		}

		if (!isIc2rComponentDrivenWearable(newItem))
		{
			return;
		}

		// Same IC2R armor / jetpack-augmented piece; only NBT/components differ.
		ci.cancel();
	}

	private static boolean isIc2rComponentDrivenWearable(ItemStack stack)
	{
		Item item = stack.getItem();
		if (item instanceof ItemArmorIC2R || item instanceof ItemArmorUtility)
		{
			return true;
		}
		// Jetpack module attached to third-party chest armor also mutates CUSTOM_DATA.
		return JetpackHandler.hasJetpackAttached(stack);
	}
}
