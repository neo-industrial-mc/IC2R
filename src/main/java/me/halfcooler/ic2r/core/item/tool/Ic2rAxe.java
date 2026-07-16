package me.halfcooler.ic2r.core.item.tool;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;

public class Ic2rAxe extends AxeItem
{
	public Ic2rAxe(Tier material, float attackDamage, float attackSpeed, Properties settings)
	{
		super(material, settings.attributes(DiggerItem.createAttributes(material, attackDamage, attackSpeed)));
	}
}
