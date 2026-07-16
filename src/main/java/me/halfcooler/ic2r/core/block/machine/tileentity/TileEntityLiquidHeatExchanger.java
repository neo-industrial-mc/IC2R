package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.recipe.ILiquidHeatExchangerManager;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.LiquidHeatExchangerManager;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumable;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableItemStack;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquid;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquidByManager;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquidByTank;
import me.halfcooler.ic2r.core.block.invslot.InvSlotOutput;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.block.machine.container.ContainerLiquidHeatExchanger;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityHeatSourceInventory;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.ref.Ic2rFluids;
import me.halfcooler.ic2r.core.ref.Ic2rItems;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class TileEntityLiquidHeatExchanger extends TileEntityHeatSourceInventory implements IHasGui, IUpgradableBlock
{
	public final Ic2rFluidTank inputTank;
	public final Ic2rFluidTank outputTank;
	public final InvSlotConsumable heatexchangerslots;
	public final InvSlotOutput hotoutputSlot;
	public final InvSlotOutput cooloutputSlot;
	public final InvSlotConsumableLiquid hotfluidinputSlot;
	public final InvSlotConsumableLiquid coolfluidinputSlot;
	public final InvSlotUpgrade upgradeSlot;
	protected final Fluids fluids = this.addComponent(new Fluids(this));
	private boolean newActive;

	public TileEntityLiquidHeatExchanger(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.LIQUID_HEAT_EXCHANGER, pos, state);
		this.inputTank = this.fluids.addTankInsert("inputTank", 2000, Fluids.fluidPredicate(Recipes.liquidCooldownManager));
		this.outputTank = this.fluids.addTankExtract("outputTank", 2000);
		this.heatexchangerslots = new InvSlotConsumableItemStack(this, "heatExchanger", 10, new ItemStack(Ic2rItems.HEAT_CONDUCTOR));
		this.heatexchangerslots.setStackSizeLimit(1);
		this.hotoutputSlot = new InvSlotOutput(this, "hotOutputSlot", 1);
		this.cooloutputSlot = new InvSlotOutput(this, "outputSlot", 1);
		this.hotfluidinputSlot = new InvSlotConsumableLiquidByManager(
			this, "hotFluidInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, Recipes.liquidCooldownManager
		);
		this.coolfluidinputSlot = new InvSlotConsumableLiquidByTank(
			this, "coolFluidOutput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, this.outputTank
		);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 3);
		this.newActive = false;
	}

	public static void init()
	{
		Recipes.liquidCooldownManager = new LiquidHeatExchangerManager(false);
		Recipes.liquidHeatUpManager = new LiquidHeatExchangerManager(true);
		IC2R.envProxy
			.runAfterRegistryInit(
				() ->
				{
					addCooldownRecipe(
						net.minecraft.world.level.material.Fluids.LAVA,
						Ic2rFluids.PAHOEHOE_LAVA.still(),
						Math.round(20.0F * IC2RConfig.balance.energy.fluidConversion.heatExchangerLava.get().floatValue())
					);
					addBiDiRecipe(
						Ic2rFluids.HOT_COOLANT.still(),
						Ic2rFluids.COOLANT.still(),
						Math.round(20.0F * IC2RConfig.balance.energy.fluidConversion.heatExchangerHotCoolant.get().floatValue())
					);
					addHeatupRecipe(
						Ic2rFluids.HOT_WATER.still(),
						net.minecraft.world.level.material.Fluids.WATER,
						Math.round(IC2RConfig.balance.energy.fluidConversion.heatExchangerWater.get().floatValue())
					);
				}
			);
	}

	public static void addBiDiRecipe(Fluid hotFluid, Fluid coldFluid, int huPerMB)
	{
		addHeatupRecipe(hotFluid, coldFluid, huPerMB);
		addCooldownRecipe(hotFluid, coldFluid, huPerMB);
	}

	public static void addHeatupRecipe(Fluid hotFluid, Fluid coldFluid, int huPerMB)
	{
		Recipes.liquidHeatUpManager.addFluid(coldFluid, hotFluid, huPerMB);
	}

	public static void addCooldownRecipe(Fluid hotFluid, Fluid coldFluid, int huPerMB)
	{
		Recipes.liquidCooldownManager.addFluid(hotFluid, coldFluid, huPerMB);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.hotfluidinputSlot.processIntoTank(this.inputTank, this.hotoutputSlot);
		this.coolfluidinputSlot.processFromTank(this.outputTank, this.cooloutputSlot);
		this.newActive = this.HeatBuffer > 0;
		if (this.getActive() != this.newActive)
		{
			this.setActive(this.newActive);
		}

		this.upgradeSlot.tick();
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerLiquidHeatExchanger(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerLiquidHeatExchanger(syncId, inventory, this);
	}

	@Override
	public int getMaxHeatEmittedPerTick()
	{
		int count = 0;

		for (int i = 0; i < this.heatexchangerslots.size(); i++)
		{
			if (!this.heatexchangerslots.isEmpty(i))
			{
				count += 10;
			}
		}

		return count;
	}

	@Override
	protected int fillHeatBuffer(int bufferSpace)
	{
		if (bufferSpace > 0)
		{
			int AmountHotCoolant = this.inputTank.getFluidAmount();
			int OutputTankFreeCap = this.outputTank.getCapacity() - this.outputTank.getFluidAmount();
			Ic2rFluidStack drainCoolant;
			if (OutputTankFreeCap == 0 || AmountHotCoolant == 0)
			{
				return 0;
			}

			Fluid fluidInputTank = this.inputTank.getFluidStack().getFluid();
			Fluid fluidOutput = null;
			int hUper1mb = 0;
			if (Recipes.liquidCooldownManager.acceptsFluid(fluidInputTank))
			{
				ILiquidHeatExchangerManager.HeatExchangeProperty hep = Recipes.liquidCooldownManager.getHeatExchangeProperty(fluidInputTank);
				fluidOutput = hep.outputFluid();
				hUper1mb = hep.huPerMB();
			}

			if (fluidOutput == null)
			{
				return 0;
			}

			if (this.outputTank.getFluidAmount() > 0 && !this.outputTank.getFluidStack().hasExactFluid(fluidOutput))
			{
				return 0;
			}

			int mbtofillheatbuffer = bufferSpace / hUper1mb;
			if (OutputTankFreeCap >= AmountHotCoolant)
			{
				if (mbtofillheatbuffer <= AmountHotCoolant)
				{
					drainCoolant = this.inputTank.drainMbUnchecked(mbtofillheatbuffer, true);
				} else
				{
					drainCoolant = this.inputTank.drainMbUnchecked(AmountHotCoolant, true);
				}
			} else if (mbtofillheatbuffer <= OutputTankFreeCap)
			{
				drainCoolant = this.inputTank.drainMbUnchecked(mbtofillheatbuffer, true);
			} else
			{
				drainCoolant = this.inputTank.drainMbUnchecked(OutputTankFreeCap * 20, true);
			}

			if (drainCoolant != null)
			{
				this.inputTank.drainMbUnchecked(drainCoolant.getAmountMb(), false);
				this.outputTank.fillMbUnchecked(Ic2rFluidStack.create(fluidOutput, drainCoolant.getAmountMb()), false);
				return drainCoolant.getAmountMb() * hUper1mb;
			}
		}

		return 0;
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
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing
		);
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
}
