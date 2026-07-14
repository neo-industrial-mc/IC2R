package me.halfcooler.ic2r.core.block.kineticgenerator.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import me.halfcooler.ic2r.core.gui.*;
import me.halfcooler.ic2r.core.gui.dynamic.TextProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.Supplier;

public class GuiSteamKineticGenerator extends Ic2rGui<ContainerSteamKineticGenerator>
{
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guisteamkineticgenerator.png");

	public GuiSteamKineticGenerator(ContainerSteamKineticGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(TankGauge.createPlain(this, 75, 21, 26, 26, container.base.getDistilledWaterTank()));
		this.addElement(new SlotGrid(this, 80, 26, SlotGrid.SlotStyle.Plain).withTooltip(() -> !container.base.hasTurbine() ? "ic2r.steam_kinetic_generator.gui.turbineslot" : null));
		this.addElement(Image.create(this, 36, 20, 30, 26, TEXTURE, 256, 256, 176, 0, 206, 26).withEnableHandler(() -> container.base.hasTurbine() && container.base.isVentingSteam()).withTooltip("ic2r.steam_kinetic_generator.gui.venting_warning"));
		this.addElement(Image.create(this, 110, 20, 30, 26, TEXTURE, 256, 256, 176, 0, 206, 26).withEnableHandler(() -> container.base.hasTurbine() && container.base.isThrottled()).withTooltip("ic2r.steam_kinetic_generator.gui.condensation_warning"));
		this.addElement(TextLabel.create(this, 8, 51, 160, 13, TextProvider.of(new Supplier<>()
		{
			public String get()
			{
				return Component.translatable(this.getRaw()).getString();
			}

			private String getRaw()
			{
				if (!container.base.hasTurbine())
				{
					return "ic2r.steam_kinetic_generator.gui.error.noturbine";
				} else if (container.base.isTurbineBlockedByWater())
				{
					return "ic2r.steam_kinetic_generator.gui.error.filledupwithwater";
				} else
				{
					return container.base.getActive() ? "ic2r.steam_kinetic_generator.gui.active" : "ic2r.steam_kinetic_generator.gui.waiting";
				}
			}
		}), () -> container.base.hasTurbine() && !container.base.isTurbineBlockedByWater() ? 2157374 : 14946604, false, 4, 0, false, true));
		this.addElement(TextLabel.create(this, 8, 68, 160, 13, TextProvider.of(() -> Component.translatable("ic2r.steam_kinetic_generator.gui.turbine.ouput", container.base.getKUoutput()).getString()), 2157374, false, 4, 0, false, true).withEnableHandler(() -> container.base.hasTurbine() && !container.base.isTurbineBlockedByWater()));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return TEXTURE;
	}
}
