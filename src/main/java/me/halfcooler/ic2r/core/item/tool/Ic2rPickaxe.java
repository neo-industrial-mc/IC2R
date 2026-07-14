package me.halfcooler.ic2r.core.item.tool;

import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;

public class Ic2rPickaxe extends PickaxeItem
{
	public Ic2rPickaxe(Tier material, int attackDamage, float attackSpeed, Properties settings)
	{
		super(material, attackDamage, attackSpeed, settings);
	}
}
