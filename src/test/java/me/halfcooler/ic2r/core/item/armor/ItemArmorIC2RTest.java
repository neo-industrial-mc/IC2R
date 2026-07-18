package me.halfcooler.ic2r.core.item.armor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.entity.EquipmentSlot;
import org.junit.jupiter.api.Test;

class ItemArmorIC2RTest
{
	@Test
	void calculatesBronzeDurabilityForEveryArmorSlot()
	{
		int multiplier = 15;

		assertEquals(165, ItemArmorIC2R.durabilityForSlot(EquipmentSlot.HEAD, multiplier));
		assertEquals(240, ItemArmorIC2R.durabilityForSlot(EquipmentSlot.CHEST, multiplier));
		assertEquals(225, ItemArmorIC2R.durabilityForSlot(EquipmentSlot.LEGS, multiplier));
		assertEquals(195, ItemArmorIC2R.durabilityForSlot(EquipmentSlot.FEET, multiplier));
	}
}
