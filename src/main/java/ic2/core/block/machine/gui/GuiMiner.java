package ic2.core.block.machine.gui;

import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerMiner;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.VanillaButton;
import ic2.core.ref.Ic2Items;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiMiner extends Ic2Gui<ContainerMiner> {
  public GuiMiner(ContainerMiner container, Inventory playerInventory, Component title) {
    super(container, playerInventory, title);
    this.addElement(EnergyGauge.asBolt(this, 155, 41, container.base));
    this.addElement(
        new VanillaButton(this, 152, 40, 18, 18, this.createEventSender(0))
            .withIcon(() -> new ItemStack(Ic2Items.PUMP))
            .withTooltip(container.base::getPumpModeTooltip));
  }

  @Override
  protected ResourceLocation getTextureLocation() {
    return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guiminer.png");
  }
}
