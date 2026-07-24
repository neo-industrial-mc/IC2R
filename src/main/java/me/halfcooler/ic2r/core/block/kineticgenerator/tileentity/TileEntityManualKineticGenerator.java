package me.halfcooler.ic2r.core.block.kineticgenerator.tileentity;

import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.util.Util;
import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@NotClassic
public class TileEntityManualKineticGenerator extends TileEntityAbstractKineticGenerator implements ServerTicker
{
	public static final int maxClicksPerTick = 10;
	public int clicks;

	public TileEntityManualKineticGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.MANUAL_KINETIC_GENERATOR, pos, state);
		this.maxKuBuffer = 1000;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.clicks = 0;
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		this.playerClicked(player);
		return InteractionResult.SUCCESS;
	}

	private void playerClicked(Player player)
	{
		if (player.getFoodData().getFoodLevel() > 6)
		{
			if (player instanceof ServerPlayer)
			{
				if (this.clicks < 10)
				{
					int ku;
					if (!Util.isFakePlayer(player, false))
					{
						ku = 400;
					} else
					{
						ku = 20;
					}

					ku = ku * Math.round(IC2RConfig.balance.energy.kineticGenerator.manual.get().floatValue());
					this.kuBuffer = Math.min(this.kuBuffer + ku, this.maxKuBuffer);
					player.causeFoodExhaustion(0.25F);
					this.clicks++;
				}
			}
		}
	}

	@Override
	public int maxrequestkineticenergyTick(Direction directionFrom)
	{
		return this.drawKineticEnergy(directionFrom, Integer.MAX_VALUE, true);
	}

	@Override
	public int getConnectionBandwidth(Direction side)
	{
		return this.maxKuBuffer;
	}

	@Override
	public int drawKineticEnergy(Direction side, int request, boolean simulate)
	{
		int max = Math.min(this.kuBuffer, request);
		if (!simulate)
		{
			this.kuBuffer -= max;
		}

		return max;
	}
}
