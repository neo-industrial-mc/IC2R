package me.halfcooler.ic2r.core.block.machine.tileentity;

import me.halfcooler.ic2r.api.network.INetworkClientTileEntityEventListener;
import me.halfcooler.ic2r.api.upgrade.IUpgradableBlock;
import me.halfcooler.ic2r.api.upgrade.UpgradableProperty;
import me.halfcooler.ic2r.core.ChunkLoaderLogic;
import me.halfcooler.ic2r.core.ContainerBase;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.IHasGui;
import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.invslot.InvSlot;
import me.halfcooler.ic2r.core.block.invslot.InvSlotDischarge;
import me.halfcooler.ic2r.core.block.invslot.InvSlotUpgrade;
import me.halfcooler.ic2r.core.block.machine.container.ContainerChunkLoader;
import me.halfcooler.ic2r.core.block.tileentity.TileEntityInventory;
import me.halfcooler.ic2r.core.init.IC2RConfig;
import me.halfcooler.ic2r.core.network.GrowingBuffer;
import me.halfcooler.ic2r.core.profile.NotClassic;
import me.halfcooler.ic2r.core.ref.Ic2rBlockEntities;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.block.tileentity.ServerTicker;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;

@NotClassic
public class TileEntityChunkLoader extends TileEntityInventory implements INetworkClientTileEntityEventListener, IHasGui, IUpgradableBlock, ServerTicker
{
	public final InvSlotUpgrade upgradeSlot;
	public final InvSlotDischarge dischargeSlot;
	public final Energy energy;
	private final LongSet loadedChunks = new LongOpenHashSet();
	private final double euPerChunk = IC2RConfig.balance.euPerChunk.get();

	public TileEntityChunkLoader(BlockPos pos, BlockState state)
	{
		super(Ic2rBlockEntities.CHUNK_LOADER, pos, state);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
		this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.IO, 1, true, InvSlot.InvSide.ANY);
		this.energy = this.addComponent(Energy.asBasicSink(this, 2500.0, 1).addManagedSlot(this.dischargeSlot));
	}

	@Override
	public void updateEntityServer()
	{
		super.updateEntityServer();
		int recipePower = (int) (this.getLoadedChunks().size() * this.euPerChunk);
		this.energy.syncConsumerProfile(recipePower);
		boolean active = this.energy.useEnergy(recipePower);
		if (active != this.getActive())
		{
			this.setActive(active);
		}

		this.upgradeSlot.tick();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		ListTag list = nbt.getList("loadedChunks", 4);
		this.loadedChunks.clear();

		for (net.minecraft.nbt.Tag tag : list)
		{
			this.loadedChunks.add(((LongTag) tag).getAsLong());
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.saveAdditional(nbt, registries);
		ListTag list = new ListTag();
		nbt.put("loadedChunks", list);

		for (long pos : this.loadedChunks)
		{
			list.add(LongTag.valueOf(pos));
		}
	}

	@Override
	public void setActive(boolean active)
	{
		Level world = this.getLevel();
		if (!world.isClientSide && this.getActive() != active)
		{
			if (active)
			{
				ChunkLoaderLogic.addChunkLoader((ServerLevel) world, this.worldPosition, this.loadedChunks);
			} else
			{
				ChunkLoaderLogic.removeChunkLoader((ServerLevel) world, this.worldPosition);
			}
		}

		super.setActive(active);
	}

	@Override
	public void onLoaded()
	{
		super.onLoaded();
		Level world = this.getLevel();
		if (!world.isClientSide)
		{
			this.setOverclockRates();
			if (this.getActive())
			{
				ChunkLoaderLogic.addChunkLoader((ServerLevel) world, this.worldPosition, this.loadedChunks);
			}
		}
	}

	@Override
	public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing)
	{
		super.onPlaced(stack, placer, facing);
		this.loadedChunks.add(ChunkPos.asLong(this.worldPosition));
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerChunkLoader(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerChunkLoader(syncId, inventory, this);
	}

	public void addChunkToLoaded(ChunkPos chunk)
	{
		if (this.level.isClientSide)
		{
			new RuntimeException("Something tried to change the ChunkLoaderState on the client.").printStackTrace();
		} else if (!this.isChunkInRange(chunk))
		{
			IC2R.log.warn(LogCategory.Block, "Trying to add a Chunk to loaded, however the chunk is too far away. Aborting.");
		} else
		{
			if (this.loadedChunks.add(chunk.toLong()))
			{
				ChunkLoaderLogic.updateChunkLoader((ServerLevel) this.level, this.worldPosition, this.loadedChunks);
				this.setChanged();
			}
		}
	}

	public void removeChunkFromLoaded(ChunkPos chunk)
	{
		if (this.level.isClientSide)
		{
			new RuntimeException("Something tried to change the ChunkLoaderState on the client.").printStackTrace();
		} else if (ChunkPos.asLong(this.worldPosition) != chunk.toLong())
		{
			if (this.loadedChunks.remove(chunk.toLong()))
			{
				ChunkLoaderLogic.updateChunkLoader((ServerLevel) this.level, this.worldPosition, this.loadedChunks);
				this.setChanged();
			}
		}
	}

	public LongSet getLoadedChunks()
	{
		return this.loadedChunks;
	}

	public boolean isChunkInRange(ChunkPos chunk)
	{
		ChunkPos mainChunk = new ChunkPos(this.worldPosition);
		return Math.abs(chunk.x - mainChunk.x) <= 4 && Math.abs(chunk.z - mainChunk.z) <= 4;
	}

	public int getMaxChunks()
	{
		return 9;
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		int x = (event & 15) - 8;
		int z = (event >> 4 & 15) - 8;
		ChunkPos mainChunk = new ChunkPos(this.worldPosition);
		ChunkPos chunk = new ChunkPos(mainChunk.x + x, mainChunk.z + z);
		if (this.isChunkInRange(chunk))
		{
			if (this.getLoadedChunks().contains(chunk.toLong()))
			{
				this.removeChunkFromLoaded(chunk);
			} else
			{
				this.addChunkToLoaded(chunk);
			}
		}
	}

	@Override
	protected void onBlockBreak()
	{
		super.onBlockBreak();
		ChunkLoaderLogic.removeChunkLoader((ServerLevel) this.level, this.worldPosition);
	}

	@Override
	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.Transformer);
	}

	public void setOverclockRates()
	{
		this.upgradeSlot.onChanged();
		int tier = this.upgradeSlot.getTier(1);
		this.energy.setSinkTier(tier);
		this.dischargeSlot.setTier(tier);
		this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(2500, 0, 0));
		this.energy.syncConsumerProfile(0);
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if (IC2R.sideProxy.isSimulating())
		{
			this.setOverclockRates();
		}
	}
}
