package ic2.core.item.tool;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Tier;

public class Ic2Axe extends AxeItem {
  public Ic2Axe(Tier material, float attackDamage, float attackSpeed, Properties settings) {
    super(
        material,
        settings.attributes(AxeItem.createAttributes(material, attackDamage, attackSpeed)));
  }
}
