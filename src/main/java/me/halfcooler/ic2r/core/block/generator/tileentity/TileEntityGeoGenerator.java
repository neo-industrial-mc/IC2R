package me.halfcooler.ic2r.core.block.generator.tileentity;

import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquid;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquidByTank;
import me.halfcooler.ic2r.core.block.invslot.InvSlotOutput;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityGeoGenerator extends TileEntityBaseGenerator
{
	private static final int fluidPerTick = 2;
	public final InvSlotConsumableLiquid fluidSlot;
	public final InvSlotOutput outputSlot;
	@GuiSynced
	protected final Ic2rFluidTank fluidTank;
	protected final Fluids fluids = this.addComponent(new Fluids(this));

	public TileEntityGeoGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.GEO_GENERATOR, pos, state, 20.0, 1, 2400);
		this.fluidTank = this.fluids.addTankInsert("fluid", 8000, Fluids.fluidPredicate(net.minecraft.world.level.material.Fluids.LAVA));
		this.production = Math.round(20.0F * IC2RConfig.balance.energy.generator.geothermal.get().floatValue());
		this.fluidSlot = new InvSlotConsumableLiquidByTank(
			this, "fluidSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Drain, this.fluidTank
		);
		this.outputSlot = new InvSlotOutput(this, "output", 1);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.fluidSlot.processIntoTank(this.fluidTank, this.outputSlot))
		{
			this.setChanged();
		}
	}

	@Override
	public boolean gainFuel()
	{
		boolean dirty = false;
		Ic2rFluidStack ret = this.fluidTank.drainMbUnchecked(2, true);
		if (ret != null && ret.getAmountMb() >= 2)
		{
			this.fluidTank.drainMbUnchecked(2, false);
			this.fuel++;
			dirty = true;
		}

		return dirty;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2rSoundEvents.GENERATOR_GEOTHERMAL_LOOP.get();
	}

	@Override
	protected void onBlockBreak()
	{
		super.onBlockBreak();
		this.fluidTank.isEmpty();
	}
}
