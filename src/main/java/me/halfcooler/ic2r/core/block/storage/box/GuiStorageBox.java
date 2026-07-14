package me.halfcooler.ic2r.core.block.storage.box;

import me.halfcooler.ic2r.core.gui.GuiDefaultBackground;
import me.halfcooler.ic2r.core.gui.SlotGrid;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * G2.3 pure-code Screen for {@link ContainerStorageBox}.
 * <p>
 * Uses {@link GuiDefaultBackground} (nine-slice) + {@link SlotGrid} frames — same visual stack as
 * DynamicGui for the former storage box guidef XMLs.
 */
public class GuiStorageBox extends GuiDefaultBackground<ContainerStorageBox>
{
	public GuiStorageBox(ContainerStorageBox container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, container.guiWidth, container.guiHeight);

		// Content slot frames (guidef slotgrid at x=7 y=16).
		this.addElement(new SlotGrid(this, 7, 16, container.cols, container.rows, SlotGrid.SlotStyle.Normal));

		// Player inventory + hotbar frames (hotbarOffset default 58).
		int px = container.playerInvX();
		int py = container.playerInvY();
		this.addElement(new SlotGrid(this, px, py, 9, 3, SlotGrid.SlotStyle.Normal));
		this.addElement(new SlotGrid(this, px, py + 58, 9, 1, SlotGrid.SlotStyle.Normal));
	}

	@Override
	protected void drawBackgroundAndTitle(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY)
	{
		super.drawBackgroundAndTitle(guiGraphics, partialTicks, mouseX, mouseY);
		// Centered title (replaces guidef <text y="6" align="center">%name%</text>).
		this.drawXCenteredString(guiGraphics, this.imageWidth / 2, 6, this.title, 4210752, false);
	}
}
