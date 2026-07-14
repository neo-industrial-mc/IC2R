package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerPatternStorage;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityPatternStorage;
import me.halfcooler.ic2r.core.gui.CustomButton;
import me.halfcooler.ic2r.core.gui.IEnableHandler;
import me.halfcooler.ic2r.core.gui.ItemImage;
import me.halfcooler.ic2r.core.gui.TextLabel;
import me.halfcooler.ic2r.core.gui.dynamic.TextProvider;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiPatternStorage extends Ic2rGui<ContainerPatternStorage>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guipatternstorage.png");

	public GuiPatternStorage(ContainerPatternStorage container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(new CustomButton(this, 7, 19, 9, 18, this.createEventSender(0)).withTooltip("ic2r.PatternStorage.gui.info.last"));
		this.addElement(new CustomButton(this, 36, 19, 9, 18, this.createEventSender(1)).withTooltip("ic2r.PatternStorage.gui.info.next"));
		this.addElement(new CustomButton(this, 10, 37, 16, 8, this.createEventSender(2)).withTooltip("ic2r.PatternStorage.gui.info.export"));
		this.addElement(new CustomButton(this, 26, 37, 16, 8, this.createEventSender(3)).withTooltip("ic2r.PatternStorage.gui.info.import"));
		this.addElement(TextLabel.create(this, this.imageWidth / 2, 30, TextProvider.of(() ->
		{
			TileEntityPatternStorage te = container.base;
			return Math.min(te.index + 1, te.maxIndex) + " / " + te.maxIndex;
		}), 4210752, false, true, false));
		this.addElement(TextLabel.create(this, 10, 48, TextProvider.ofTranslated("ic2r.generic.text.Name"), 16777215, false));
		this.addElement(TextLabel.create(this, 10, 59, TextProvider.ofTranslated("ic2r.generic.text.UUMatte"), 16777215, false));
		this.addElement(TextLabel.create(this, 10, 70, TextProvider.ofTranslated("ic2r.generic.text.Energy"), 16777215, false));
		IEnableHandler patternInfoEnabler = () -> container.base.pattern != null;
		this.addElement(TextLabel.create(this, 80, 48, TextProvider.of(() ->
		{
			ItemStack pattern = container.base.pattern;
			return pattern != null ? pattern.getHoverName().getString() : null;
		}), 16777215, false).withEnableHandler(patternInfoEnabler));
		this.addElement(TextLabel.create(this, 80, 59, TextProvider.of(() -> Util.toSiString(container.base.patternUu, 4) + Component.translatable("ic2r.generic.text.bucketUnit").getString()), 16777215, false).withEnableHandler(patternInfoEnabler));
		this.addElement(TextLabel.create(this, 80, 70, TextProvider.of(() -> Util.toSiString(container.base.patternEu, 4) + Component.translatable("ic2r.generic.text.EU").getString()), 16777215, false).withEnableHandler(patternInfoEnabler));
		this.addElement(new ItemImage(this, 152, 29, () -> container.base.pattern));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}
