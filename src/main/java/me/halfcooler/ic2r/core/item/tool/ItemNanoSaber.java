package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.api.item.ElectricItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ItemNanoSaber extends AbstractItemNanoSaber
{
	public ItemNanoSaber(Properties settings)
	{
		super(settings);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack)
	{
		int dmg = 4;
		float speed = -3.0f;

		if (ElectricItem.manager.canUse(stack, 400.0) && isActive(stack))
		{
			dmg = 20;
			speed = 0f;
		}

		return ItemAttributeModifiers.builder()
			.add(Attributes.ATTACK_DAMAGE,
				new AttributeModifier(BASE_ATTACK_DAMAGE_ID, dmg, Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND)
			.add(Attributes.ATTACK_SPEED,
				new AttributeModifier(BASE_ATTACK_SPEED_ID, speed, Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND)
			.build();
	}
}
