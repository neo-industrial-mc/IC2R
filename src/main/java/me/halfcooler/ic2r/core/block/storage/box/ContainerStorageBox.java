package me.halfcooler.ic2r.core.block.storage.box;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotInvSlot;

import net.minecraft.world.entity.player.Inventory;

/**
 * G2.3 pure-code Menu for all {@link TileEntityStorageBox} tiers.
 * <p>
 * Layout mirrors the former guidef XMLs (title + slotgrid + playerInventory), sized from
 * inventory capacity so wood/iron/bronze/steel/iridium share one MenuType.
 */
public class ContainerStorageBox extends ContainerFullInv<TileEntityStorageBox>
{
	/**
	 * Content grid columns (9 for most tiers; 18 for iridium).
	 */
	public final int cols;
	/**
	 * Content grid rows.
	 */
	public final int rows;
	/**
	 * GUI image width (matches former guidef {@code width}).
	 */
	public final int guiWidth;
	/**
	 * GUI image height (matches former guidef {@code height}).
	 */
	public final int guiHeight;

	public ContainerStorageBox(int syncId, Inventory playerInventory, TileEntityStorageBox te)
	{
		super(
			Ic2rScreenHandlers.STORAGE_BOX,
			syncId,
			playerInventory,
			te,
			layoutGuiWidth(te.inventory.size()) + 2,
			layoutGuiHeight(te.inventory.size())
		);

		int size = te.inventory.size();
		this.cols = layoutCols(size);
		this.rows = layoutRows(size);
		this.guiWidth = layoutGuiWidth(size);
		this.guiHeight = layoutGuiHeight(size);

		int idx = 0;
		for (int row = 0; row < this.rows; row++)
		{
			for (int col = 0; col < this.cols && idx < size; col++)
			{
				this.addSlot(new SlotInvSlot(te.inventory, idx, 8 + col * 18, 17 + row * 18));
				idx++;
			}
		}
	}

	static int layoutCols(int size)
	{
		return size > 63 ? 18 : 9;
	}

	static int layoutRows(int size)
	{
		int cols = layoutCols(size);
		return (size + cols - 1) / cols;
	}

	static int layoutGuiWidth(int size)
	{
		return 14 + layoutCols(size) * 18;
	}

	static int layoutGuiHeight(int size)
	{
		return 16 + layoutRows(size) * 18 + 13 + 83;
	}

	/**
	 * Top-left of player inventory SlotGrid (guidef {@code playerInventory x/y}).
	 */
	public int playerInvX()
	{
		return this.cols > 9 ? 88 : 7;
	}

	public int playerInvY()
	{
		return 16 + this.rows * 18 + 13;
	}
}
