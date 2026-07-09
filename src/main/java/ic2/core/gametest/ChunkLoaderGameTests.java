package ic2.core.gametest;

import ic2.core.block.machine.tileentity.TileEntityChunkLoader;
import ic2.core.event.WorldData;
import ic2.core.ref.Ic2Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ChunkLoaderGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final BlockPos LOADER_POS = new BlockPos(1, 1, 1);

	// chunk loader: consumes euPerChunk (default 1 EU/t) per loaded chunk and keeps the chunks force-loaded while powered
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void chunkLoaderForceLoadsWhilePoweredAndStopsWhenDrained(GameTestHelper helper)
	{
		TileEntityChunkLoader te = placeLoader(helper);
		long ownChunk = ChunkPos.asLong(helper.absolutePos(LOADER_POS));
		te.energy.addEnergy(50.0);

		helper.runAtTickTime(10, () ->
		{
			helper.assertTrue(te.getActive(), "a powered chunk loader should be active");
			helper.assertTrue(te.energy.getEnergy() < 50.0, "the chunk loader should consume 1 EU/t per chunk, has " + te.energy.getEnergy());
			WorldData worldData = WorldData.get(helper.getLevel());
			helper.assertTrue(worldData.loadedChunks.containsKey(ownChunk), "the loader's own chunk should be registered as force-loaded");
		});

		helper.runAtTickTime(120, () ->
		{
			helper.assertFalse(te.getActive(), "the chunk loader must shut down once its 50 EU are used up");
			WorldData worldData = WorldData.get(helper.getLevel());
			helper.assertFalse(worldData.loadedChunks.containsKey(ownChunk), "the chunk must be released when the loader shuts down");
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY)
	public static void chunkLoaderOnlyAcceptsChunksInRange(GameTestHelper helper)
	{
		TileEntityChunkLoader te = placeLoader(helper);
		ChunkPos mainChunk = new ChunkPos(helper.absolutePos(LOADER_POS));
		ChunkPos nearChunk = new ChunkPos(mainChunk.x + 4, mainChunk.z);
		ChunkPos farChunk = new ChunkPos(mainChunk.x + 5, mainChunk.z);

		helper.assertTrue(te.isChunkInRange(nearChunk), "a chunk 4 chunks away is within the 4 chunk radius");
		helper.assertFalse(te.isChunkInRange(farChunk), "a chunk 5 chunks away is outside the 4 chunk radius");

		te.addChunkToLoaded(nearChunk);
		te.addChunkToLoaded(farChunk);
		helper.assertValueEqual(te.getLoadedChunks().size(), 2, "loaded chunk entries (own + near)");
		helper.assertTrue(te.getLoadedChunks().contains(nearChunk.toLong()), "the in-range chunk should be accepted");
		helper.assertFalse(te.getLoadedChunks().contains(farChunk.toLong()), "the out-of-range chunk must be rejected");

		helper.succeed();
	}

	private static TileEntityChunkLoader placeLoader(GameTestHelper helper)
	{
		helper.setBlock(LOADER_POS, Ic2Blocks.CHUNK_LOADER);
		TileEntityChunkLoader te = getTe(helper, LOADER_POS, TileEntityChunkLoader.class);
		// placing via setBlock skips onPlaced, so register the loader's own chunk like item placement would
		te.getLoadedChunks().add(ChunkPos.asLong(helper.absolutePos(LOADER_POS)));
		return te;
	}

	private static <T extends BlockEntity> T getTe(GameTestHelper helper, BlockPos pos, Class<T> type)
	{
		BlockEntity be = helper.getBlockEntity(pos);
		if (!type.isInstance(be))
		{
			throw new IllegalStateException("expected " + type.getSimpleName() + " at " + pos + ", found " + be);
		}

		return type.cast(be);
	}
}
