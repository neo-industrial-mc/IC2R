package ic2.core.uu;

import ic2.core.IC2;
import ic2.core.Ic2Player;
import ic2.core.util.ConfigUtil;
import ic2.core.util.ItemComparableItemStack;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

public class DropScan
{
	private static final int BASE_OFFSET = 1_000_000;
	private final ServerLevel parentWorld;
	private final int range;
	private final Player player;
	private final Map<ItemComparableItemStack, MutableLong> drops = new java.util.HashMap<>();
	private final Map<BlockState, DropScan.DropDesc> typicalDrops = new IdentityHashMap<>();

	public DropScan(ServerLevel parentWorld, int range)
	{
		if (parentWorld == null)
		{
			throw new NullPointerException("null world");
		}

		if (range < 4)
		{
			throw new IllegalArgumentException("range has to be at least 4");
		}

		this.parentWorld = parentWorld;
		this.range = range;
		this.player = Ic2Player.get(parentWorld);
	}

	public void start(int area, int areaCount)
	{
		long lastPrint = 0L;

		for (int i = 0; i < areaCount; i++)
		{
			int x = BASE_OFFSET + IC2.random.nextInt(area) - area / 2;
			int z = BASE_OFFSET + IC2.random.nextInt(area) - area / 2;

			try
			{
				this.scanArea(x, z);
			} catch (Exception e)
			{
				IC2.log.warn(LogCategory.Uu, e, "Scan failed.");
			}

			if (i % 4 == 0 && lastPrint <= System.nanoTime() - 10_000_000_000L)
			{
				lastPrint = System.nanoTime();
				IC2.log.info(LogCategory.Uu, "World scan progress: %.1f%%.", 100.0F * i / areaCount);
			}
		}

		this.analyze();
	}

	public void cleanup()
	{
		this.drops.clear();
		this.typicalDrops.clear();
	}

	private void analyze()
	{
		ItemComparableItemStack cobblestone = new ItemComparableItemStack(new ItemStack(Blocks.COBBLESTONE), false);
		ItemComparableItemStack netherrack = new ItemComparableItemStack(new ItemStack(Blocks.NETHERRACK), false);
		double normalizeBy;
		if (!this.drops.containsKey(cobblestone))
		{
			if (!this.drops.containsKey(netherrack))
			{
				IC2.log.warn(LogCategory.Uu, "UU scan failed, there was no cobblestone or netherrack dropped");
				return;
			}

			normalizeBy = this.drops.get(netherrack).value;
		} else
		{
			normalizeBy = this.drops.get(cobblestone).value;
			if (this.drops.containsKey(netherrack))
			{
				normalizeBy = Math.max(normalizeBy, this.drops.get(netherrack).value);
			}
		}

		List<Entry<ItemComparableItemStack, MutableLong>> sorted = new ArrayList<>(this.drops.entrySet());
		this.drops.clear();
		sorted.sort(Comparator.comparingLong(a -> -a.getValue().value));
		IC2.log.info(LogCategory.Uu, "UU world scan complete. Copy the following entries into config/ic2-uu-scan-values.toml:");

		for (Entry<ItemComparableItemStack, MutableLong> entry : sorted)
		{
			ItemStack stack = entry.getKey().toStack();
			long count = entry.getValue().value;
			double value = normalizeBy / count;
			String line = ConfigUtil.fromStack(stack) + " " + value;
			IC2.log.info(LogCategory.Uu, "%d %s -> %s", count, stack.getHoverName().getString(), line);
		}
	}

	private void scanArea(int xStart, int zStart)
	{
		List<LevelChunk> innerChunks = new ArrayList<>(Util.square(this.range - 3));

		for (int x = xStart; x < xStart + this.range; x++)
		{
			for (int z = zStart; z < zStart + this.range; z++)
			{
				ChunkAccess chunk = this.parentWorld.getChunkSource().getChunk(x, z, ChunkStatus.FULL, true);
				if (x != xStart && x != xStart + this.range - 2 && z != zStart && z != zStart + this.range - 2 && chunk instanceof LevelChunk levelChunk)
				{
					innerChunks.add(levelChunk);
				}
			}
		}

		for (LevelChunk chunk : innerChunks)
		{
			this.scanChunk(chunk);
		}
	}

	private void scanChunk(LevelChunk chunk)
	{
		ChunkPos chunkPos = chunk.getPos();
		int xMax = (chunkPos.x + 1) * 16;
		int yMax = this.parentWorld.getMaxBuildHeight();
		int zMax = (chunkPos.z + 1) * 16;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int y = this.parentWorld.getMinBuildHeight(); y < yMax; y++)
		{
			for (int z = chunkPos.z * 16; z < zMax; z++)
			{
				for (int x = chunkPos.x * 16; x < xMax; x++)
				{
					pos.set(x, y, z);
					BlockState state = chunk.getBlockState(pos);
					if (!state.isAir())
					{
						for (ItemStack drop : this.getDrops(pos, state))
						{
							this.addDrop(drop);
						}
					}
				}
			}
		}
	}

	private List<ItemStack> getDrops(BlockPos pos, BlockState state)
	{
		DropScan.DropDesc typicalDrop = this.typicalDrops.get(state);
		if (typicalDrop != null && typicalDrop.dropCount.get() >= 1000)
		{
			return typicalDrop.drops;
		}

		List<ItemStack> drops = StackUtil.getDrops(this.parentWorld, pos, state, this.player, 0, false);
		if (typicalDrop == null)
		{
			typicalDrop = new DropScan.DropDesc(drops);
			this.typicalDrops.put(state, typicalDrop);
		}

		if (typicalDrop.dropCount.get() >= 0)
		{
			boolean equal = typicalDrop.drops.size() == drops.size();
			if (equal)
			{
				Iterator<ItemStack> it = drops.iterator();
				Iterator<ItemStack> it2 = typicalDrop.drops.iterator();

				while (it.hasNext())
				{
					ItemStack a = it.next();
					ItemStack b = it2.next();
					if (!ItemStack.isSameItemSameTags(a, b))
					{
						equal = false;
						break;
					}
				}
			}

			if (equal)
			{
				int prev = typicalDrop.dropCount.incrementAndGet();
				if (prev < 0)
				{
					typicalDrop.dropCount.set(Integer.MIN_VALUE);
				}
			} else
			{
				typicalDrop.dropCount.set(Integer.MIN_VALUE);
			}
		}

		return drops;
	}

	private void addDrop(ItemStack stack)
	{
		ItemComparableItemStack key = new ItemComparableItemStack(stack, false);
		MutableLong amount = this.drops.get(key);
		if (amount == null)
		{
			amount = new MutableLong();
			this.drops.put(key.copy(), amount);
		}

		amount.value += StackUtil.getSize(stack);
	}

	private static final class DropDesc
	{
		final List<ItemStack> drops;
		final AtomicInteger dropCount = new AtomicInteger();

		DropDesc(List<ItemStack> drops)
		{
			this.drops = drops;
		}
	}

	private static final class MutableLong
	{
		long value;
	}
}