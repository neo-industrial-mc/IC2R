package me.halfcooler.ic2r.core.item.tool;

import me.halfcooler.ic2r.core.item.ContainerHandHeldInventory;
import me.halfcooler.ic2r.core.ref.Ic2rItems;
import me.halfcooler.ic2r.core.ref.Ic2rScreenHandlers;
import me.halfcooler.ic2r.core.slot.SlotDischarge;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerAnalyzer extends ContainerHandHeldInventory<HandHeldCropAnalyzer>
{
	public ContainerAnalyzer(int syncId, HandHeldCropAnalyzer analyzer)
	{
		super(Ic2rScreenHandlers.CROP_ANALYZER, syncId, analyzer);
		// Input slot - only accepts crop seed bags
		this.addSlot(new Slot(analyzer, HandHeldCropAnalyzer.SLOT_INPUT, 8, 7)
		{
			@Override
			public boolean mayPlace(@NotNull ItemStack stack)
			{
				return stack.is(Ic2rItems.CROP_SEED_BACK);
			}
		});
		// Output slot - no item can be placed (output only)
		this.addSlot(new Slot(analyzer, HandHeldCropAnalyzer.SLOT_OUTPUT, 41, 7)
		{
			@Override
			public boolean mayPlace(@NotNull ItemStack stack)
			{
				return false;
			}
		});
		// Battery slot
		this.addSlot(new SlotDischarge(analyzer, 2, HandHeldCropAnalyzer.SLOT_BATTERY, 152, 7));
		this.addPlayerInventorySlots(analyzer.player.getInventory(), 223);
	}

	@Override
	public void broadcastChanges()
	{
		this.base.tryScan();
		super.broadcastChanges();
	}
}
