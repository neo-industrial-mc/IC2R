package ic2.core.gametest;

import ic2.core.ref.Ic2Blocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class RubberLeavesGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final BlockPos LEAVES_POS = new BlockPos(1, 1, 1);

  @GameTest(template = EMPTY)
  public static void rubberLeavesRequireShearsOrSilkTouchToDropThemselves(GameTestHelper helper) {
    helper.setBlock(LEAVES_POS, Ic2Blocks.RUBBER_LEAVES);
    BlockPos absolutePos = helper.absolutePos(LEAVES_POS);
    BlockState state = helper.getBlockState(LEAVES_POS);

    List<ItemStack> handDrops = getDrops(helper, absolutePos, state, ItemStack.EMPTY);
    helper.assertFalse(
        containsRubberLeaves(handDrops),
        "rubber leaves must not drop themselves when broken by hand");

    List<ItemStack> axeDrops =
        getDrops(helper, absolutePos, state, new ItemStack(Items.DIAMOND_AXE));
    helper.assertFalse(
        containsRubberLeaves(axeDrops),
        "rubber leaves must not drop themselves when broken with an ordinary axe");

    List<ItemStack> shearsDrops = getDrops(helper, absolutePos, state, new ItemStack(Items.SHEARS));
    helper.assertTrue(
        containsRubberLeaves(shearsDrops), "rubber leaves should drop themselves when sheared");

    ItemStack silkTouchTool = new ItemStack(Items.DIAMOND_PICKAXE);
    HolderLookup.RegistryLookup<Enchantment> enchantments =
        helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    Holder<Enchantment> silkTouch = enchantments.getOrThrow(Enchantments.SILK_TOUCH);
    silkTouchTool.enchant(silkTouch, 1);

    List<ItemStack> silkTouchDrops = getDrops(helper, absolutePos, state, silkTouchTool);
    helper.assertTrue(
        containsRubberLeaves(silkTouchDrops),
        "rubber leaves should drop themselves with silk touch");
    helper.succeed();
  }

  private static List<ItemStack> getDrops(
      GameTestHelper helper, BlockPos pos, BlockState state, ItemStack tool) {
    return Block.getDrops(state, helper.getLevel(), pos, null, null, tool);
  }

  private static boolean containsRubberLeaves(List<ItemStack> drops) {
    return drops.stream().anyMatch(stack -> stack.is(Ic2Blocks.RUBBER_LEAVES.asItem()));
  }
}
