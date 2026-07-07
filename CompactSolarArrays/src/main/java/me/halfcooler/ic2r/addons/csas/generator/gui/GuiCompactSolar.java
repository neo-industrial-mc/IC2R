package me.halfcooler.ic2r.addons.csas.generator.gui;

import ic2.core.gui.FixedSizeOverlaySupplier;
import ic2.core.gui.GuiFullInv;
import ic2.core.gui.Image;
import ic2.core.gui.SlotGrid;
import me.halfcooler.ic2r.addons.csas.generator.container.ContainerCompactSolar;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiCompactSolar extends GuiFullInv<ContainerCompactSolar>
{
	private static final ResourceLocation SOLAR_OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/overlay/solar_sun.png");

	public GuiCompactSolar(ContainerCompactSolar container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(new SlotGrid(this, 79, 25, 1, 1, SlotGrid.SlotStyle.Normal));
		this.addElement(Image.create(this, 81, 45, 14, 14, SOLAR_OVERLAY_TEXTURE, 28, 14, new FixedSizeOverlaySupplier(14)
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
		this.addElement(Image.create(this, 81, 45, 14, 14, SOLAR_OVERLAY_TEXTURE, 28, 14, new FixedSizeOverlaySupplier(14)
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