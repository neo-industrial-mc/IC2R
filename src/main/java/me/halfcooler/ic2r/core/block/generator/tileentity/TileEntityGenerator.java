package me.halfcooler.ic2r.core.block.generator.tileentity;

import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableFuel;
import me.halfcooler.ic2r.core.gui.dynamic.IGuiValueProvider;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import me.halfcooler.ic2r.core.util.ParticleUtil;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;

public class TileEntityGenerator extends TileEntityBaseGenerator implements IGuiValueProvider
{
	public final InvSlotConsumableFuel fuelSlot;
	@GuiSynced
	public int totalFuel = 0;

	public TileEntityGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.GENERATOR, pos, state, Math.round(10.0F * IC2RConfig.balance.energy.generator.generator.get().floatValue()), 1, 4000);
		this.fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, false);
	}

	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		if (this.getActive())
		{
			ParticleUtil.showFurnaceFlames(this.getLevel(), this.worldPosition, this.getFacing());
		}
	}

	public double getFuelRatio()
	{
		return this.fuel <= 0 ? 0.0 : (double) this.fuel / this.totalFuel;
	}

	@Override
	public boolean gainFuel()
	{
		int fuelValue = this.fuelSlot.consumeFuel() / 4;
		if (fuelValue == 0)
		{
			return false;
		}

		this.fuel += fuelValue;
		this.totalFuel = fuelValue;
		return true;
	}

	@Override
	public boolean isConverting()
	{
		return this.fuel > 0;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2rSoundEvents.GENERATOR_GENERATOR_LOOP.value();
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("fuel".equals(name))
		{
			return this.getFuelRatio();
		} else
		{
			throw new IllegalArgumentException();
		}
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.totalFuel = nbt.getInt("totalFuel");
	}

	@Override
	public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.saveAdditional(nbt, registries);
		nbt.putInt("totalFuel", this.totalFuel);
	}
}
