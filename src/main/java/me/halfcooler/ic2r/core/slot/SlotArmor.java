package me.halfcooler.ic2r.core.slot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SlotArmor extends Slot
{
	/**
	 * Indexed by {@link EquipmentSlot#getIndex()} for humanoid armor:
	 * 0=FEET, 1=LEGS, 2=CHEST, 3=HEAD.
	 * <p>
	 * Uses public {@link InventoryMenu} constants — 1.21 no longer has a single
	 * {@code ResourceLocation[]} field on the class (reflection returned null).
	 */
	private static final ResourceLocation[] EMPTY_ARMOR_SLOT_TEXTURES = {
		InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
		InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
		InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
		InventoryMenu.EMPTY_ARMOR_SLOT_HELMET
	};
	private final EquipmentSlot armorType;

	public SlotArmor(Inventory inventory, EquipmentSlot armorType, int x, int y)
	{
		super(inventory, 36 + armorType.getIndex(), x, y);
		this.armorType = armorType;
	}

	public boolean mayPlace(ItemStack stack)
	{
		Item item = stack.getItem();
		return item != null && stack.getEquipmentSlot() == this.armorType;
	}

	public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()
	{
		return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_ARMOR_SLOT_TEXTURES[this.armorType.getIndex()]);
	}
}
