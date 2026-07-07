package ic2.core.block.generator.tileentity;

import ic2.api.energy.profile.VoltageTier;
import ic2.api.energy.tile.IKineticSource;
import ic2.core.energy.profile.ElectricalDisplay;
import ic2.core.init.IC2Config;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.Ic2Tooltip;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityKineticGenerator extends TileEntityConversionGenerator
{
	private static final VoltageTier MAX_OUTPUT_VOLTAGE = VoltageTier.HV;
	private final double euPerKu = 0.25 * IC2Config.balance.energy.generator.kinetic.get().floatValue();
	protected IKineticSource source;

	public TileEntityKineticGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.KINETIC_GENERATOR, pos, state);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateSource();
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		this.updateSource();
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		if (this.getBlockPos().relative(this.getFacing()).equals(neighborPos))
		{
			this.updateSource();
		}
	}

	protected void updateSource()
	{
		BlockEntity te = this.level.getBlockEntity(this.worldPosition.relative(this.getFacing()));
		if (te instanceof IKineticSource kineticSource && !te.isRemoved())
		{
			this.source = kineticSource;
		} else
		{
			this.source = null;
		}
	}

	@Override
	protected int getEnergyAvailable()
	{
		if (this.source != null)
		{
			assert !((BlockEntity) this.source).isRemoved();
			return this.source.drawKineticEnergy(this.getFacing().getOpposite(), this.source.getConnectionBandwidth(this.getFacing().getOpposite()), true);
		} else
		{
			return 0;
		}
	}

	@Override
	protected void drawEnergyAvailable(int amount)
	{
		if (this.source != null)
		{
			assert !((BlockEntity) this.source).isRemoved();
			this.source.drawKineticEnergy(this.getFacing().getOpposite(), amount, false);
		} else
		{
			assert false;
		}
	}

	@Override
	protected double getMultiplier()
	{
		return this.euPerKu;
	}

	@Override
	protected double clampOutputEuPerTick(double outputEuPerTick)
	{
		double maxOutput = MAX_OUTPUT_VOLTAGE.getVoltage() * this.getMaxSourceAmperage();
		return Math.min(outputEuPerTick, maxOutput);
	}

	@Override
	protected VoltageTier clampOutputVoltageTier(VoltageTier tier)
	{
		return tier.getIcTier() > MAX_OUTPUT_VOLTAGE.getIcTier() ? MAX_OUTPUT_VOLTAGE : tier;
	}

	@Override
	public void appendItemTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag advanced)
	{
		Ic2Tooltip.add(
			tooltip,
			Component.translatable("ic2.KineticGenerator.tooltip.max_output_voltage", ElectricalDisplay.formatTierWithValue(MAX_OUTPUT_VOLTAGE))
		);
	}
}
