package me.halfcooler.ic2r.core.block.kineticgenerator.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.kineticgenerator.container.ContainerWaterKineticGenerator;
import me.halfcooler.ic2r.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import me.halfcooler.ic2r.core.gui.IEnableHandler;
import me.halfcooler.ic2r.core.gui.TextLabel;
import me.halfcooler.ic2r.core.gui.dynamic.TextProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiWaterKineticGenerator extends Ic2rGui<ContainerWaterKineticGenerator>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guiwaterkineticgenerator.png");

	public GuiWaterKineticGenerator(ContainerWaterKineticGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		IEnableHandler validBiome = () -> container.base.type != TileEntityWaterKineticGenerator.BiomeState.INVALID;
		IEnableHandler invalidBiome = IEnableHandler.EnableHandlers.not(validBiome);
		this.addElement(TextLabel.create(this, 38, 52, TextProvider.ofTranslated("ic2r.WaterKineticGenerator.gui.wrongbiome1"), 2157374, false).withEnableHandler(invalidBiome));
		this.addElement(TextLabel.create(this, 45, 69, TextProvider.ofTranslated("ic2r.WaterKineticGenerator.gui.wrongbiome2"), 2157374, false).withEnableHandler(invalidBiome));
		IEnableHandler missingRotor = container.base.rotorSlot::isEmpty;
		this.addElement(TextLabel.create(this, 27, 52, TextProvider.ofTranslated("ic2r.WaterKineticGenerator.gui.rotormiss"), 2157374, false).withEnableHandler(IEnableHandler.EnableHandlers.and(validBiome, missingRotor)));
		IEnableHandler hasRotor = IEnableHandler.EnableHandlers.not(missingRotor);
		IEnableHandler hasRotorSpace = () -> container.base.checkSpace(container.base.getRotorDiameter(), true) == 0;
		this.addElement(TextLabel.create(this, 20, 52, TextProvider.ofTranslated("ic2r.WaterKineticGenerator.gui.rotorspace"), 2157374, false).withEnableHandler(IEnableHandler.EnableHandlers.and(validBiome, hasRotor, IEnableHandler.EnableHandlers.not(hasRotorSpace))));
		this.addElement(TextLabel.create(this, 55, 52, TextProvider.of(() -> Component.translatable("ic2r.WaterKineticGenerator.gui.output", container.base.getKuOutput()).getString()), 2157374, false).withEnableHandler(IEnableHandler.EnableHandlers.and(validBiome, hasRotor, hasRotorSpace)));
		this.addElement(TextLabel.create(this, 46, 70, TextProvider.of(container.base::getRotorHealth), 2157374, false).withEnableHandler(IEnableHandler.EnableHandlers.and(validBiome, hasRotor, hasRotorSpace)));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
