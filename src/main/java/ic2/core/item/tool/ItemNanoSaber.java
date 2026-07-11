package ic2.core.item.tool;

public class ItemNanoSaber extends AbstractItemNanoSaber {
  public ItemNanoSaber(Properties settings) {
    super(settings);
  }

  // The active-state attack damage/speed boost is applied dynamically via
  // ic2.forge.EventHandlerForge#onItemAttributeModifiers (NeoForge's
  // ItemAttributeModifierEvent replaces the removed Forge getAttributeModifiers hook).
}
