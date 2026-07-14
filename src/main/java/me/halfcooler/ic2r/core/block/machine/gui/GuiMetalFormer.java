package me.halfcooler.ic2r.core.block.machine.gui;

import me.halfcooler.ic2r.core.Ic2rGui;
import me.halfcooler.ic2r.core.block.machine.container.ContainerMetalFormer;
import me.halfcooler.ic2r.core.gui.CustomGauge;
import me.halfcooler.ic2r.core.gui.EnergyGauge;
import me.halfcooler.ic2r.core.gui.Gauge;
import me.halfcooler.ic2r.core.gui.RecipeButton;
import me.halfcooler.ic2r.core.gui.VanillaButton;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiMetalFormer extends Ic2rGui<ContainerMetalFormer>
{
	public GuiMetalFormer(ContainerMetalFormer container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(EnergyGauge.asBolt(this, 20, 37, container.base));
		this.addElement(CustomGauge.create(this, 52, 39, container.base::getProgress, Gauge.GaugeStyle.ProgressMetalFormer));
		this.addElement(new VanillaButton(this, 65, 53, 20, 20, this.createEventSender(0)).withIcon(() -> switch (container.base.getMode())
		{
			case 0 -> new ItemStack(Ic2rItems.COPPER_CABLE);
			case 1 -> new ItemStack(Ic2rItems.FORGE_HAMMER);
			case 2 -> new ItemStack(Ic2rItems.CUTTER);
			default -> null;
		}).withTooltip(() -> switch (container.base.getMode())
		{
			case 0 -> Component.translatable("ic2r.MetalFormer.gui.switch.Extruding").getString();
			case 1 -> Component.translatable("ic2r.MetalFormer.gui.switch.Rolling").getString();
			case 2 -> Component.translatable("ic2r.MetalFormer.gui.switch.Cutting").getString();
			default -> null;
		}));
		if (RecipeButton.canUse())
		{
			for (int i = 0; i < 3; i++)
			{
				final int mode = i;
				this.addElement(new RecipeButton(this, 52, 39, 46, 9, new String[] { "metal_former" + mode }).withEnableHandler(() -> container.base.getMode() == mode));
			}
		}
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/guimetalformer.png");
	}
}
