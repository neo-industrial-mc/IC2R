package ic2.core.gametest;

import ic2.core.block.comp.Energy;
import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class AdvMinerGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";

  private static final BlockPos MINER_POS = new BlockPos(1, 2, 1);
  private static final BlockPos ORE_POS = new BlockPos(0, 1, 1);
  private static final BlockPos CHEST_POS = new BlockPos(2, 2, 1);

  // whitelist filter card: only listed drops are mined, everything else is skipped by the scan
  @GameTest(template = EMPTY, timeoutTicks = 400)
  public static void advMinerMinesWhitelistedOreIntoChest(GameTestHelper helper) {
    helper.setBlock(ORE_POS, Blocks.GOLD_ORE);
    helper.setBlock(CHEST_POS, Blocks.CHEST);
    TileEntityAdvMiner te = setupAdvMiner(helper);
    te.cardSlot.put(0, makeFilterCard(helper, false, Items.RAW_GOLD));
    pointScanAt(helper, te, ORE_POS);

    helper.succeedWhen(
        () -> {
          helper.assertTrue(
              helper.getBlockState(ORE_POS).isAir(),
              "the whitelisted gold ore should be mined, scan cursor is at " + te.getMineTarget());
          ChestBlockEntity chest = getTe(helper, CHEST_POS, ChestBlockEntity.class);
          helper.assertValueEqual(
              countItems(chest, Items.RAW_GOLD), 1, "raw gold delivered to the adjacent chest");
        });
  }

  @GameTest(template = EMPTY, timeoutTicks = 200)
  public static void advMinerSkipsOreOutsideWhitelist(GameTestHelper helper) {
    helper.setBlock(ORE_POS, Blocks.GOLD_ORE);
    TileEntityAdvMiner te = setupAdvMiner(helper);
    te.cardSlot.put(0, makeFilterCard(helper, false, Items.DIAMOND));
    pointScanAt(helper, te, ORE_POS);

    helper.runAtTickTime(
        120,
        () -> {
          helper.assertTrue(
              helper.getBlockState(ORE_POS).is(Blocks.GOLD_ORE),
              "gold ore is not on the whitelist and must not be mined");
          helper.succeed();
        });
  }

  @GameTest(template = EMPTY)
  public static void advMinerFilterCardControlsMineability(GameTestHelper helper) {
    helper.setBlock(ORE_POS, Blocks.GOLD_ORE);
    TileEntityAdvMiner te = setupAdvMiner(helper);
    BlockPos orePos = helper.absolutePos(ORE_POS);
    BlockState oreState = helper.getLevel().getBlockState(orePos);

    helper.assertTrue(
        te.canMine(orePos, oreState.getBlock(), oreState),
        "without any filter every ore is mineable");

    te.cardSlot.put(0, makeFilterCard(helper, true, Items.RAW_GOLD));
    helper.assertFalse(
        te.canMine(orePos, oreState.getBlock(), oreState),
        "a blacklist card listing raw gold must block the gold ore");

    te.cardSlot.put(0, makeFilterCard(helper, true, Items.DIAMOND));
    helper.assertTrue(
        te.canMine(orePos, oreState.getBlock(), oreState),
        "a blacklist card listing something else must not block the gold ore");

    te.cardSlot.put(0, makeFilterCard(helper, false, Items.RAW_GOLD));
    helper.assertTrue(
        te.canMine(orePos, oreState.getBlock(), oreState),
        "a whitelist card listing raw gold must allow the gold ore");

    te.cardSlot.clear(0);
    te.filterSlot.put(0, new ItemStack(Items.RAW_GOLD));
    te.blacklist = true;
    helper.assertFalse(
        te.canMine(orePos, oreState.getBlock(), oreState),
        "the machine's own blacklist must block the gold ore");

    te.blacklist = false;
    helper.assertTrue(
        te.canMine(orePos, oreState.getBlock(), oreState),
        "the machine's own whitelist must allow the gold ore");

    helper.succeed();
  }

  private static TileEntityAdvMiner setupAdvMiner(GameTestHelper helper) {
    helper.setBlock(MINER_POS, Ic2Blocks.ADVANCED_MINER);
    TileEntityAdvMiner te = getTe(helper, MINER_POS, TileEntityAdvMiner.class);
    te.getComponent(Energy.class).addEnergy(100000.0);
    te.scannerSlot.put(
        0, ElectricItemManager.getCharged(Ic2Items.ADVANCED_SCANNER, Double.POSITIVE_INFINITY));
    return te;
  }

  // aims the scan cursor one block before the target so the next scanned position is the target
  // itself,
  // instead of waiting for the full 65x65 sweep to come around
  private static void pointScanAt(
      GameTestHelper helper, TileEntityAdvMiner te, BlockPos relTarget) {
    BlockPos target = helper.absolutePos(relTarget);
    CompoundTag tag = te.saveWithoutMetadata(helper.getLevel().registryAccess());
    tag.putInt("mineTargetX", target.getX() - 1);
    tag.putInt("mineTargetY", target.getY());
    tag.putInt("mineTargetZ", target.getZ());
    te.loadWithComponents(tag, helper.getLevel().registryAccess());
    helper.assertValueEqual(
        te.getMineTarget(), target.west(), "scan cursor after aiming at " + target);
  }

  private static ItemStack makeFilterCard(GameTestHelper helper, boolean blacklist, Item... items) {
    ItemStack card = new ItemStack(Ic2Items.MINING_FILTER_CARD);
    CompoundTag nbt = StackUtil.getOrCreateNbtData(card);
    nbt.putBoolean("blacklist", blacklist);
    ListTag list = new ListTag();

    for (Item item : items) {
      list.add(new ItemStack(item).save(helper.getLevel().registryAccess()));
    }

    nbt.put("Items", list);
    return card;
  }

  private static int countItems(Container container, Item item) {
    int total = 0;

    for (int i = 0; i < container.getContainerSize(); i++) {
      ItemStack stack = container.getItem(i);
      if (!stack.isEmpty() && stack.getItem() == item) {
        total += stack.getCount();
      }
    }

    return total;
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
