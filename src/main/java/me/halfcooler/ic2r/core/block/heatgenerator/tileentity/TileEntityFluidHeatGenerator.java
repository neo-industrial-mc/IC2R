package me.halfcooler.ic2r.core.block.heatgenerator.tileentity;

import me.halfcooler.ic2r.api.recipe.IFluidHeatManager;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.FluidHeatManager;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.heatgenerator.container.ContainerFluidHeatGenerator;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquid;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquidByManager;
import me.halfcooler.ic2r.core.block.invslot.InvSlotOutput;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityHeatSourceInventory;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

@NotClassic
public class TileEntityFluidHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui
{
	public final InvSlotConsumableLiquid fluidSlot;
	public final InvSlotOutput outputSlot;
	@GuiSynced
	protected final Ic2rFluidTank fluidTank;
	protected final Fluids fluids;
	protected int burnAmount = 0;
	protected int production = 0;
	boolean newActive = false;
	private short ticker = 0;

	public TileEntityFluidHeatGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.FLUID_HEAT_GENERATOR, pos, state);
		this.fluidSlot = new InvSlotConsumableLiquidByManager(this, "fluidSlot", 1, Recipes.fluidHeatGenerator);
		this.outputSlot = new InvSlotOutput(this, "output", 1);
		this.fluids = this.addComponent(new Fluids(this));
		this.fluidTank = this.fluids.addTankInsert("fluidTank", 10000, Fluids.fluidPredicate(Recipes.semiFluidGenerator));
	}

	public static void init()
	{
		Recipes.fluidHeatGenerator = new FluidHeatManager();
		if (IC2RConfig.balance.energy.generator.semiFluidBiogas.get().floatValue() > 0.0F)
		{
			addFuel(Ic2rFluids.BIOGAS.still(), 10, Math.round(32.0F * IC2RConfig.balance.energy.heatGenerator.semiFluidBiogas.get().floatValue()));
		}
	}

	public static void addFuel(Fluid fluid, int amount, int heat)
	{
		Recipes.fluidHeatGenerator.addFluid(fluid, amount, heat);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.needsFluid())
		{
			needsInvUpdate = this.gainFuel();
		}

		if (needsInvUpdate)
		{
			this.setChanged();
		}

		if (this.getActive() != this.newActive)
		{
			this.setActive(this.newActive);
		}
	}

	public boolean isConverting()
	{
		return this.getTankAmount() > 0 && this.HeatBuffer < this.getMaxHeatEmittedPerTick();
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.fluidTank.fromNbt(nbt.getCompound("fluidTank"));
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		CompoundTag fluidTankTag = new CompoundTag();
		this.fluidTank.toNbt(fluidTankTag);
		nbt.put("fluidTank", fluidTankTag);
	}

	@Override
	protected int fillHeatBuffer(int maxAmount)
	{
		if (this.isConverting())
		{
			if (this.ticker >= 19)
			{
				this.fluidTank.drainMbUnchecked(this.burnAmount, false);
				this.ticker = 0;
			} else
			{
				this.ticker++;
			}

			this.newActive = true;
			return this.production;
		} else
		{
			this.newActive = false;
			return 0;
		}
	}

	@Override
	public int getMaxHeatEmittedPerTick()
	{
		return this.calcHeatProduction();
	}

	@Override
	public ContainerBase<TileEntityFluidHeatGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerFluidHeatGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerFluidHeatGenerator(syncId, inventory, this);
	}

	protected int calcHeatProduction()
	{
		if (!this.fluidTank.isEmpty() && this.getFluidfromTank() != null)
		{
			IFluidHeatManager.BurnProperty property = Recipes.fluidHeatGenerator.getBurnProperty(this.getFluidfromTank());
			if (property != null)
			{
				return this.production = property.heat();
			}
		}

		return this.production = 0;
	}

	protected void calcBurnAmount()
	{
		if (this.getFluidfromTank() != null)
		{
			IFluidHeatManager.BurnProperty property = Recipes.fluidHeatGenerator.getBurnProperty(this.getFluidfromTank());
			if (property != null)
			{
				this.burnAmount = property.amount();
				return;
			}
		}

		this.burnAmount = 0;
	}

	public Ic2rFluidTank getFluidTank()
	{
		return this.fluidTank;
	}

	public Ic2rFluidStack getFluidStackfromTank()
	{
		return this.fluidTank.getFluidStack();
	}

	public Fluid getFluidfromTank()
	{
		return this.getFluidStackfromTank().getFluid();
	}

	public int getTankAmount()
	{
		return this.fluidTank.getFluidAmount();
	}

	public int gaugeLiquidScaled(int i)
	{
		return this.fluidTank.getFluidAmount() <= 0 ? 0 : this.fluidTank.getFluidAmount() * i / this.fluidTank.getCapacity();
	}

	public boolean needsFluid()
	{
		return this.fluidTank.getFluidAmount() <= this.fluidTank.getCapacity();
	}

	protected boolean gainFuel()
	{
		if (!this.fluidTank.isEmpty())
		{
			this.calcHeatProduction();
			this.calcBurnAmount();
		}

		return this.fluidSlot.processIntoTank(this.fluidTank, this.outputSlot);
	}
}
