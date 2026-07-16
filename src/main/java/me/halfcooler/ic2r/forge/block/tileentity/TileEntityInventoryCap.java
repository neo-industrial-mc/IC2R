package me.halfcooler.ic2r.forge.block.tileentity;

import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.forge.block.invslot.InvSlotItemHandler;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import java.util.List;

/**
 * Forge-side capability helper for {@link TileEntityInventory} (A40.2).
 */
public final class TileEntityInventoryCap {

    private TileEntityInventoryCap() {
    }

    public static IItemHandler createCombinedItemHandler(TileEntityInventory te) {
        List<Object> handlers = te.getInvSlotHandlerList();
        if (handlers.isEmpty()) {
            return new CombinedInvWrapper();
        }
        IItemHandlerModifiable[] arr = new IItemHandlerModifiable[handlers.size()];
        for (int i = 0; i < handlers.size(); i++) {
            arr[i] = (InvSlotItemHandler) handlers.get(i);
        }
        return new CombinedInvWrapper(arr);
    }

    public static LazyOptional<IItemHandler> createCap(TileEntityInventory te) {
        return LazyOptional.of(() -> createCombinedItemHandler(te));
    }
}
