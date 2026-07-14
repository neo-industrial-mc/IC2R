package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.recipe.ICannerBottleRecipeManager;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableSolidCanner;
import me.halfcooler.ic2r.core.block.invslot.InvSlotProcessableSolidCanner;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySolidCanner extends TileEntityStandardMachine<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput>
{
	public final InvSlotConsumableSolidCanner canInputSlot;

	public TileEntitySolidCanner(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.SOLID_CANNER, pos, state, 2, 200, 1);
		this.inputSlot = new InvSlotProcessableSolidCanner(this, "input", 1);
		this.canInputSlot = new InvSlotConsumableSolidCanner(this, "canInput", 1);
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2rSoundEvents.MACHINE_CANNER_OPERATE;
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return Ic2rSoundEvents.MACHINE_INTERRUPT1;
	}

	protected Collection<ItemStack> getOutput(ItemStack output)
	{
		return Collections.singleton(output);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.Processing,
			UpgradableProperty.Transformer,
			UpgradableProperty.EnergyStorage,
			UpgradableProperty.ItemConsuming,
			UpgradableProperty.ItemProducing
		);
	}
}
