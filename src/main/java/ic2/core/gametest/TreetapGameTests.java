package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.core.block.misc.RubberLogBlock;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class TreetapGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final BlockPos LOG_POS = new BlockPos(1, 1, 1);

	private static ServerPlayer makePlayer(GameTestHelper helper)
	{
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		return player;
	}

	private static void placeWetLog(GameTestHelper helper)
	{
		helper.setBlock(LOG_POS, Ic2Blocks.RUBBER_LOG.defaultBlockState().setValue(RubberLogBlock.stateProperty, RubberLogBlock.RubberWoodState.wet_north));
	}

	private static RubberLogBlock.RubberWoodState getLogState(GameTestHelper helper)
	{
		BlockState state = helper.getBlockState(LOG_POS);
		return state.getValue(RubberLogBlock.stateProperty);
	}

	// tapping the wet spot always yields resin, dries the spot and damages the treetap
	@GameTest(template = EMPTY)
	public static void treetapExtractsResinFromWetLog(GameTestHelper helper)
	{
		placeWetLog(helper);
		ServerPlayer player = makePlayer(helper);
		ItemStack stack = new ItemStack(Ic2Items.TREETAP);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		InteractionResult result = stack.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, LOG_POS, Direction.NORTH));

		helper.assertValueEqual(result, InteractionResult.SUCCESS, "treetap use result");
		helper.assertValueEqual(getLogState(helper), RubberLogBlock.RubberWoodState.dry_north, "log state after tapping");
		helper.assertValueEqual(player.getMainHandItem().getDamageValue(), 1, "treetap durability used");
		helper.succeedWhen(() -> helper.assertItemEntityPresent(Ic2Items.RESIN, LOG_POS, 2.0));
	}

	// the resin spot can only be tapped from the side it faces
	@GameTest(template = EMPTY)
	public static void treetapWrongSideDoesNothing(GameTestHelper helper)
	{
		placeWetLog(helper);
		ServerPlayer player = makePlayer(helper);
		ItemStack stack = new ItemStack(Ic2Items.TREETAP);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		InteractionResult result = stack.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, LOG_POS, Direction.EAST));

		helper.assertValueEqual(result, InteractionResult.FAIL, "treetap use result");
		helper.assertValueEqual(getLogState(helper), RubberLogBlock.RubberWoodState.wet_north, "log must stay wet");
		helper.assertValueEqual(player.getMainHandItem().getDamageValue(), 0, "treetap must not take damage");
		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void treetapPlainLogDoesNothing(GameTestHelper helper)
	{
		helper.setBlock(LOG_POS, Ic2Blocks.RUBBER_LOG.defaultBlockState().setValue(RubberLogBlock.stateProperty, RubberLogBlock.RubberWoodState.plain));
		ServerPlayer player = makePlayer(helper);
		ItemStack stack = new ItemStack(Ic2Items.TREETAP);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		InteractionResult result = stack.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, LOG_POS, Direction.NORTH));

		helper.assertValueEqual(result, InteractionResult.FAIL, "treetap use result");
		helper.assertValueEqual(getLogState(helper), RubberLogBlock.RubberWoodState.plain, "log must stay plain");
		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void electricTreetapExtractsResin(GameTestHelper helper)
	{
		placeWetLog(helper);
		ServerPlayer player = makePlayer(helper);
		ItemStack stack = ElectricItemManager.getCharged(Ic2Items.ELECTRIC_TREETAP, Double.POSITIVE_INFINITY);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		InteractionResult result = stack.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, LOG_POS, Direction.NORTH));

		helper.assertValueEqual(result, InteractionResult.SUCCESS, "electric treetap use result");
		helper.assertValueEqual(getLogState(helper), RubberLogBlock.RubberWoodState.dry_north, "log state after tapping");
		helper.assertValueEqual(ElectricItem.manager.getCharge(stack), 10000.0 - 50.0, "charge after tapping");
		helper.succeedWhen(() -> helper.assertItemEntityPresent(Ic2Items.RESIN, LOG_POS, 2.0));
	}

	@GameTest(template = EMPTY)
	public static void electricTreetapWithoutChargeDoesNothing(GameTestHelper helper)
	{
		placeWetLog(helper);
		ServerPlayer player = makePlayer(helper);
		ItemStack stack = new ItemStack(Ic2Items.ELECTRIC_TREETAP);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		InteractionResult result = stack.getItem().useOn(Ic2GameTestUtil.useOn(helper, player, LOG_POS, Direction.NORTH));

		helper.assertValueEqual(result, InteractionResult.PASS, "electric treetap use result");
		helper.assertValueEqual(getLogState(helper), RubberLogBlock.RubberWoodState.wet_north, "log must stay wet");
		helper.succeed();
	}
}
