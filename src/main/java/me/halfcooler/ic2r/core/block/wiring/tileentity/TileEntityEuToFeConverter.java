package me.halfcooler.ic2r.core.block.wiring.tileentity;

import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;
import me.halfcooler.ic2r.core.energy.EnergyBridgeMath;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.forge.EuToFeEnergyStorage;
import me.halfcooler.ic2r.platform.services.PlatformServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class TileEntityEuToFeConverter extends Ic2rTileEntity implements ServerTicker
{
	/** EU buffer size. */
	public static final double CAPACITY_EU = 10_000.0;
	/** Max EU converted / pushed per tick (FE = EU × {@link EnergyBridgeMath#DEFAULT_FE_PER_EU}). */
	public static final double MAX_TRANSFER_EU_PER_TICK = 2_048.0;
	/** Accept up to EV packets so any cable tier can feed the converter. */
	public static final int SINK_TIER = 4;

	private final Energy energy;
	private final EuToFeEnergyStorage feStorage;

	public TileEntityEuToFeConverter(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.EU_TO_FE_CONVERTER, pos, state);
		this.energy = this.addComponent(Energy.asBasicSink(this, CAPACITY_EU, SINK_TIER));
		this.energy.configureEnergyBuffer(4);
		this.feStorage = new EuToFeEnergyStorage(this.energy);
	}

	/** FE capability entry used by NeoForge registration (extract-only). */
	@Nullable
	public IEnergyStorage getFeStorage(@Nullable Direction side)
	{
		// Exposed on every side so FE pipes can pull; receive is always false.
		return this.feStorage;
	}

	public Energy getEnergyComponent()
	{
		return this.energy;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean transferred = pushFeToFacing();
		if (this.getActive() != transferred)
		{
			this.setActive(transferred);
		}
	}

	/**
	 * Actively inject FE into the neighbor at {@link #getFacing()}.
	 *
	 * @return true if any FE was accepted this tick
	 */
	private boolean pushFeToFacing()
	{
		double euAvailable = this.energy.getEnergy();
		if (euAvailable <= 0.0)
		{
			return false;
		}

		Level level = this.getLevel();
		if (level == null || level.isClientSide)
		{
			return false;
		}

		double euOffer = Math.min(euAvailable, MAX_TRANSFER_EU_PER_TICK);
		long feOffer = EnergyBridgeMath.euToFeFloor(euOffer);
		if (feOffer <= 0L)
		{
			return false;
		}

		Direction facing = this.getFacing();
		BlockPos neighborPos = this.worldPosition.relative(facing);
		BlockEntity neighbor = level.getBlockEntity(neighborPos);
		if (neighbor == null)
		{
			return false;
		}

		// Insert into the face of the neighbor that touches us (opposite of our facing).
		long accepted = PlatformServices.energy().insert(neighbor, facing.getOpposite(), feOffer, false);
		if (accepted <= 0L)
		{
			return false;
		}

		double leftoverEu = EnergyBridgeMath.residualEuAfterFeTransfer(euOffer, accepted);
		double spentEu = euOffer - leftoverEu;
		if (spentEu > 0.0)
		{
			this.energy.useEnergy(spentEu, false);
			return true;
		}

		return false;
	}
}
