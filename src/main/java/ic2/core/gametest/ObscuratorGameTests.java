package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.tool.ItemObscurator;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ObscuratorGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final double MAX_CHARGE = 100000.0;
	private static final double APPLY_COST = 5000.0;

	private static final BlockPos WALL_POS = new BlockPos(1, 1, 1);

	private static ItemObscurator obscurator()
	{
		return (ItemObscurator) Ic2Items.OBSCURATOR;
	}

	/** Fakes the client-side scan by writing the reference block data the scan would have stored. */
	private static ItemStack makeScannedObscurator(double charge)
	{
		ItemStack stack = ElectricItemManager.getCharged(Ic2Items.OBSCURATOR, charge);
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		nbt.putString("refBlock", "minecraft:stone");
		nbt.putString("refVariant", "normal");
		nbt.putByte("refSide", (byte) Direction.NORTH.ordinal());
		nbt.putIntArray("refColorMuls", new int[] { -1 });
		return stack;
	}

	// applying a scanned reference to a CF wall turns it into an obscured wall for 5000 EU
	@GameTest(template = EMPTY)
	public static void obscuratorRetexturesWall(GameTestHelper helper)
	{
		helper.setBlock(WALL_POS, Ic2Blocks.WHITE_WALL);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		ItemStack stack = makeScannedObscurator(Double.POSITIVE_INFINITY);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		InteractionResult result = obscurator().onItemUseFirst(stack, Ic2GameTestUtil.useOn(helper, player, WALL_POS, Direction.NORTH));

		helper.assertValueEqual(result, InteractionResult.SUCCESS, "obscurator use result");
		helper.assertBlockPresent(Ic2Blocks.OBSCURED_WALL, WALL_POS);
		helper.assertValueEqual(ElectricItem.manager.getCharge(stack), MAX_CHARGE - APPLY_COST, "charge after retexturing");
		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void obscuratorWithoutChargeDoesNothing(GameTestHelper helper)
	{
		helper.setBlock(WALL_POS, Ic2Blocks.WHITE_WALL);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		ItemStack stack = makeScannedObscurator(100.0);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		InteractionResult result = obscurator().onItemUseFirst(stack, Ic2GameTestUtil.useOn(helper, player, WALL_POS, Direction.NORTH));

		helper.assertValueEqual(result, InteractionResult.PASS, "obscurator use result");
		helper.assertBlockPresent(Ic2Blocks.WHITE_WALL, WALL_POS);
		helper.succeed();
	}

	// a non-retexturable target leaves the block and the charge untouched
	@GameTest(template = EMPTY)
	public static void obscuratorOnPlainBlockDoesNothing(GameTestHelper helper)
	{
		helper.setBlock(WALL_POS, Blocks.DIRT);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		ItemStack stack = makeScannedObscurator(Double.POSITIVE_INFINITY);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		InteractionResult result = obscurator().onItemUseFirst(stack, Ic2GameTestUtil.useOn(helper, player, WALL_POS, Direction.NORTH));

		helper.assertValueEqual(result, InteractionResult.PASS, "obscurator use result");
		helper.assertBlockPresent(Blocks.DIRT, WALL_POS);
		helper.assertValueEqual(ElectricItem.manager.getCharge(stack), MAX_CHARGE, "charge must be untouched");
		helper.succeed();
	}

	// using the obscurator without a scanned reference clears any stale reference data
	@GameTest(template = EMPTY)
	public static void obscuratorWithoutReferenceClearsNbt(GameTestHelper helper)
	{
		helper.setBlock(WALL_POS, Ic2Blocks.WHITE_WALL);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		ItemStack stack = ElectricItemManager.getCharged(Ic2Items.OBSCURATOR, Double.POSITIVE_INFINITY);
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		nbt.putString("refBlock", "minecraft:stone"); // incomplete: no side / color multipliers
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		InteractionResult result = obscurator().onItemUseFirst(stack, Ic2GameTestUtil.useOn(helper, player, WALL_POS, Direction.NORTH));

		helper.assertValueEqual(result, InteractionResult.PASS, "obscurator use result");
		helper.assertFalse(nbt.contains("refBlock"), "stale reference data should be cleared");
		helper.assertBlockPresent(Ic2Blocks.WHITE_WALL, WALL_POS);
		helper.succeed();
	}
}
