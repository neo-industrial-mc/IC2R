package ic2.core.gametest;

import ic2.core.block.storage.box.TileEntityWoodenStorageBox;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class StorageBoxGameTests {
  private static final String EMPTY = "gametest/empty3x3x3";
  private static final BlockPos BOX_POS = new BlockPos(1, 1, 1);

  @GameTest(template = EMPTY)
  public static void woodenStorageBoxPersistsEnchantedItems(GameTestHelper helper) {
    helper.setBlock(BOX_POS, Ic2Blocks.WOODEN_STORAGE_BOX);
    TileEntityWoodenStorageBox box = getStorageBox(helper);

    HolderLookup.RegistryLookup<Enchantment> enchantments =
        helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    Holder<Enchantment> sharpness = enchantments.getOrThrow(Enchantments.SHARPNESS);
    ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
    sword.enchant(sharpness, 3);
    box.setItem(0, sword);

    CompoundTag nbt = box.saveWithFullMetadata(helper.getLevel().registryAccess());
    helper.setBlock(BOX_POS, Blocks.AIR);
    helper.setBlock(BOX_POS, Ic2Blocks.WOODEN_STORAGE_BOX);
    TileEntityWoodenStorageBox restored = getStorageBox(helper);
    restored.loadWithComponents(nbt, helper.getLevel().registryAccess());

    ItemStack restoredSword = restored.getItem(0);
    helper.assertTrue(
        restoredSword.is(Items.DIAMOND_SWORD), "restored item should be a diamond sword");
    helper.assertValueEqual(
        EnchantmentHelper.getItemEnchantmentLevel(sharpness, restoredSword),
        3,
        "sharpness level after save and load");
    helper.succeed();
  }

  private static TileEntityWoodenStorageBox getStorageBox(GameTestHelper helper) {
    BlockEntity blockEntity = helper.getBlockEntity(BOX_POS);
    if (!(blockEntity instanceof TileEntityWoodenStorageBox storageBox)) {
      throw new IllegalStateException(
          "expected wooden storage box at " + BOX_POS + ", found " + blockEntity);
    }

    return storageBox;
  }
}
