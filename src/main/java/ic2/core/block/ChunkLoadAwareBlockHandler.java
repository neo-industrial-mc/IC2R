package ic2.core.block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

public final class ChunkLoadAwareBlockHandler {
  private static final Map<BlockState, ChunkLoadAwareBlock> stateMap = new IdentityHashMap<>();
  private static final Map<Level, Set<ChunkPos>> pendingLoads = new IdentityHashMap<>();
  private static boolean initialized;

  public static void init() {
    if (initialized) {
      return;
    }

    stateMap.clear();
    for (Block rawBlock : BuiltInRegistries.BLOCK) {
      if (rawBlock instanceof ChunkLoadAwareBlock block) {
        for (BlockState state : block.getLoadAwareState(rawBlock)) {
          stateMap.put(state, block);
        }
      }
    }
    initialized = true;
  }

  private static void ensureInit() {
    if (!initialized) {
      init();
    }
  }

  public static void onChunkLoad(LevelChunk chunk) {
    Level world = chunk.getLevel();
    if (world == null || world.isClientSide) {
      return;
    }

    ensureInit();
    pendingLoads.computeIfAbsent(world, key -> new HashSet<>()).add(chunk.getPos());
  }

  public static void onChunkUnload(LevelChunk chunk) {
    Level world = chunk.getLevel();
    if (world == null || world.isClientSide) {
      return;
    }

    ensureInit();
    Set<ChunkPos> pending = pendingLoads.get(world);
    if (pending != null) {
      pending.remove(chunk.getPos());
    }

    processChunk(chunk, false);
  }

  public static void onWorldTick(Level world) {
    if (world.isClientSide) {
      return;
    }

    Set<ChunkPos> pending = pendingLoads.get(world);
    if (pending == null || pending.isEmpty()) {
      return;
    }

    ensureInit();
    List<ChunkPos> toProcess = new ArrayList<>(pending);
    pending.clear();

    for (ChunkPos pos : toProcess) {
      if (world.getChunkSource().hasChunk(pos.x, pos.z)) {
        processChunk(world.getChunk(pos.x, pos.z), true);
      }
    }
  }

  public static void onWorldUnload(Level world) {
    pendingLoads.remove(world);
  }

  private static void processChunk(LevelChunk chunk, boolean isLoad) {
    if (stateMap.isEmpty()) {
      return;
    }

    Level world = chunk.getLevel();
    if (world == null || world.isClientSide) {
      return;
    }

    LevelChunkSection[] sections = chunk.getSections();

    for (int sectionIdx = 0; sectionIdx < sections.length; sectionIdx++) {
      LevelChunkSection section = sections[sectionIdx];
      if (!section.hasOnlyAir()) {
        PalettedContainer<BlockState> container = section.getStates();
        if (container.maybeHas(stateMap::containsKey)) {
          MutableBlockPos pos = new MutableBlockPos();
          pos.set(
              chunk.getPos().getMinBlockX(),
              chunk.getMinBuildHeight() + sectionIdx * 16,
              chunk.getPos().getMinBlockZ());
          BlockState lastState = null;
          ChunkLoadAwareBlock lastBlock = null;

          for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
              for (int x = 0; x < 16; x++) {
                BlockState state = (BlockState) container.get(x, y, z);
                if (state != lastState) {
                  lastState = state;
                  lastBlock = stateMap.get(state);
                }

                if (lastBlock != null) {
                  pos.set(pos.getX() & -16 | x, pos.getY() & -16 | y, pos.getZ() & -16 | z);
                  if (isLoad) {
                    lastBlock.onLoad(state, world, pos);
                  } else {
                    lastBlock.onUnload(state, world, pos);
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
