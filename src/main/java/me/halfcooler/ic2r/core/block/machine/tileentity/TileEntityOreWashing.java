package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.recipe.IRecipeInput;
import me.halfcooler.ic2r.api.recipe.MachineRecipeResult;
import me.halfcooler.ic2r.api.recipe.Recipes;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquid;
import me.halfcooler.ic2r.core.block.invslot.InvSlotConsumableLiquidByList;
import me.halfcooler.ic2r.core.block.invslot.InvSlotOutput;
import me.halfcooler.ic2r.core.block.invslot.InvSlotProcessableGeneric;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidTank;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityOreWashing extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public final InvSlotConsumableLiquid fluidSlot;
	public final InvSlotOutput cellSlot;
	@GuiSynced
	protected final Ic2rFluidTank fluidTank;
	protected final Fluids fluids;

	public TileEntityOreWashing(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.ORE_WASHING_PLANT, pos, state, 16, 500, 3);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.oreWashing);
		this.fluidSlot = new InvSlotConsumableLiquidByList(this, "fluid", 1, net.minecraft.world.level.material.Fluids.WATER);
		this.cellSlot = new InvSlotOutput(this, "cell", 1);
		this.fluids = this.addComponent(new Fluids(this));
		this.fluidTank = this.fluids.addTankInsert("fluid", 8000, Fluids.fluidPredicate(net.minecraft.world.level.material.Fluids.WATER));
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.fluidTank.getFluidAmount() < this.fluidTank.getCapacity())
		{
			this.gainFluid();
		}
	}

	@Override
	public void operateOnce(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output, Collection<ItemStack> processResult)
	{
		super.operateOnce(output, processResult);
		this.fluidTank.drainMbUnchecked(output.recipe().getMetaData().getInt("amount"), false);
	}

	@Override
	public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getRecipeResult()
	{
		MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> ret = super.getRecipeResult();
		if (ret != null)
		{
			if (ret.recipe().getMetaData() == null)
			{
				return null;
			}

			if (ret.recipe().getMetaData().getInt("amount") > this.fluidTank.getFluidAmount())
			{
				return null;
			}
		}

		return ret;
	}

	public boolean gainFluid()
	{
		return this.fluidSlot.processIntoTank(this.fluidTank, this.cellSlot);
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.Processing,
			UpgradableProperty.Transformer,
			UpgradableProperty.EnergyStorage,
			UpgradableProperty.ItemConsuming,
			UpgradableProperty.ItemProducing,
			UpgradableProperty.FluidConsuming
		);
	}
}
