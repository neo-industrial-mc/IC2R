package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.energy.tile.IHeatSource;
import me.halfcooler.ic2r.api.recipe.IFermenterRecipeManager;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquid;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquidByManager;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquidByTank;
import me.halfcooler.ic2r.core.block.invslot.InvSlotOutput;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.block.machine.container.ContainerFermenter;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.gui.dynamic.IGuiValueProvider;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.recipe.FermenterRecipeManager;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.core.ref.Ic2rItems;

import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityFermenter extends TileEntityInventory implements IHasGui, IGuiValueProvider, IUpgradableBlock, ServerTicker
{
	public final InvSlotConsumableLiquidByManager fluidInputCellInSlot;
	public final InvSlotConsumableLiquidByTank fluidOutputCellInSlot;
	public final InvSlotOutput fluidInputCellOutSlot;
	public final InvSlotOutput fluidOutputCellOutSlot;
	public final InvSlotOutput fertiliserSlot;
	public final InvSlotUpgrade upgradeSlot;
	protected final Fluids fluids;
	private final Ic2rFluidTank inputTank;
	private final Ic2rFluidTank outputTank;
	private final int maxProgress = IC2RConfig.balance.fermenter.biomassPerFertilizier.get();
	public int progress = 0;
	private int heatBuffer = 0;

	public TileEntityFermenter(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.FERMENTER, pos, state);
		this.fluids = this.addComponent(new Fluids(this));
		this.outputTank = this.fluids.addTankExtract("output", 2000);
		this.inputTank = this.fluids.addTankInsert("input", 10000, Fluids.fluidPredicate(Recipes.fermenter));
		this.fluidInputCellOutSlot = new InvSlotOutput(this, "biomassOutput", 1);
		this.fluidOutputCellOutSlot = new InvSlotOutput(this, "biogassOutput", 1);
		this.fertiliserSlot = new InvSlotOutput(this, "output", 1);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);
		this.fluidOutputCellInSlot = new InvSlotConsumableLiquidByTank(
			this, "biogasInput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, this.outputTank
		);
		this.fluidInputCellInSlot = new InvSlotConsumableLiquidByManager(
			this, "biomassInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, Recipes.fermenter
		);
	}

	public static void init()
	{
		Recipes.fermenter = new FermenterRecipeManager();
		Recipes.fermenter
			.addRecipe(
				Ic2rFluids.BIOMASS.still(),
				IC2RConfig.balance.fermenter.needAmountBiomassPerRun.get(),
				IC2RConfig.balance.fermenter.hUPerRun.get(),
				Ic2rFluids.BIOGAS.still(),
				IC2RConfig.balance.fermenter.outputAmountBiogasPerRun.get()
			);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.inputTank.fromNbt(nbt.getCompound("inputTank"));
		this.outputTank.fromNbt(nbt.getCompound("outputTank"));
		this.progress = nbt.getInt("progress");
		this.heatBuffer = nbt.getInt("heatBuffer");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.put("inputTank", this.inputTank.toNbt(new CompoundTag()));
		nbt.put("outputTank", this.outputTank.toNbt(new CompoundTag()));
		nbt.putInt("progress", this.progress);
		nbt.putInt("heatBuffer", this.heatBuffer);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.fluidInputCellInSlot.processIntoTank(this.inputTank, this.fluidInputCellOutSlot);
		this.fluidOutputCellInSlot.processFromTank(this.outputTank, this.fluidOutputCellOutSlot);
		boolean newActive = this.work();
		if (this.getActive() != newActive)
		{
			this.setActive(newActive);
		}

		this.upgradeSlot.tick();
	}

	private boolean work()
	{
		if (this.progress >= this.maxProgress)
		{
			this.fertiliserSlot.add(new ItemStack(Ic2rItems.FERTILIZER));
			this.progress = 0;
		}

		Direction dir = this.getFacing();
		BlockEntity te = this.getLevel().getBlockEntity(this.worldPosition.relative(dir));
		if (te instanceof IHeatSource && !this.inputTank.isEmpty())
		{
			IFermenterRecipeManager.FermentationProperty fp = Recipes.fermenter.getFermentationInformation(this.inputTank.getFluidStack().getFluid());
			if (fp != null
				&& this.inputTank.getFluidAmount() >= fp.inputAmount()
				&& fp.outputAmount() <= this.outputTank.getCapacity() - this.outputTank.getFluidAmount())
			{
				this.heatBuffer = this.heatBuffer + ((IHeatSource) te).drawHeat(dir.getOpposite(), 100, false);
				if (this.heatBuffer >= fp.heat())
				{
					this.heatBuffer = this.heatBuffer - fp.heat();
					this.inputTank.drainMbUnchecked(fp.inputAmount(), false);
					this.outputTank.fillMbUnchecked(fp.getOutput(), false);
					this.progress = this.progress + fp.inputAmount();
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public ContainerBase<TileEntityFermenter> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerFermenter(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerFermenter(syncId, inventory, this);
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("heat".equals(name))
		{
			if (this.heatBuffer == 0)
			{
				return 0.0;
			}

			double maxHeatBuff = IC2RConfig.balance.fermenter.hUPerRun.get();
			if (!this.inputTank.isEmpty())
			{
				IFermenterRecipeManager.FermentationProperty fp = Recipes.fermenter.getFermentationInformation(this.inputTank.getFluidStack().getFluid());
				if (fp != null)
				{
					maxHeatBuff = fp.heat();
				}
			}

			return this.heatBuffer / maxHeatBuff;
		} else if ("progress".equals(name))
		{
			return this.progress == 0 ? 0.0 : (double) this.progress / this.maxProgress;
		} else
		{
			throw new IllegalArgumentException("Invalid GUI value: " + name);
		}
	}

	public int gaugeLiquidScaled(int i, int tank)
	{
		return switch (tank)
		{
			case 0 ->
			{
				if (this.inputTank.isEmpty())
				{
					yield 0;
				}

				yield this.inputTank.getFluidAmount() * i / this.inputTank.getCapacity();
			}
			case 1 ->
			{
				if (this.outputTank.isEmpty())
				{
					yield 0;
				}

				yield this.outputTank.getFluidAmount() * i / this.outputTank.getCapacity();
			}
			default -> 0;
		};
	}

	public Ic2rFluidTank getInputTank()
	{
		return this.inputTank;
	}

	public Ic2rFluidTank getOutputTank()
	{
		return this.outputTank;
	}

	@Override
	public double getEnergy()
	{
		return 40.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return true;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing
		);
	}
}
