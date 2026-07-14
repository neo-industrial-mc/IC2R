package me.halfcooler.ic2r.core.block.wiring;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.energy.profile.ElectricalDisplay;
import me.halfcooler.ic2r.core.gui.TextLabel;
import me.halfcooler.ic2r.core.gui.VanillaButton;
import me.halfcooler.ic2r.core.gui.dynamic.TextProvider;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuiChargePadBlock extends Ic2rGui<ContainerChargepadBlock>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guichargepadblock.png");

	public GuiChargePadBlock(ContainerChargepadBlock container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 161);
		this.addElement(EnergyGauge.asBar(this, 79, 38, container.base));
		this.addElement(new VanillaButton(this, 152, 4, 20, 20, this.createEventSender(0)).withIcon(() -> new ItemStack(Items.REDSTONE)).withTooltip((container.base::getRedstoneMode)));
		this.addElement(TextLabel.create(this, 79, 25, TextProvider.of(() -> Component.translatable("ic2r.EUStorage.gui.info.level", ElectricalDisplay.formatTierName(container.base.energy.getWorkingVoltage())).getString()), 4210752, false));
		this.addElement(TextLabel.create(this, 110, 35, TextProvider.of(() -> " " + (int) Math.min(container.base.energy.getEnergy(), container.base.energy.getCapacity())), 4210752, false));
		this.addElement(TextLabel.create(this, 110, 45, TextProvider.of(() -> "/" + (int) container.base.energy.getCapacity()), 4210752, false));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
