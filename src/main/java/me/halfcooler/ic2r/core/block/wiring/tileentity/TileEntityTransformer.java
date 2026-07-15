package me.halfcooler.ic2r.core.block.wiring.tileentity;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.profile.VoltageTier;
import me.halfcooler.ic2r.api.network.INetworkClientTileEntityEventListener;
import me.halfcooler.ic2r.api.network.NetworkHelper;
import me.halfcooler.ic2r.core.energy.EnergyNetMode;
import me.halfcooler.ic2r.core.energy.profile.ElectricalDisplay;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.block.wiring.ContainerTransformer;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.util.Ic2rTooltip;

import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityTransformer extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener, ServerTicker
{
	private static final TileEntityTransformer.Mode defaultMode = TileEntityTransformer.Mode.redstone;
	protected final Energy energy;
	private final int defaultTier;
	private double inputFlow = 0.0;
	private double outputFlow = 0.0;
	private TileEntityTransformer.Mode configuredMode = defaultMode;
	private TileEntityTransformer.Mode transformMode = null;

	public TileEntityTransformer(BlockEntityType<? extends TileEntityTransformer> type, BlockPos pos, BlockState state, int tier)
	{
		super(type, pos, state);
		this.defaultTier = tier;
		this.energy = this.addComponent(new Energy(this, EnergyNet.instance.getPowerFromTier(tier) * 8.0, Collections.emptySet(), Collections.emptySet(), tier, tier, true).setMultiSource(true));
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		int mode = nbt.getInt("mode");
		if (mode >= 0 && mode < TileEntityTransformer.Mode.VALUES.length)
		{
			this.configuredMode = TileEntityTransformer.Mode.VALUES[mode];
		} else
		{
			this.configuredMode = defaultMode;
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("mode", this.configuredMode.ordinal());
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide)
		{
			this.updateRedstone(true);
		}
	}

	public TileEntityTransformer.Mode getMode()
	{
		return this.configuredMode;
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event >= 0 && event < TileEntityTransformer.Mode.VALUES.length)
		{
			this.configuredMode = TileEntityTransformer.Mode.VALUES[event];
			this.updateRedstone(false);
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.updateRedstone(false);
	}

	private void updateRedstone(boolean force)
	{
		assert !this.getLevel().isClientSide;

		TileEntityTransformer.Mode newMode = switch (this.configuredMode)
		{
			case redstone ->
				this.getLevel().hasNeighborSignal(this.worldPosition) ? TileEntityTransformer.Mode.stepUp : TileEntityTransformer.Mode.stepDown;
			case stepDown, stepUp -> this.configuredMode;
		};

		if (!force && this.transformMode != null && this.transformMode != newMode)
		{
			if (this.energy.applyTransformerModeSwitch(newMode, this.transformMode))
			{
				return;
			}
		}

		this.energy.setEnabled(true);
		if (force || this.transformMode != newMode)
		{
			TileEntityTransformer.Mode previousMode = this.transformMode;
			this.transformMode = newMode;
			if (previousMode != newMode)
			{
				NetworkHelper.updateTileEntityField(this, "transformMode");
			}

			this.setActive(this.isStepUp());
			if (this.isStepUp())
			{
				this.energy.setSourceTier(this.defaultTier + 1);
				this.energy.setSinkTier(this.defaultTier);
				this.energy.setPacketOutput(1);
				this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing())), EnumSet.of(this.getFacing()));
			} else
			{
				this.energy.setSourceTier(this.defaultTier);
				this.energy.setSinkTier(this.defaultTier + 1);
				this.energy.setPacketOutput(4);
				this.energy.setDirections(EnumSet.of(this.getFacing()), EnumSet.complementOf(EnumSet.of(this.getFacing())));
			}

			this.outputFlow = EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier());
			this.inputFlow = EnergyNet.instance.getPowerFromTier(this.energy.getSinkTier());
			this.energy.configureTransformerProfile(this.isStepUp());
		}
	}

	@Override
	public void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		if (!this.getLevel().isClientSide)
		{
			this.updateRedstone(true);
		}
	}

	@Override
	public void appendItemTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag advanced)
	{
		if (EnergyNetMode.fromConfig(IC2RConfig.misc.useGregTechEnergyNet.get()) != EnergyNetMode.GT)
		{
			super.appendItemTooltip(stack, tooltip, advanced);
		}

		Ic2rTooltip.add(tooltip, Component.translatable("ic2r.Transformer.tooltip.high", this.formatRatedPower(true)));
		Ic2rTooltip.add(tooltip, Component.translatable("ic2r.Transformer.tooltip.low", this.formatRatedPower(false)));
	}

	public Component getInputFlowDisplay()
	{
		return this.getFlowDisplay(true);
	}

	public Component getOutputFlowDisplay()
	{
		return this.getFlowDisplay(false);
	}

	private Component getFlowDisplay(boolean input)
	{
		boolean stepUp = this.isStepUp();
		int amps;
		VoltageTier tier;
		if (input)
		{
			amps = stepUp ? 4 : 1;
			tier = stepUp ? this.getLowTier() : this.getHighTier();
		} else
		{
			amps = stepUp ? 1 : 4;
			tier = stepUp ? this.getHighTier() : this.getLowTier();
		}

		return ElectricalDisplay.formatPower(tier.getVoltage() * amps, tier, amps);
	}

	private Component formatRatedPower(boolean high)
	{
		VoltageTier tier = high ? this.getHighTier() : this.getLowTier();
		int amps = high ? 1 : 4;
		return ElectricalDisplay.formatPowerCompact(tier.getVoltage() * amps, tier, amps);
	}

	private VoltageTier getLowTier()
	{
		return VoltageTier.fromIcTier(this.defaultTier);
	}

	private VoltageTier getHighTier()
	{
		return VoltageTier.fromIcTier(this.defaultTier + 1);
	}

	@Override
	public ContainerBase<TileEntityTransformer> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerTransformer(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerTransformer(syncId, inventory, this);
	}

	public double getInputFlow()
	{
		return !this.isStepUp() ? this.inputFlow : this.outputFlow;
	}

	public double getOutputFlow()
	{
		return this.isStepUp() ? this.inputFlow : this.outputFlow;
	}

	private boolean isStepUp()
	{
		if (this.transformMode != null)
		{
			return this.transformMode == TileEntityTransformer.Mode.stepUp;
		}

		return switch (this.configuredMode)
		{
			case stepUp -> true;
			case stepDown -> false;
			case redstone -> this.getLevel() != null && this.getLevel().hasNeighborSignal(this.worldPosition);
		};
	}

	public enum Mode
	{
		redstone, stepDown, stepUp;

		static final TileEntityTransformer.Mode[] VALUES = values();
	}
}
