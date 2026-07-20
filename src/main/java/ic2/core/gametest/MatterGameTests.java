package ic2.core.gametest;

import ic2.core.block.comp.Energy;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.block.machine.tileentity.TileEntityPatternStorage;
import ic2.core.block.machine.tileentity.TileEntityReplicator;
import ic2.core.block.machine.tileentity.TileEntityScanner;
import ic2.core.block.misc.UUMatterBlock;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.item.ItemCrystalMemory;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class MatterGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);

  // matter fabricator: a full 1,000,000 EU buffer is converted into exactly 1 mB of UU matter
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void matterFabricatorProducesUuMatterAtFullCharge(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.MATTER_GENERATOR);
    TileEntityMatter te = getTe(helper, MACHINE_POS, TileEntityMatter.class);
    Energy energy = te.getComponent(Energy.class);
    energy.forceAddEnergy(energy.getCapacity());

    helper.succeedWhen(
        () -> {
          helper.assertValueEqual(
              te.fluidTank.getFluidAmount(), 1, "UU matter produced from one full buffer");
          Ic2GameTestAssertions.assertNear(
              helper, energy.getEnergy(), 0.0, "fabricator buffer after generating");
        });
  }

  // Parity treats any stored EU as active fabrication, even between incoming energy packets.
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void matterFabricatorIsActiveWhenHoldingCharge(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.MATTER_GENERATOR);
    TileEntityMatter te = getTe(helper, MACHINE_POS, TileEntityMatter.class);
    te.getComponent(Energy.class).forceAddEnergy(1000.0);

    helper.runAtTickTime(
        20,
        () -> {
          helper.assertTrue(te.getActive(), "fabricator holding charge should be active");
          helper.succeed();
        });
  }

  // scrap amplification: one scrap is worth 5000 amplifier points, each point turning 1 EU of input
  // into 5 bonus EU - so the amplified fabricator ends up exactly 25,000 EU ahead of an identical
  // plain one
  @GameTest(template = EMPTY, timeoutTicks = 200)
  public static void matterFabricatorScrapAmplifiesEnergyInput(GameTestHelper helper) {
    BlockPos scrapFabPos = new BlockPos(0, 1, 1);
    BlockPos plainFabPos = new BlockPos(2, 1, 1);
    helper.setBlock(scrapFabPos, Ic2Blocks.MATTER_GENERATOR);
    helper.setBlock(plainFabPos, Ic2Blocks.MATTER_GENERATOR);
    helper.setBlock(scrapFabPos.above(), Ic2Blocks.CREATIVE_GENERATOR);
    helper.setBlock(plainFabPos.above(), Ic2Blocks.CREATIVE_GENERATOR);
    TileEntityMatter scrapFab = getTe(helper, scrapFabPos, TileEntityMatter.class);
    TileEntityMatter plainFab = getTe(helper, plainFabPos, TileEntityMatter.class);
    scrapFab.amplifierSlot.put(0, new ItemStack(Ic2Items.SCRAP));

    helper.runAtTickTime(
        150,
        () -> {
          helper.assertTrue(
              scrapFab.amplifierSlot.isEmpty(), "fabricator should consume the scrap");
          helper.assertValueEqual(
              scrapFab.scrap, 0, "amplifier points left after the bonus is used up");
          double bonus =
              scrapFab.getComponent(Energy.class).getEnergy()
                  - plainFab.getComponent(Energy.class).getEnergy();
          Ic2GameTestAssertions.assertNear(
              helper, bonus, 25000.0, "extra energy gained from one scrap");
          helper.succeed();
        });
  }

  // redstone control: a redstone signal suspends the fabricator, so a full buffer produces nothing
  @GameTest(template = EMPTY, timeoutTicks = 150)
  public static void matterFabricatorHaltsOnRedstoneSignal(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS.above(), Blocks.REDSTONE_BLOCK);
    helper.setBlock(MACHINE_POS, Ic2Blocks.MATTER_GENERATOR);
    TileEntityMatter te = getTe(helper, MACHINE_POS, TileEntityMatter.class);
    Energy energy = te.getComponent(Energy.class);
    energy.forceAddEnergy(energy.getCapacity());

    helper.runAtTickTime(
        80,
        () -> {
          helper.assertValueEqual(
              te.fluidTank.getFluidAmount(), 0, "UU matter produced while redstone powered");
          Ic2GameTestAssertions.assertNear(
              helper,
              energy.getEnergy(),
              energy.getCapacity(),
              "fabricator buffer while redstone powered");
          helper.succeed();
        });
  }

  // container slot: an empty cell in the fabricator's container slot is filled from the internal
  // tank
  @GameTest(template = EMPTY, timeoutTicks = 200)
  public static void matterFabricatorFillsEmptyCellsFromTank(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.MATTER_GENERATOR);
    TileEntityMatter te = getTe(helper, MACHINE_POS, TileEntityMatter.class);
    te.getComponent(Energy.class).forceAddEnergy(1000.0);
    int filled =
        te.fluidTank.fillMb(Ic2FluidStack.create(Ic2Fluids.UU_MATTER.still(), 1000), false);
    helper.assertValueEqual(filled, 1000, "UU matter accepted by the fabricator tank");
    te.containerSlot.put(0, new ItemStack(Ic2Items.EMPTY_CELL));

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              te.outputSlot.get(0).getItem() == Ic2Items.UU_MATTER_CELL,
              "fabricator should fill the empty cell, has " + te.outputSlot.get(0));
          helper.assertTrue(
              te.fluidTank.isEmpty(), "fabricator tank should be drained into the cell");
        });
  }

  // scanner: scans an item (256 EU/t over 3300 ticks) and records the pattern into an adjacent
  // pattern storage.
  // progress is preset near the end for a fast-completing test.
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void scannerScansItemIntoPatternStorage(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.UU_SCANNER);
    helper.setBlock(MACHINE_POS.above(), Ic2Blocks.PATTERN_STORAGE);
    TileEntityScanner scanner = getTe(helper, MACHINE_POS, TileEntityScanner.class);
    TileEntityPatternStorage storage =
        getTe(helper, MACHINE_POS.above(), TileEntityPatternStorage.class);
    scanner.getComponent(Energy.class).forceAddEnergy(512000.0);
    scanner.inputSlot.put(0, new ItemStack(Items.COBBLESTONE));
    scanner.progress = 3290;

    helper.runAtTickTime(
        60,
        () -> {
          helper.assertTrue(
              scanner.getState() == TileEntityScanner.State.COMPLETED,
              "scan should complete, state is " + scanner.getState());
          helper.assertTrue(
              scanner.inputSlot.get(0).isEmpty(), "scanner should consume the scanned item");
          scanner.onNetworkEvent(null, 1); // save the pattern
          helper.assertValueEqual(
              storage.getPatterns().size(), 1, "patterns in the storage after saving");
          helper.assertTrue(
              storage.getPatterns().get(0).getItem() == Items.COBBLESTONE,
              "stored pattern should be cobblestone, is " + storage.getPatterns().get(0));
          helper.succeed();
        });
  }

  // scanner: with neither a pattern storage nor a crystal memory it refuses to scan
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void scannerRequiresPatternStorageOrCrystalMemory(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.UU_SCANNER);
    TileEntityScanner scanner = getTe(helper, MACHINE_POS, TileEntityScanner.class);
    scanner.getComponent(Energy.class).forceAddEnergy(512000.0);
    scanner.inputSlot.put(0, new ItemStack(Items.COBBLESTONE));

    helper.runAtTickTime(
        20,
        () -> {
          helper.assertTrue(
              scanner.getState() == TileEntityScanner.State.NO_STORAGE,
              "scanner without storage should report NO_STORAGE, state is " + scanner.getState());
          helper.assertValueEqual(scanner.progress, 0, "scan progress without a pattern storage");
          helper.assertValueEqual(
              scanner.inputSlot.get(0).getCount(), 1, "scanner input while unable to scan");
          helper.succeed();
        });
  }

  // scanner: a pattern that is already recorded in the adjacent storage is not scanned again
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void scannerRejectsAlreadyRecordedPattern(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.UU_SCANNER);
    helper.setBlock(MACHINE_POS.above(), Ic2Blocks.PATTERN_STORAGE);
    TileEntityScanner scanner = getTe(helper, MACHINE_POS, TileEntityScanner.class);
    TileEntityPatternStorage storage =
        getTe(helper, MACHINE_POS.above(), TileEntityPatternStorage.class);
    storage.addPattern(new ItemStack(Items.COBBLESTONE));
    scanner.getComponent(Energy.class).forceAddEnergy(512000.0);
    scanner.inputSlot.put(0, new ItemStack(Items.COBBLESTONE));

    helper.runAtTickTime(
        20,
        () -> {
          helper.assertTrue(
              scanner.getState() == TileEntityScanner.State.ALREADY_RECORDED,
              "scanner should report ALREADY_RECORDED, state is " + scanner.getState());
          helper.assertValueEqual(
              scanner.inputSlot.get(0).getCount(),
              1,
              "scanner input for an already recorded pattern");
          helper.succeed();
        });
  }

  // scanner: with a crystal memory in the disk slot the pattern is written to the crystal instead
  // of a storage
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void scannerSavesPatternToCrystalMemory(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.UU_SCANNER);
    TileEntityScanner scanner = getTe(helper, MACHINE_POS, TileEntityScanner.class);
    scanner.diskSlot.put(0, new ItemStack(Ic2Items.CRYSTAL_MEMORY));
    scanner.getComponent(Energy.class).forceAddEnergy(512000.0);
    scanner.inputSlot.put(0, new ItemStack(Items.COBBLESTONE));
    scanner.progress = 3290;

    helper.runAtTickTime(
        60,
        () -> {
          helper.assertTrue(
              scanner.getState() == TileEntityScanner.State.COMPLETED,
              "scan should complete, state is " + scanner.getState());
          scanner.onNetworkEvent(null, 1); // save the pattern
          ItemStack crystal = scanner.diskSlot.get(0);
          ItemStack recorded = ((ItemCrystalMemory) crystal.getItem()).readItemStack(crystal);
          helper.assertTrue(
              recorded.getItem() == Items.COBBLESTONE,
              "crystal memory should hold the cobblestone pattern, has " + recorded);
          helper.succeed();
        });
  }

  // pattern storage: each pattern is stored once, duplicates are rejected
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void patternStorageRejectsDuplicatePatterns(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.PATTERN_STORAGE);
    TileEntityPatternStorage storage = getTe(helper, MACHINE_POS, TileEntityPatternStorage.class);
    helper.assertTrue(
        storage.addPattern(new ItemStack(Items.COBBLESTONE)), "first pattern should be accepted");
    helper.assertFalse(
        storage.addPattern(new ItemStack(Items.COBBLESTONE)),
        "duplicate pattern should be rejected");
    helper.assertValueEqual(
        storage.getPatterns().size(), 1, "patterns stored after adding a duplicate");
    helper.succeed();
  }

  // pattern storage: a pattern can be copied onto a crystal memory and loaded into another storage
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void patternStorageCopiesPatternViaCrystalMemory(GameTestHelper helper) {
    BlockPos sourcePos = new BlockPos(0, 1, 1);
    BlockPos targetPos = new BlockPos(2, 1, 1);
    helper.setBlock(sourcePos, Ic2Blocks.PATTERN_STORAGE);
    helper.setBlock(targetPos, Ic2Blocks.PATTERN_STORAGE);
    TileEntityPatternStorage source = getTe(helper, sourcePos, TileEntityPatternStorage.class);
    TileEntityPatternStorage target = getTe(helper, targetPos, TileEntityPatternStorage.class);
    source.addPattern(new ItemStack(Items.COBBLESTONE));
    source.diskSlot.put(0, new ItemStack(Ic2Items.CRYSTAL_MEMORY));

    source.onNetworkEvent(null, 2); // write the selected pattern to the crystal
    ItemStack crystal = source.diskSlot.get(0);
    ItemStack recorded = ((ItemCrystalMemory) crystal.getItem()).readItemStack(crystal);
    helper.assertTrue(
        recorded.getItem() == Items.COBBLESTONE,
        "crystal memory should hold the copied pattern, has " + recorded);

    target.diskSlot.put(0, crystal);
    target.onNetworkEvent(null, 3); // load the pattern from the crystal
    helper.assertValueEqual(
        target.getPatterns().size(), 1, "patterns in the target storage after loading");
    helper.assertTrue(
        target.getPatterns().get(0).getItem() == Items.COBBLESTONE,
        "loaded pattern should be cobblestone, is " + target.getPatterns().get(0));
    helper.succeed();
  }

  // replicator: replicates the pattern selected in an adjacent pattern storage from UU matter and
  // EU.
  // cobblestone costs ~1e-5 buckets, well under the 1e-4 buckets/tick rate, so it finishes in a
  // single
  // 512 EU tick draining 1 mB from the tank.
  @GameTest(template = EMPTY, timeoutTicks = 200)
  public static void replicatorReplicatesStoredPattern(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.REPLICATOR);
    helper.setBlock(MACHINE_POS.above(), Ic2Blocks.PATTERN_STORAGE);
    TileEntityReplicator te = getTe(helper, MACHINE_POS, TileEntityReplicator.class);
    TileEntityPatternStorage storage =
        getTe(helper, MACHINE_POS.above(), TileEntityPatternStorage.class);
    storage.addPattern(new ItemStack(Items.COBBLESTONE));
    te.refreshInfo();
    te.getComponent(Energy.class).forceAddEnergy(2000000.0);
    int filled =
        te.fluidTank.fillMb(Ic2FluidStack.create(Ic2Fluids.UU_MATTER.still(), 1000), false);
    helper.assertValueEqual(filled, 1000, "UU matter accepted by the replicator tank");
    te.onNetworkEvent(null, 4); // single replication

    helper.succeedWhen(
        () -> {
          ItemStack output = te.outputSlot.get(0);
          helper.assertTrue(
              output.getItem() == Items.COBBLESTONE && output.getCount() == 1,
              "replicator should produce 1 cobblestone, has " + output);
          helper.assertValueEqual(
              te.fluidTank.getFluidAmount(), 999, "UU matter left after replicating cobblestone");
          helper.assertTrue(
              te.getMode() == TileEntityReplicator.Mode.STOPPED,
              "replicator should stop after a single replication, mode is " + te.getMode());
        });
  }

  // replicator: without UU matter in the tank nothing is replicated and no energy is spent
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void replicatorWaitsWithoutUuMatter(GameTestHelper helper) {
    helper.setBlock(MACHINE_POS, Ic2Blocks.REPLICATOR);
    helper.setBlock(MACHINE_POS.above(), Ic2Blocks.PATTERN_STORAGE);
    TileEntityReplicator te = getTe(helper, MACHINE_POS, TileEntityReplicator.class);
    TileEntityPatternStorage storage =
        getTe(helper, MACHINE_POS.above(), TileEntityPatternStorage.class);
    storage.addPattern(new ItemStack(Items.COBBLESTONE));
    te.refreshInfo();
    te.getComponent(Energy.class).forceAddEnergy(2000000.0);
    te.onNetworkEvent(null, 4); // single replication

    helper.runAtTickTime(
        60,
        () -> {
          helper.assertTrue(
              te.outputSlot.get(0).isEmpty(),
              "replicator without UU matter must not produce anything, has "
                  + te.outputSlot.get(0));
          Ic2GameTestAssertions.assertNear(
              helper,
              te.getComponent(Energy.class).getEnergy(),
              2000000.0,
              "replicator buffer while starved of UU matter");
          helper.succeed();
        });
  }

  // UU matter fluid: standing in it grants regeneration
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void uuMatterFluidGrantsRegeneration(GameTestHelper helper) {
    buildContainmentRing(helper);
    helper.setBlock(MACHINE_POS, uuMatterSource());
    Pig pig = helper.spawn(EntityType.PIG, MACHINE_POS);

    helper.succeedWhen(
        () ->
            helper.assertTrue(
                pig.hasEffect(MobEffects.REGENERATION), "pig in UU matter should regenerate"));
  }

  // UU matter fluid: adjacent source lava becomes obsidian, flowing lava becomes cobblestone
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void uuMatterFluidReactsWithLava(GameTestHelper helper) {
    BlockPos sourceLavaPos = new BlockPos(2, 1, 1);
    BlockPos flowingLavaPos = MACHINE_POS.above();
    helper.setBlock(new BlockPos(0, 1, 1), Blocks.STONE);
    helper.setBlock(new BlockPos(1, 1, 0), Blocks.STONE);
    helper.setBlock(new BlockPos(1, 1, 2), Blocks.STONE);
    helper.setBlock(MACHINE_POS, uuMatterSource());
    helper.setBlock(sourceLavaPos, Blocks.LAVA);
    helper.setBlock(flowingLavaPos, Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 2));

    helper.runAtTickTime(
        10,
        () -> {
          helper.assertBlockPresent(Blocks.OBSIDIAN, sourceLavaPos);
          helper.assertBlockPresent(Blocks.COBBLESTONE, flowingLavaPos);
          helper.assertTrue(
              helper.getBlockState(MACHINE_POS).getBlock() instanceof UUMatterBlock,
              "the UU matter source should survive the reaction");
          helper.succeed();
        });
  }

  // UU matter fluid: bottling it up only ever yields a plain water bottle
  @GameTest(template = EMPTY, timeoutTicks = 100)
  public static void uuMatterFluidFillsBottlesWithWater(GameTestHelper helper) {
    buildContainmentRing(helper);
    helper.setBlock(MACHINE_POS, uuMatterSource());
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
    player.setItemInHand(InteractionHand.MAIN_HAND, bottle);

    BlockPos absPos = helper.absolutePos(MACHINE_POS);
    BlockState state = helper.getLevel().getBlockState(absPos);
    UUMatterBlock block = (UUMatterBlock) state.getBlock();
    block.useItemOn(
        bottle,
        state,
        helper.getLevel(),
        absPos,
        player,
        InteractionHand.MAIN_HAND,
        new BlockHitResult(Vec3.atCenterOf(absPos), Direction.UP, absPos, false));

    ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
    helper.assertTrue(
        held.getItem() == Items.POTION, "bottling UU matter should yield a potion, has " + held);
    PotionContents contents = held.get(DataComponents.POTION_CONTENTS);
    helper.assertTrue(
        contents != null && contents.is(Potions.WATER),
        "the bottled potion should just be water, has " + contents);
    helper.succeed();
  }

  // stone ring around the center block so the UU matter source cannot flow anywhere
  private static void buildContainmentRing(GameTestHelper helper) {
    for (int x = 0; x < 3; x++) {
      for (int z = 0; z < 3; z++) {
        if (x != 1 || z != 1) {
          helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        }
      }
    }
  }

  private static BlockState uuMatterSource() {
    return Ic2Fluids.UU_MATTER.still().defaultFluidState().createLegacyBlock();
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
