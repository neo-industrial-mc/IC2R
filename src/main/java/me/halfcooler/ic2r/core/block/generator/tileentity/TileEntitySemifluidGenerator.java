package me.halfcooler.ic2r.core.block.generator.tileentity;

import me.halfcooler.ic2r.api.recipe.ISemiFluidFuelManager;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.core.SemiFluidFuelManager;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquid;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquidByManager;
import me.halfcooler.ic2r.core.block.invslot.InvSlotOutput;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.core.ref.Ic2rSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

@NotClassic
public class TileEntitySemifluidGenerator extends TileEntityBaseGenerator
{
	public final InvSlotConsumableLiquid fluidSlot;
	public final InvSlotOutput outputSlot;
	@GuiSynced
	protected final Ic2rFluidTank fluidTank;
	protected final Fluids fluids = this.addComponent(new Fluids(this));

	public TileEntitySemifluidGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.SEMIFLUID_GENERATOR, pos, state, 32.0, 1, 32000);
		this.fluidTank = this.fluids.addTankInsert("fluid", 10000, Fluids.fluidPredicate(Recipes.semiFluidGenerator));
		this.fluidSlot = new InvSlotConsumableLiquidByManager(this, "fluidSlot", 1, Recipes.semiFluidGenerator);
		this.outputSlot = new InvSlotOutput(this, "output", 1);
	}

	public static void init()
	{
		Recipes.semiFluidGenerator = new SemiFluidFuelManager();
		if (IC2RConfig.balance.energy.generator.semiFluidBiogas.get().floatValue() > 0.0F)
		{
			addFuel(Ic2rFluids.BIOGAS.still(), 10, Math.round(16.0F * IC2RConfig.balance.energy.generator.semiFluidBiogas.get().floatValue()));
		}
	}

	public static void addFuel(Fluid fluid, int amount, int eu)
	{
		Recipes.semiFluidGenerator.addFluid(fluid, amount, eu);
	}

	@Override
	public void updateEntityServer()
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
		Ic2rFluidStack ret = this.fluidTank.drainMbUnchecked(Integer.MAX_VALUE, true);
		if (ret != null)
		{
			ISemiFluidFuelManager.BurnProperty property = Recipes.semiFluidGenerator.getBurnProperty(ret.getFluid());
			if (property != null && ret.getAmountMb() >= property.amount())
			{
				this.fluidTank.drainMbUnchecked(property.amount(), false);
				this.production = property.power();
				this.fuel = this.fuel + property.amount();
				dirty = true;
			}
		}

		return dirty;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2rSoundEvents.GENERATOR_GEOTHERMAL_LOOP;
	}
}
