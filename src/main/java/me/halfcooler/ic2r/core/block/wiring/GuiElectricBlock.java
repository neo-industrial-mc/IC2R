package me.halfcooler.ic2r.core.block.wiring;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.energy.profile.ElectricalDisplay;
import me.halfcooler.ic2r.core.gui.VanillaButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuiElectricBlock extends Ic2rGui<ContainerElectricBlock>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guielectricblock.png");

	public GuiElectricBlock(ContainerElectricBlock container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 196);
		this.addElement(EnergyGauge.asBar(this, 79, 38, container.base));
		this.addElement(new VanillaButton(this, 152, 4, 20, 20, this.createEventSender(0)).withIcon(() -> new ItemStack(Items.REDSTONE)).withTooltip(container.base::getRedstoneMode));
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		int rightTextHeight = 24;
		int color = 4207152;
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		this.drawString(guiGraphics, 8, 74, Component.translatable("ic2r.EUStorage.gui.info.armor").getString(), color);
		this.drawString(guiGraphics, 82, rightTextHeight, Component.translatable("ic2r.EUStorage.gui.info.level", ElectricalDisplay.formatTierName(this.menu.base.energy.getWorkingVoltage())).getString(), color);
		int e = (int) Math.min(this.menu.base.energy.getEnergy(), this.menu.base.energy.getCapacity());
		this.drawString(guiGraphics, 110, rightTextHeight + 10, " " + e, color);
		this.drawString(guiGraphics, 110, rightTextHeight + 20, "/" + (int) this.menu.base.energy.getCapacity(), color);
		String output = Component.translatable("ic2r.EUStorage.gui.info.output").getString();
		String power = ElectricalDisplay.formatPower(
			this.menu.base.energy.getWorkingVoltage().getVoltage() * this.menu.base.energy.getMaxSourceAmperage(),
			this.menu.base.energy.getWorkingVoltage(),
			this.menu.base.energy.getMaxSourceAmperage()
		).getString();
		this.drawString(guiGraphics, 82, rightTextHeight + 35, output, color);
		this.drawString(guiGraphics, 82, rightTextHeight + 45, power, color);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
