package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.fluid.FluidBeBridge;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidBlock;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidItem;
import me.halfcooler.ic2r.core.item.tool.AbstractItemNanoSaber;
import me.halfcooler.ic2r.core.util.Util;
import me.halfcooler.ic2r.forge.block.tileentity.TileEntityInventoryCap;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

/**
 * Registers NeoForge 1.21 capabilities (replaces AttachCapabilitiesEvent + LazyOptional).
 */
public final class Ic2rCapabilities {

    private Ic2rCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        // --- Block entity item / fluid handlers for every ic2r BE type ---
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            if (!isIc2r(type)) {
                continue;
            }
            registerBlockEntity(event, type);
        }

        // --- Items: fluid handlers + nano saber ---
        for (Item item : BuiltInRegistries.ITEM) {
            if (BuiltInRegistries.ITEM.getKey(item) == null
                || !"ic2r".equals(BuiltInRegistries.ITEM.getKey(item).getNamespace())) {
                continue;
            }
            if (item instanceof Ic2rFluidItem) {
                event.registerItem(
                    Capabilities.FluidHandler.ITEM,
                    (stack, ctx) -> new ItemFluidCapImpl(stack),
                    item);
            }
            if (item instanceof AbstractItemNanoSaber) {
                event.registerItem(
                    NanoSaberCapabilities.NANO_SABER_STATE,
                    (stack, ctx) -> new ItemNanoSaberCapImpl(stack),
                    item);
            }
        }
    }

    private static boolean isIc2r(BlockEntityType<?> type) {
        var key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        return key != null && "ic2r".equals(key.getNamespace());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerBlockEntity(RegisterCapabilitiesEvent event, BlockEntityType type) {
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            type,
            (BlockEntity be, Direction side) -> fluidHandler(be, side));

        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            type,
            (BlockEntity be, Direction side) -> itemHandler(be, side));
    }

    private static net.neoforged.neoforge.fluids.capability.IFluidHandler fluidHandler(BlockEntity be, Direction side) {
        if (!(be instanceof Ic2rTileEntity)) {
            return null;
        }
        if (be instanceof FluidBeBridge bridge) {
            Ic2rFluidBlock fb = bridge.getFluidBlock();
            if (fb != null && fb.isFluidBlock(null, null, null, be)) {
                return new BlockFluidCapImpl(fb, be).getHandler(side);
            }
            return null;
        }
        return new LazyBlockFluidCapImpl(be).getHandler(side);
    }

    private static IItemHandler itemHandler(BlockEntity be, Direction side) {
        if (!(be instanceof Ic2rTileEntity)) {
            return null;
        }
        if (be instanceof TileEntityInventory teInv) {
            if (side == null) {
                return TileEntityInventoryCap.createCombinedItemHandler(teInv);
            }
            IItemHandler[] sided = cacheSided(teInv);
            int idx = side.ordinal();
            if (idx >= 0 && idx < sided.length) {
                return sided[idx];
            }
            return null;
        }
        if (be instanceof WorldlyContainer worldly) {
            if (side == null) {
                return null;
            }
            return new SidedInvWrapper(worldly, side);
        }
        if (be instanceof Container container) {
            return new InvWrapper(container);
        }
        return null;
    }

    /** Per-BE sided wrappers; create on demand without LazyOptional. */
    private static IItemHandler[] cacheSided(TileEntityInventory teInv) {
        // SidedInvWrapper.create returns LazyOptional[] in old API; new API is per-side constructor.
        IItemHandler[] handlers = new IItemHandler[Util.ALL_DIRS.length];
        for (int i = 0; i < Util.ALL_DIRS.length; i++) {
            handlers[i] = new SidedInvWrapper(teInv, Util.ALL_DIRS[i]);
        }
        return handlers;
    }
}
