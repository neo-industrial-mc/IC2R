package me.halfcooler.ic2r.core.block.storage.tank;

import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.api.network.INetworkClientTileEntityEventListener;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Fluids;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.fluid.FluidBeBridge;
import me.halfcooler.ic2r.core.fluid.FluidHandler;
import me.halfcooler.ic2r.core.fluid.FluidTankInfo;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidBlock;
import me.halfcooler.ic2r.core.fluid.Ic2rFluidStack;
import me.halfcooler.ic2r.core.gui.dynamic.DynamicContainer;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.network.GuiSynced;
import me.halfcooler.ic2r.core.proxy.SideProxyClient;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;
import me.halfcooler.ic2r.core.util.LiquidUtil;
import me.halfcooler.ic2r.core.util.StackUtil;

import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;


import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class TileEntityTank extends TileEntityInventory implements IHasGui, Ic2rFluidBlock, FluidBeBridge, INetworkClientTileEntityEventListener, IUpgradableBlock, ServerTicker
{
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	protected final Fluids fluidsComponent = this.addComponent(new Fluids(this));
	@GuiSynced
	protected final Fluids.InternalFluidTank contents;

	public TileEntityTank(BlockEntityType<? extends TileEntityTank> type, BlockPos pos, BlockState state, int bucketMultiplier)
	{
		super(type, pos, state);
		this.contents = this.fluidsComponent.addTank("contents", 1000 * bucketMultiplier);
	}

	@Override
	public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing)
	{
		super.onPlaced(stack, placer, facing);
		if (!this.getLevel().isClientSide)
		{
			CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
			this.contents.fromNbt(nbt);
		}
	}

	@Override
	public ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(drop);
		if (this.contents.getFluidAmount() > 0)
		{
			this.contents.toNbt(nbt);
		}

		return drop;
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		if (LiquidUtil.transferFluidFromHandClick(player, hand, this.contents, player.isShiftKeyDown()))
		{
			this.setChanged();
			return InteractionResult.SUCCESS;
		}

		return super.onActivated(player, hand, side, hit);
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event == 0 || event == 1)
		{
			LiquidUtil.transferFluidFromGuiClick(player, this.contents, event == 1);
		}
	}

	@Override
	public void appendItemTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag advanced)
	{
		Ic2rTooltip.add(tooltip, Component.translatable("item.ic2r.tank.tooltip", contents.getCapacity()));
		CompoundTag nbt = stack.getTag();
		if (nbt != null && !nbt.contains("Empty"))
		{
			Ic2rFluidStack fluidStack = Ic2rFluidStack.read(nbt);
			if (fluidStack != null && !fluidStack.isEmpty())
			{
				Ic2rTooltip.add(tooltip, Component.literal(SideProxyClient.envProxy.getFluidName(fluidStack)));
				Ic2rTooltip.add(tooltip, Component.translatable("ic2r.generic.text.amount", fluidStack.getAmountMb()));
				Ic2rTooltip.add(tooltip, Component.literal("Type: " + (FluidHandler.isGaseous(fluidStack.getFluid()) ? "Gas" : "Liquid")));
				return;
			}
		}
		Ic2rTooltip.add(tooltip, Component.translatable("ic2r.item.fluid_container.empty"));
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

	// ---- Ic2rFluidBlock + FluidBeBridge ----

	@Override
	public Ic2rFluidBlock getFluidBlock()
	{
		return this;
	}

	@Override
	public boolean isFluidBlock(BlockState state, Level world, BlockPos pos, BlockEntity be)
	{
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfos(BlockState state, Level world, BlockPos pos, BlockEntity be)
	{
		return this.fluidsComponent.getTankInfos();
	}

	@Override
	public Ic2rFluidStack drainMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, int amount, boolean simulate)
	{
		return this.fluidsComponent.drainMb(side, amount, simulate);
	}

	@Override
	public int drainMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, Ic2rFluidStack drainFs, boolean simulate)
	{
		return this.fluidsComponent.drainMb(side, drainFs, simulate);
	}

	@Override
	public int fillMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, Ic2rFluidStack fillFs, boolean simulate)
	{
		return this.fluidsComponent.fillMb(side, fillFs, simulate);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.upgradeSlot.tick();
	}

	@Override
	public double getEnergy()
	{
		return 0.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return false;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
	}
}
