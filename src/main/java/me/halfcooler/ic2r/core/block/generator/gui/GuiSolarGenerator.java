package me.halfcooler.ic2r.core.block.generator.gui;

import me.halfcooler.ic2r.core.block.generator.container.ContainerSolarGenerator;
import me.halfcooler.ic2r.core.gui.FixedSizeOverlaySupplier;
import me.halfcooler.ic2r.core.gui.GuiFullInv;
import me.halfcooler.ic2r.core.gui.Image;
import me.halfcooler.ic2r.core.gui.SlotGrid;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiSolarGenerator extends GuiFullInv<ContainerSolarGenerator>
{
	private static final ResourceLocation solarOverlayTexture = ResourceLocation.fromNamespaceAndPath("ic2r", "textures/gui/overlay/solar_sun.png");

	public GuiSolarGenerator(ContainerSolarGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(new SlotGrid(this, 79, 25, 1, 1, SlotGrid.SlotStyle.Normal));
		this.addElement(Image.create(this, 81, 45, 14, 14, solarOverlayTexture, 28, 14, new FixedSizeOverlaySupplier(14)
		{
			@Override
			public int getUS()
			{
				return 0;
			}

			@Override
			public int getVS()
			{
				return 0;
			}
		}));
		this.addElement(Image.create(this, 81, 45, 14, 14, solarOverlayTexture, 28, 14, new FixedSizeOverlaySupplier(14)
		{
			@Override
			public int getUS()
			{
				return 14;
			}

			@Override
			public int getVS()
			{
				return 0;
			}
		}).withEnableHandler(container.base::isSunlight));
	}
}
