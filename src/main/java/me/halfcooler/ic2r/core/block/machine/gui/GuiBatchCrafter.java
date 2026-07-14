package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerBatchCrafter;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.gui.Gauge;
import me.halfcooler.ic2r.core.gui.ItemStackImage;
import me.halfcooler.ic2r.core.gui.LinkedGauge;
import me.halfcooler.ic2r.core.util.StackUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiBatchCrafter extends Ic2rGui<ContainerBatchCrafter>
{
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guibatchcrafter.png");

	public GuiBatchCrafter(ContainerBatchCrafter container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 206);
		this.addElement(EnergyGauge.asBolt(this, 12, 45, container.base));
		this.addElement(new LinkedGauge(this, 90, 35, container.base, "progress", Gauge.GaugeStyle.ProgressArrow));
		this.addElement(new ItemStackImage(this, 94, 14, () -> StackUtil.wrapEmpty(GuiBatchCrafter.this.menu.base.recipeOutput)));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return TEXTURE;
	}
}
