package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.block.invslot.InvSlotProcessableGeneric;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEntityMacerator extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public TileEntityMacerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.MACERATOR, pos, state, 2, 300, 1);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.macerator);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		Level world = this.getLevel();
		if (world == null)
		{
			return;
		}

		RandomSource rng = world.random;
		if (this.getActive() && rng.nextInt(8) == 0)
		{
			for (int i = 0; i < 4; i++)
			{
				double x = this.worldPosition.getX() + 0.5 + rng.nextFloat() * 0.6 - 0.3;
				double y = this.worldPosition.getY() + 1 + rng.nextFloat() * 0.2 - 0.1;
				double z = this.worldPosition.getZ() + 0.5 + rng.nextFloat() * 0.6 - 0.3;
				world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
			}
		}
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2rSoundEvents.MACHINE_MACERATOR_OPERATE.get();
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return Ic2rSoundEvents.MACHINE_INTERRUPT1.get();
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
