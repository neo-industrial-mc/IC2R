package ic2.core.gametest;

import ic2.core.block.comp.Energy;
import ic2.core.block.inherit.Ic2FenceBlock;
import ic2.core.block.machine.tileentity.TileEntityMagnetizer;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class MagnetizerGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final String TALL = "gametest/empty3x9x3";

  private static final BlockPos FENCE_POS = new BlockPos(1, 1, 1);
  private static final BlockPos MAGNETIZER_POS = new BlockPos(2, 1, 1);

  @GameTest(template = EMPTY)
  public static void magnetizerBoostsPlayerOnIronFence(GameTestHelper helper) {
    TileEntityMagnetizer te = setupMagnetizedFence(helper);
    te.getComponent(Energy.class).addEnergy(100.0);
    helper.assertTrue(te.canBoost(), "a charged magnetizer should be able to boost");

    Player player = boostPlayerOnFence(helper);

    // 0.075 upwards boost, then scaled by 1.03 while moving up
    Ic2GameTestAssertions.assertNear(
        helper,
        player.getDeltaMovement().y,
        0.075 * 1.03,
        "player velocity after one magnetizer boost");
    Ic2GameTestAssertions.assertNear(
        helper, te.getComponent(Energy.class).getEnergy(), 98.0, "boosting should consume 2 EU");

    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void magnetizerWithoutEnergyGivesNoBoost(GameTestHelper helper) {
    TileEntityMagnetizer te = setupMagnetizedFence(helper);
    te.getComponent(Energy.class).addEnergy(1.0);
    helper.assertFalse(te.canBoost(), "a magnetizer below 2 EU must not boost");

    Player player = boostPlayerOnFence(helper);

    Ic2GameTestAssertions.assertNear(
        helper, player.getDeltaMovement().y, 0.0, "player velocity without magnetizer energy");
    Ic2GameTestAssertions.assertNear(
        helper,
        te.getComponent(Energy.class).getEnergy(),
        1.0,
        "energy of a magnetizer that cannot boost");

    helper.succeed();
  }

  // the gametest framework encases the structure bounds in barrier blocks, so the tall
  // template is needed to keep the player and the scanned air column inside the structure
  @GameTest(template = TALL)
  public static void magnetizerBoostReachesPlayerAboveFence(GameTestHelper helper) {
    TileEntityMagnetizer te = setupMagnetizedFence(helper);
    te.getComponent(Energy.class).addEnergy(100.0);

    BlockPos fencePos = helper.absolutePos(FENCE_POS);
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    player.setPos(fencePos.getX() + 0.5, fencePos.getY() + 5.0, fencePos.getZ() + 0.5);
    player.setDeltaMovement(Vec3.ZERO);

    Ic2FenceBlock.onPlayerTick(player);

    Ic2GameTestAssertions.assertNear(
        helper,
        player.getDeltaMovement().y,
        0.075 * 1.03,
        "player velocity after a boost from above the fence");
    Ic2GameTestAssertions.assertNear(
        helper, te.getComponent(Energy.class).getEnergy(), 98.0, "boosting should consume 2 EU");

    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void magnetizerBoostAppliesOnlyOncePerTick(GameTestHelper helper) {
    TileEntityMagnetizer te = setupMagnetizedFence(helper);
    te.getComponent(Energy.class).addEnergy(100.0);

    // the collision handler and the player tick hook may both fire in the same tick
    Player player = boostPlayerOnFence(helper);
    Ic2FenceBlock.onPlayerTick(player);

    Ic2GameTestAssertions.assertNear(
        helper,
        player.getDeltaMovement().y,
        0.075 * 1.03,
        "player velocity must reflect a single boost per tick");
    Ic2GameTestAssertions.assertNear(
        helper,
        te.getComponent(Energy.class).getEnergy(),
        98.0,
        "a deduplicated boost must only consume 2 EU");

    helper.succeed();
  }

  @GameTest(template = EMPTY)
  public static void ironFenceConnectsToFencesAndSolidBlocks(GameTestHelper helper) {
    BlockPos solidPos = new BlockPos(2, 1, 1);
    BlockPos otherFencePos = new BlockPos(0, 1, 1);
    helper.setBlock(FENCE_POS, Ic2Blocks.IRON_FENCE);
    helper.setBlock(solidPos, Blocks.STONE);
    helper.setBlock(otherFencePos, Ic2Blocks.IRON_FENCE);

    helper.assertBlockProperty(
        FENCE_POS, Ic2FenceBlock.connectProperties.get(Direction.EAST), true);
    helper.assertBlockProperty(
        FENCE_POS, Ic2FenceBlock.connectProperties.get(Direction.WEST), true);
    helper.assertBlockProperty(
        otherFencePos, Ic2FenceBlock.connectProperties.get(Direction.EAST), true);
    helper.assertBlockProperty(
        FENCE_POS, Ic2FenceBlock.connectProperties.get(Direction.NORTH), false);
    helper.assertBlockProperty(
        FENCE_POS, Ic2FenceBlock.connectProperties.get(Direction.SOUTH), false);

    helper.setBlock(solidPos, Blocks.AIR);

    helper.assertBlockProperty(
        FENCE_POS, Ic2FenceBlock.connectProperties.get(Direction.EAST), false);
    helper.succeed();
  }

  private static TileEntityMagnetizer setupMagnetizedFence(GameTestHelper helper) {
    helper.setBlock(FENCE_POS, Ic2Blocks.IRON_FENCE);
    // the magnetizer only powers a fence it is facing
    helper.setBlock(
        MAGNETIZER_POS,
        Ic2Blocks.MAGNETIZER
            .defaultBlockState()
            .setValue(Ic2TileEntityBlock.horizontalFacingProperty, Direction.WEST));
    return getTe(helper, MAGNETIZER_POS, TileEntityMagnetizer.class);
  }

  private static Player boostPlayerOnFence(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos fencePos = helper.absolutePos(FENCE_POS);
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    player.setPos(fencePos.getX() + 0.5, fencePos.getY(), fencePos.getZ() + 0.5);
    player.setDeltaMovement(Vec3.ZERO);

    BlockState fenceState = level.getBlockState(fencePos);
    ((Ic2FenceBlock) fenceState.getBlock()).entityInside(fenceState, level, fencePos, player);
    return player;
  }

  private static <T extends BlockEntity> T getTe(
      GameTestHelper helper, BlockPos pos, Class<T> type) {
    BlockEntity be = helper.getBlockEntity(pos);
    if (!type.isInstance(be)) {
      throw new IllegalStateException(
          "expected " + type.getSimpleName() + " at " + pos + ", found " + be);
    }

    return type.cast(be);
  }
}
