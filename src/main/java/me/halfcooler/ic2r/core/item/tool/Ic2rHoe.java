package me.halfcooler.ic2r.core.item.tool;

import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;

public class Ic2rHoe extends HoeItem
{
	public Ic2rHoe(Tier material, int attackDamage, float attackSpeed, Properties settings)
	{
		super(material, settings.attributes(DiggerItem.createAttributes(material, attackDamage, attackSpeed)));
	}
}
