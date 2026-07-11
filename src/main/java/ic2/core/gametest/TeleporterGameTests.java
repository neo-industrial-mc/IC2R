package ic2.core.gametest;

import ic2.core.block.machine.tileentity.TileEntityTeleporter;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFSU;
import ic2.core.init.IC2Config;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class TeleporterGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final BlockPos SOURCE_POS = new BlockPos(0, 1, 1);
  private static final BlockPos TARGET_POS = new BlockPos(2, 1, 1);

  // pig weight 100, distance 2: cost = 100 * (2 + 10)^0.7 * 5
  private static final int PIG_COST = (int) (100 * Math.pow(2 + 10, 0.7) * 5.0);

  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void teleporterSendsEntityToTargetAndDrainsEnergy(GameTestHelper helper) {
    TileEntityTeleporter source = setupTeleporterPair(helper);
    TileEntityElectricMFSU mfsu =
        getTe(helper, new BlockPos(0, 0, 1), TileEntityElectricMFSU.class);
    mfsu.energy.addEnergy(PIG_COST);
    helper.assertValueEqual(source.getAvailableEnergy(), PIG_COST, "energy seen by the teleporter");

    Pig pig = helper.spawn(EntityType.PIG, new BlockPos(0, 2, 1));
    BlockPos destination = helper.absolutePos(TARGET_POS);

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              pig.blockPosition().distSqr(destination.above()) <= 2.0,
              "pig should arrive above the target teleporter, is at "
                  + pig.blockPosition()
                  + " instead of "
                  + destination.above());
          helper.assertTrue(
              mfsu.energy.getEnergy() < 1.0,
              "teleport should consume the stored "
                  + PIG_COST
                  + " EU, left: "
                  + mfsu.energy.getEnergy());
        });
  }

  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void teleporterWithoutEnergyKeepsEntity(GameTestHelper helper) {
    setupTeleporterPair(helper);
    Pig pig = helper.spawn(EntityType.PIG, new BlockPos(0, 2, 1));
    pig.setNoAi(true);
    BlockPos start = helper.absolutePos(SOURCE_POS);

    helper.runAtTickTime(
        40,
        () -> {
          helper.assertTrue(
              pig.blockPosition().distSqr(start) <= 4.0,
              "pig must stay at the source teleporter without energy, is at "
                  + pig.blockPosition());
          helper.succeed();
        });
  }

  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void teleporterIgnoresWeightlessEntities(GameTestHelper helper) {
    TileEntityTeleporter source = setupTeleporterPair(helper);
    TileEntityElectricMFSU mfsu =
        getTe(helper, new BlockPos(0, 0, 1), TileEntityElectricMFSU.class);
    mfsu.energy.addEnergy(1000000.0);

    ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(0, 2, 1));
    helper.assertValueEqual(source.getWeightOf(stand), 0, "armor stand weight");
    BlockPos start = helper.absolutePos(SOURCE_POS);

    helper.runAtTickTime(
        40,
        () -> {
          helper.assertTrue(
              stand.blockPosition().distSqr(start) <= 4.0,
              "weightless armor stand must not be teleported, is at " + stand.blockPosition());
          Ic2GameTestAssertions.assertNear(
              helper,
              mfsu.energy.getEnergy(),
              1000000.0,
              "no energy may be consumed for a weightless entity");
          helper.succeed();
        });
  }

  @GameTest(template = EMPTY)
  public static void teleporterInventoryWeightConfigAffectsPlayerWeight(GameTestHelper helper) {
    helper.setBlock(SOURCE_POS, Ic2Blocks.TELEPORTER);
    TileEntityTeleporter te = getTe(helper, SOURCE_POS, TileEntityTeleporter.class);

    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    helper.assertValueEqual(
        te.getWeightOf(player), 1000, "weight of a player with an empty inventory");

    // a full stack adds 100, a half stack 50 (outside the hotbar so it is not the held item)
    player.getInventory().items.set(9, new ItemStack(Items.COBBLESTONE, 64));
    player.getInventory().items.set(10, new ItemStack(Items.COBBLESTONE, 32));
    helper.assertValueEqual(
        te.getWeightOf(player), 1150, "weight of a player with 1.5 stacks of cobblestone");

    boolean previous = IC2Config.balance.teleporterUseInventoryWeight.get();
    try {
      IC2Config.balance.teleporterUseInventoryWeight.set(false);
      helper.assertValueEqual(
          te.getWeightOf(player), 1000, "weight with teleporterUseInventoryWeight disabled");
    } finally {
      IC2Config.balance.teleporterUseInventoryWeight.set(previous);
    }

    helper.succeed();
  }

  private static TileEntityTeleporter setupTeleporterPair(GameTestHelper helper) {
    helper.setBlock(SOURCE_POS, Ic2Blocks.TELEPORTER);
    helper.setBlock(TARGET_POS, Ic2Blocks.TELEPORTER);
    helper.setBlock(new BlockPos(0, 0, 1), Ic2Blocks.MFSU);
    helper.setBlock(new BlockPos(1, 1, 1), Blocks.REDSTONE_BLOCK);
    TileEntityTeleporter source = getTe(helper, SOURCE_POS, TileEntityTeleporter.class);
    source.setTarget(helper.absolutePos(TARGET_POS));
    return source;
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
