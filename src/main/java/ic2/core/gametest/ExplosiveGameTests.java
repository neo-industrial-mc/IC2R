package ic2.core.gametest;

import ic2.core.block.machine.tileentity.TileEntityExplosive;
import ic2.core.entity.block.ITntEntity;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Entities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ExplosiveGameTests {
  private static final String EMPTY_LARGE = "gametest/empty7x7x7";

  /** ITNT block that gets caught in the blast, near the structure center. */
  private static final BlockPos TARGET_POS = new BlockPos(3, 4, 3);

  /** Where the triggering blast is centered, two blocks away from the target. */
  private static final BlockPos BLAST_POS = new BlockPos(3, 4, 1);

  /** An ITNT adjacent to the target, used as the first explosive in a chain. */
  private static final BlockPos CHAIN_SOURCE_POS = new BlockPos(3, 4, 2);

  // an ITNT caught in an IC2 explosion must chain-detonate (spawn a primed entity) instead of
  // dropping as an item
  @GameTest(template = EMPTY_LARGE, batch = "ic2Explosive")
  public static void itntCaughtInIc2ExplosionChainDetonates(GameTestHelper helper) {
    helper.setBlock(TARGET_POS, Ic2Blocks.ITNT);

    // a real primed ITNT entity, but with the blast turned down so it stays inside the structure
    Vec3 spawn = Vec3.atCenterOf(helper.absolutePos(BLAST_POS));
    ITntEntity primer = new ITntEntity(helper.getLevel(), spawn.x, spawn.y, spawn.z);
    primer.explosivePower = 3.0F;
    primer.dropRate = 0.0F;
    primer.setFuse(1);
    helper.getLevel().addFreshEntity(primer);

    // the primer blows up on tick 1-2, the chained ITNT re-fuses for at least 7 ticks
    helper.runAtTickTime(5, () -> assertChainedAndDefuse(helper));
  }

  // the same must happen for vanilla explosions, which go through Block.onBlockExploded
  @GameTest(template = EMPTY_LARGE, batch = "ic2Explosive")
  public static void itntCaughtInVanillaExplosionChainDetonates(GameTestHelper helper) {
    helper.setBlock(TARGET_POS, Ic2Blocks.ITNT);

    Vec3 center = Vec3.atCenterOf(helper.absolutePos(new BlockPos(3, 4, 2)));
    helper
        .getLevel()
        .explode(null, center.x, center.y, center.z, 2.0F, Level.ExplosionInteraction.TNT);

    helper.runAfterDelay(2, () -> assertChainedAndDefuse(helper));
  }

  // removing the neighboring ITNT through its chain-detonation path must not duplicate its item
  @GameTest(template = EMPTY_LARGE, batch = "ic2Explosive")
  public static void adjacentItntChainDetonationDoesNotDropItems(GameTestHelper helper) {
    helper.setBlock(CHAIN_SOURCE_POS, Ic2Blocks.ITNT);
    helper.setBlock(TARGET_POS, Ic2Blocks.ITNT);

    TileEntityExplosive source = (TileEntityExplosive) helper.getBlockEntity(CHAIN_SOURCE_POS);
    source.onRedstoneChange(15);

    List<ITntEntity> primed = helper.getEntities(Ic2Entities.ITNT, CHAIN_SOURCE_POS, 2.0);
    helper.assertTrue(
        primed.size() == 1,
        "the source ITNT should turn into exactly one primed ITNT entity, found " + primed.size());
    primed.get(0).setFuse(1);

    // The neighboring ITNT's shortened fuse is at most 21 ticks; both blasts have settled by 40.
    helper.runAtTickTime(
        40,
        () -> {
          helper.assertBlockPresent(Blocks.AIR, CHAIN_SOURCE_POS);
          helper.assertBlockPresent(Blocks.AIR, TARGET_POS);
          helper.assertItemEntityNotPresent(Ic2Blocks.ITNT.asItem(), TARGET_POS, 6.0);
          helper.succeed();
        });
  }

  /**
   * Asserts the target ITNT turned into a short-fused primed entity without dropping, then defuses
   * it before it can blow up the test arena.
   */
  private static void assertChainedAndDefuse(GameTestHelper helper) {
    helper.assertBlockPresent(Blocks.AIR, TARGET_POS);
    helper.assertItemEntityNotPresent(Ic2Blocks.ITNT.asItem(), TARGET_POS, 5.0);

    List<ITntEntity> primed = helper.getEntities(Ic2Entities.ITNT, TARGET_POS, 3.0);
    helper.assertTrue(
        primed.size() == 1,
        "the exploded ITNT should chain into exactly one primed ITNT entity, found "
            + primed.size());

    ITntEntity chained = primed.get(0);
    helper.assertTrue(
        chained.getFuse() < 30,
        "the chained ITNT should use a shortened fuse, has " + chained.getFuse());

    chained.discard();
    helper.succeed();
  }
}
