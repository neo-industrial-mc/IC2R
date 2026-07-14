package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerMiner;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.gui.VanillaButton;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiMiner extends Ic2rGui<ContainerMiner>
{
	public GuiMiner(ContainerMiner container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(EnergyGauge.asBolt(this, 155, 41, container.base));
		this.addElement(
			new VanillaButton(this, 152, 40, 18, 18, this.createEventSender(0))
				.withIcon(() -> new ItemStack(Ic2rItems.PUMP))
				.withTooltip(container.base::getPumpModeTooltip)
		);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guiminer.png");
	}
}
