package ic2.core.item.tool;

import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

public class Ic2Pickaxe extends PickaxeItem {
  public Ic2Pickaxe(Tier material, int attackDamage, float attackSpeed, Properties settings) {
    super(
        material,
        settings.attributes(PickaxeItem.createAttributes(material, attackDamage, attackSpeed)));
  }
}
