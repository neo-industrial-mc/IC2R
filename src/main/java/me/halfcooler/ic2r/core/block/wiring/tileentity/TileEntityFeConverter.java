package me.halfcooler.ic2r.core.block.wiring.tileentity;

import me.halfcooler.ic2r.api.energy.EnergyNet;
import me.halfcooler.ic2r.api.energy.tile.IEnergyTile;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;
import me.halfcooler.ic2r.core.energy.EnergyBridgeMath;
import me.halfcooler.ic2r.core.network.sync.BlockEntitySync;
import me.halfcooler.ic2r.core.network.sync.SyncCodecs;
import me.halfcooler.ic2r.core.network.sync.SyncKey;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.util.Util;
import me.halfcooler.ic2r.forge.EuToFeEnergyStorage;
import me.halfcooler.ic2r.platform.services.PlatformServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One-way EU → FE converter.
 * <p>
 * Face ports (client-synced): {@link PortMode#NONE} gray, {@link PortMode#EU} red, {@link PortMode#FE} yellow.
 */
public class TileEntityFeConverter extends Ic2rTileEntity implements ServerTicker
{
	/** EU buffer size. */
	public static final double CAPACITY_EU = 10_000.0;
	/** Max EU converted / pushed per tick (FE = EU × {@link EnergyBridgeMath#DEFAULT_FE_PER_EU}). */
	public static final double MAX_TRANSFER_EU_PER_TICK = 2_048.0;
	/** Accept up to EV packets so any cable tier can feed the converter. */
	public static final int SINK_TIER = 4;

	public static final SyncKey<Integer> FACE_MODES = SyncKey.of("face_modes", SyncCodecs.INT);

	/** 2 bits per {@link Direction} ordinal: 0=none, 1=eu, 2=fe. */
	private int faceModesPacked;
	private final Energy energy;
	private final EuToFeEnergyStorage feStorage;

	public TileEntityFeConverter(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.FE_CONVERTER, pos, state);
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

	/** Packed face modes for model rendering (2 bits × 6 faces). */
	public int getFaceModesPacked()
	{
		return this.faceModesPacked;
	}

	public PortMode getPortMode(Direction side)
	{
		int mode = (this.faceModesPacked >> (side.ordinal() * 2)) & 0x3;
		return PortMode.byId(mode);
	}

	@Override
	protected void registerSyncedData(BlockEntitySync sync)
	{
		super.registerSyncedData(sync);
		sync.add(FACE_MODES, this::getFaceModesPacked, this::applyFaceModesPacked, "faceModesPacked");
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("faceModesPacked");
		return ret;
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		super.onNetworkUpdate(field);
		if ("faceModesPacked".equals(field) || FACE_MODES.wireName().equals(field))
		{
			this.rerender();
		}
	}

	private void applyFaceModesPacked(int packed)
	{
		if (this.faceModesPacked != packed)
		{
			this.faceModesPacked = packed;
			if (this.getLevel() != null && this.getLevel().isClientSide)
			{
				this.rerender();
			}
		}
	}

	@Override
	protected void loadAdditional(@NotNull CompoundTag nbt, net.minecraft.core.HolderLookup.@NotNull Provider registries)
	{
		super.loadAdditional(nbt, registries);
		this.faceModesPacked = nbt.getInt("faceModes");
	}

	@Override
	public void saveAdditional(@NotNull CompoundTag nbt, net.minecraft.core.HolderLookup.@NotNull Provider registries)
	{
		super.saveAdditional(nbt, registries);
		nbt.putInt("faceModes", this.faceModesPacked);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (this.getLevel() != null && !this.getLevel().isClientSide)
		{
			this.updateFaceModes(true);
		}
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		if (this.getLevel() != null && !this.getLevel().isClientSide)
		{
			this.updateFaceModes(false);
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		// Neighbours may gain/lose FE capability without a block update (e.g. TE load order).
		this.updateFaceModes(false);
		boolean transferred = this.pushFeToNeighbors();
		if (this.getActive() != transferred)
		{
			this.setActive(transferred);
		}
	}

	/**
	 * Refresh per-face port colours from neighbours.
	 * EU EnergyNet tiles → red; FE receivers → yellow; otherwise gray.
	 */
	private void updateFaceModes(boolean forceSync)
	{
		Level level = this.getLevel();
		if (level == null || level.isClientSide)
		{
			return;
		}

		int packed = 0;
		for (Direction dir : Util.ALL_DIRS)
		{
			PortMode mode = this.detectPortMode(level, dir);
			packed |= mode.id << (dir.ordinal() * 2);
		}

		if (!forceSync && packed == this.faceModesPacked)
		{
			return;
		}

		this.faceModesPacked = packed;
		IC2R.network.get(true).updateTileEntityField(this, "faceModesPacked");
		this.setChanged();
	}

	private PortMode detectPortMode(Level level, Direction dir)
	{
		BlockPos neighborPos = this.worldPosition.relative(dir);

		// Prefer EU network connection (cables / IC machines on EnergyNet).
		IEnergyTile euTile = EnergyNet.instance.getTile(level, neighborPos);
		if (euTile == null)
		{
			euTile = EnergyNet.instance.getSubTile(level, neighborPos);
		}
		if (euTile != null && !(euTile instanceof TileEntityFeConverter))
		{
			return PortMode.EU;
		}

		// FE sink on the face that touches us.
		BlockEntity neighbor = level.getBlockEntity(neighborPos);
		if (neighbor != null && !(neighbor instanceof TileEntityFeConverter)
			&& PlatformServices.energy().canReceive(neighbor, dir.getOpposite()))
		{
			return PortMode.FE;
		}

		return PortMode.NONE;
	}

	/**
	 * Push FE into every neighbour that can receive it (yellow ports).
	 *
	 * @return true if any FE was accepted this tick
	 */
	private boolean pushFeToNeighbors()
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

		boolean any = false;
		// Prefer wrench facing first, then other sides, so facing still matters under load.
		Direction facing = this.getFacing();
		if (this.pushFeToSide(level, facing, euAvailable))
		{
			any = true;
			euAvailable = this.energy.getEnergy();
		}

		for (Direction dir : Util.ALL_DIRS)
		{
			if (dir == facing)
			{
				continue;
			}
			if (euAvailable <= 0.0)
			{
				break;
			}
			if (this.pushFeToSide(level, dir, euAvailable))
			{
				any = true;
				euAvailable = this.energy.getEnergy();
			}
		}

		return any;
	}

	private boolean pushFeToSide(Level level, Direction dir, double euAvailable)
	{
		// Only push toward faces that look like FE outputs (or unknown yet on first ticks).
		PortMode mode = this.getPortMode(dir);
		if (mode == PortMode.EU)
		{
			return false;
		}

		BlockPos neighborPos = this.worldPosition.relative(dir);
		BlockEntity neighbor = level.getBlockEntity(neighborPos);
		if (neighbor == null)
		{
			return false;
		}

		double euOffer = Math.min(euAvailable, MAX_TRANSFER_EU_PER_TICK);
		long feOffer = EnergyBridgeMath.euToFeFloor(euOffer);
		if (feOffer <= 0L)
		{
			return false;
		}

		long accepted = PlatformServices.energy().insert(neighbor, dir.getOpposite(), feOffer, false);
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

	/** Visual / logical port state for one face. */
	public enum PortMode
	{
		NONE(0),
		EU(1),
		FE(2);

		public final int id;

		PortMode(int id)
		{
			this.id = id;
		}

		public static PortMode byId(int id)
		{
			return switch (id)
			{
				case 1 -> EU;
				case 2 -> FE;
				default -> NONE;
			};
		}
	}
}
