package me.halfcooler.ic2r.core.gametest;

import me.halfcooler.ic2r.core.block.storage.box.TileEntityWoodenStorageBox;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r")
@PrefixGameTestTemplate(false)
public final class StorageBoxGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";
	private static final BlockPos BOX_POS = new BlockPos(1, 1, 1);

	private StorageBoxGameTests()
	{
	}

	@GameTest(template = EMPTY)
	public static void storageBoxDropPreservesEnchantedItem(GameTestHelper helper)
	{
		helper.setBlock(BOX_POS, Ic2rBlocks.WOODEN_STORAGE_BOX.get());
		TileEntityWoodenStorageBox box = (TileEntityWoodenStorageBox) helper.getBlockEntity(BOX_POS);

		HolderLookup.RegistryLookup<Enchantment> enchantments =
			helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		Holder<Enchantment> sharpness = enchantments.getOrThrow(Enchantments.SHARPNESS);
		ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
		sword.enchant(sharpness, 3);
		box.setItem(0, sword);

		ItemStack drop = box.adjustDrop(new ItemStack(Ic2rBlocks.WOODEN_STORAGE_BOX.get()), false);
		helper.setBlock(BOX_POS, Blocks.AIR);
		helper.setBlock(BOX_POS, Ic2rBlocks.WOODEN_STORAGE_BOX.get());
		TileEntityWoodenStorageBox restored =
			(TileEntityWoodenStorageBox) helper.getBlockEntity(BOX_POS);
		Player placer = helper.makeMockPlayer(GameType.SURVIVAL);
		restored.onPlaced(drop, placer, Direction.NORTH);

		ItemStack restoredSword = restored.getItem(0);
		helper.assertTrue(restoredSword.is(Items.DIAMOND_SWORD), "restored item should be a diamond sword");
		helper.assertValueEqual(
			EnchantmentHelper.getItemEnchantmentLevel(sharpness, restoredSword),
			3,
			"sharpness level after storage box drop round-trip");
		helper.succeed();
	}
}
