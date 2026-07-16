package me.halfcooler.ic2r.forge;

import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Lazily wraps {@link BlockFluidCapImpl}, resolving the Fluids component on first
 * capability query. Capability providers may run before BE construction fully finishes.
 */
final class LazyBlockFluidCapImpl {

    private final BlockEntity be;
    private BlockFluidCapImpl delegate;

    LazyBlockFluidCapImpl(BlockEntity be) {
        this.be = be;
    }

    private BlockFluidCapImpl resolve() {
        if (this.delegate == null && this.be instanceof Ic2rTileEntity ic2te) {
            Fluids fluids = ic2te.getComponent(Fluids.class);
            if (fluids != null) {
                this.delegate = new BlockFluidCapImpl(fluids, this.be);
            }
        }
        return this.delegate;
    }

    @Nullable
    public IFluidHandler getHandler(@Nullable Direction facing) {
        BlockFluidCapImpl d = this.resolve();
        return d != null ? d.getHandler(facing) : null;
    }
}
