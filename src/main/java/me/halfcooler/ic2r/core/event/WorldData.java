package me.halfcooler.ic2r.core.event;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.halfcooler.ic2r.core.WindSim;
import me.halfcooler.ic2r.core.block.personal.TradingMarket;
import me.halfcooler.ic2r.core.energy.grid.EnergyNetLocal;
import me.halfcooler.ic2r.core.network.TeUpdateDataServer;
import me.halfcooler.ic2r.core.util.Util;
import me.halfcooler.ic2r.platform.services.PlatformServices;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class WorldData
{
	private static final ConcurrentMap<ResourceLocation, WorldData> idxClient = PlatformServices.lifecycle().isClient() ? new ConcurrentHashMap<>() : null;
	private static final ConcurrentMap<ResourceLocation, WorldData> idxServer = new ConcurrentHashMap<>();
	public final EnergyNetLocal energyNet;
	public final Map<BlockEntity, TeUpdateDataServer> tesToUpdate = new IdentityHashMap<>();
	public final TradingMarket tradeMarket;
	public final WindSim windSim;
	public final Map<LevelChunk, CompoundTag> worldGenData = new IdentityHashMap<>();
	public final Set<LevelChunk> chunksToDecorate = Collections.newSetFromMap(new IdentityHashMap<>());
	public final Set<LevelChunk> pendingUnloadChunks = Collections.newSetFromMap(new IdentityHashMap<>());
	public final Long2ObjectMap<Set<BlockPos>> loadedChunks;
	public final Object2ObjectMap<BlockPos, LongSet> chunkLoaders;
	final Queue<IWorldTickCallback> singleUpdates = new ConcurrentLinkedQueue<>();
	final Set<IWorldTickCallback> continuousUpdates = new HashSet<>();
	final List<IWorldTickCallback> continuousUpdatesToAdd = new ArrayList<>();
	final List<IWorldTickCallback> continuousUpdatesToRemove = new ArrayList<>();
	boolean continuousUpdatesInUse = false;

	private WorldData(Level world)
	{
		if (!world.isClientSide)
		{
			this.energyNet = EnergyNetLocal.create(world);
			this.tradeMarket = new TradingMarket(world);
			this.windSim = new WindSim(world);
			this.loadedChunks = new Long2ObjectOpenHashMap<>();
			this.chunkLoaders = new Object2ObjectOpenHashMap<>();
		} else
		{
			this.energyNet = null;
			this.tradeMarket = null;
			this.windSim = null;
			this.loadedChunks = null;
			this.chunkLoaders = null;
		}
	}

	public static WorldData get(Level world)
	{
		return get(world, true);
	}

	public static WorldData get(Level world, boolean load)
	{
		if (world == null)
		{
			throw new IllegalArgumentException("world is null");
		}

		ConcurrentMap<ResourceLocation, WorldData> index = getIndex(!world.isClientSide);
		WorldData ret = index.get(getKey(world));
		if (ret == null && load)
		{
			ret = new WorldData(world);
			WorldData prev = index.putIfAbsent(getKey(world), ret);
			if (prev != null)
			{
				ret = prev;
			}

			return ret;
		} else
		{
			return ret;
		}
	}

	public static void onWorldUnload(Level world)
	{
		getIndex(!world.isClientSide).remove(getKey(world));
	}

	private static ResourceLocation getKey(Level world)
	{
		return Util.getDimId(world);
	}

	private static ConcurrentMap<ResourceLocation, WorldData> getIndex(boolean simulating)
	{
		return simulating ? idxServer : idxClient;
	}
}
