package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotDischarge;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityElectricMachine extends TileEntityBase
{
	public final InvSlotDischarge dischargeSlot;
	protected final Energy energy;

	public TileEntityElectricMachine(BlockEntityType<? extends TileEntityElectricMachine> type, BlockPos pos, BlockState state, int maxEnergy, int tier)
	{
		this(type, pos, state, maxEnergy, tier, true);
	}

	public TileEntityElectricMachine(
		BlockEntityType<? extends TileEntityElectricMachine> type, BlockPos pos, BlockState state, int maxEnergy, int tier, boolean allowRedstone
	)
	{
		super(type, pos, state);
		this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, tier, allowRedstone, InvSlot.InvSide.ANY);
		this.energy = this.addComponent(Energy.asBasicSink(this, maxEnergy, tier).addManagedSlot(this.dischargeSlot));
		this.syncElectricalProfile(0);
	}

	public void syncElectricalProfile(int recipePower)
	{
		this.energy.syncConsumerProfile(recipePower);
	}

	public boolean hasEnergy()
	{
		return this.energy.getEnergy() > 0.0;
	}
}
