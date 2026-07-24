package me.halfcooler.ic2r.core.item.upgrade;

import net.minecraft.client.gui.GuiGraphics;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.gui.GuiDefaultBackground;
import me.halfcooler.ic2r.core.gui.MouseButton;
import me.halfcooler.ic2r.core.gui.ScrollableList;
import me.halfcooler.ic2r.core.gui.SlotGrid;
import me.halfcooler.ic2r.core.item.ContainerHandHeldInventory;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotHologramSlot;

import java.util.ArrayList;
import java.util.List;


import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class HandHeldOre extends HandHeldUpgradeOption
{
	public HandHeldOre(HandHeldAdvancedUpgrade upgradeGUI)
	{
		super(upgradeGUI, "ore");
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new HandHeldOre.ContainerEditOre(syncId);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new HandHeldOre.ContainerEditOre(syncId);
	}

	public static class GuiEditOre extends GuiDefaultBackground<HandHeldOre.ContainerEditOre>
	{
		public GuiEditOre(HandHeldOre.ContainerEditOre container, Inventory playerInventory, Component title)
		{
			super(container, playerInventory, title, 200);
			this.addElement(container.base.getBackButton(this, 96));
			List<ScrollableList.IListItem> items = new ArrayList<>();

			for (String name : new String[] { "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten" })
			{
				items.add(new HandHeldOre.GuiEditOre.ListItem(name));
			}

			this.addElement(new ScrollableList(this, 10, 30, 120, 60, items));
			this.addElement(new SlotGrid(this, 7, 7, 9, 1, SlotGrid.SlotStyle.Normal));
			this.addElement(new SlotGrid(this, 7, 117, 9, 3, SlotGrid.SlotStyle.Normal));
			this.addElement(new SlotGrid(this, 7, 175, 9, 1, SlotGrid.SlotStyle.Normal));
		}

		public class ListItem implements ScrollableList.IListItem
		{
			private final String number;

			public ListItem(String number)
			{
				this.number = number;
			}

			@Override
			public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int width, int height, int mouseX, int mouseY)
			{
				GuiEditOre.this.drawString(guiGraphics, xOffset + 2, yOffset + 1, "Thing " + this.number, 16777215, false);
			}

			@Override
			public boolean onClick(MouseButton button, int mouseX, int mouseY)
			{
				System.out.println(this.number + " clicked with " + button);
				return false;
			}
		}
	}

	public class ContainerEditOre extends ContainerHandHeldInventory<HandHeldOre>
	{
		static final int HEIGHT = 200;

		public ContainerEditOre(int syncId)
		{
			super(Ic2rScreenHandlers.ADVANCED_UPGRADE_EDIT_ORE, syncId, HandHeldOre.this);
			this.addPlayerInventorySlots(this.player.getInventory(), 200);

			for (byte slot = 0; slot < 9; slot++)
			{
				this.addSlot(new SlotHologramSlot(HandHeldOre.this.inventory, slot, 8 + 18 * slot, 8, 1, HandHeldOre.this.makeSaveCallback()));
			}
		}

		@Override
		public void removed(Player player)
		{
			super.removed(player);
		}
	}
}
