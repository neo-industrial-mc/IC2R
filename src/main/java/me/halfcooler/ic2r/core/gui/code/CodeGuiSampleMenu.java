package me.halfcooler.ic2r.core.gui.code;

import me.halfcooler.ic2r.core.ContainerFullInv;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * W2.4 sample: pure-code Menu with no guidef XML and no real block binding.
 * <p>
 * Copy this pattern for new machines: extend {@link ContainerFullInv} / {@link me.halfcooler.ic2r.core.ContainerBase},
 * add slots in the constructor, register a {@link net.minecraft.world.inventory.MenuType} in
 * {@link Ic2rScreenHandlers}, and pair with a code Screen (see {@link CodeGuiSampleScreen}).
 * Real block entities should use {@code registerManagedBe} + {@link me.halfcooler.ic2r.core.IHasGui}
 * instead of this demo {@code register} factory — see {@code docs/spec/gui_modernization.md}.
 */
public final class CodeGuiSampleMenu extends ContainerFullInv<SimpleContainer>
{
	/** Demo content slot count (not wired to a tile inventory). */
	public static final int DEMO_SLOTS = 1;
	public static final int GUI_HEIGHT = 166;

	/**
	 * Client MenuType factory ({@code BiFunction} style registration).
	 */
	public static CodeGuiSampleMenu create(int syncId, Inventory playerInventory)
	{
		return new CodeGuiSampleMenu(syncId, playerInventory);
	}

	public CodeGuiSampleMenu(int syncId, Inventory playerInventory)
	{
		super(Ic2rScreenHandlers.CODE_GUI_SAMPLE, syncId, playerInventory, new SimpleContainer(DEMO_SLOTS), GUI_HEIGHT);

		// Hard-coded slot layout (replaces guidef <slot> / <playerInventory>).
		this.addSlot(new Slot(this.base, 0, 80, 35));
	}
}
