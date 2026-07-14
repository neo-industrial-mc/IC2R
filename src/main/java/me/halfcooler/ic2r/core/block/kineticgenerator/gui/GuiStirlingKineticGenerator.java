package me.halfcooler.ic2r.core.block.kineticgenerator.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.kineticgenerator.container.ContainerStirlingKineticGenerator;
import me.halfcooler.ic2r.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiStirlingKineticGenerator extends Ic2rGui<ContainerStirlingKineticGenerator>
{
	public GuiStirlingKineticGenerator(ContainerStirlingKineticGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 204);
		this.addElement(TankGauge.createPlain(this, 19, 47, 12, 44, container.base.getInputTank()));
		this.addElement(TankGauge.createPlain(this, 145, 47, 12, 44, container.base.getOutputTank()));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guistirlingkineticgenerator.png");
	}
}
