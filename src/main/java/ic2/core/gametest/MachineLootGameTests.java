package ic2.core.gametest;

import ic2.core.block.wiring.tileentity.TileEntityElectricBlock;
import ic2.core.init.IC2Config;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.item.tool.ItemToolWrench;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.StackUtil;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class MachineLootGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);

	private static final double STORED_ENERGY = 1000.0;

	// breaking a macerator returns the basic machine casing, not the macerator itself
	@GameTest(template = EMPTY)
	public static void maceratorDropsMachineCasing(GameTestHelper helper)
	{
		assertBreakingDrops(helper, Ic2Blocks.MACERATOR, Ic2Blocks.MACHINE.asItem());
	}

	@GameTest(template = EMPTY)
	public static void electricFurnaceDropsItself(GameTestHelper helper)
	{
		assertBreakingDrops(helper, Ic2Blocks.ELECTRIC_FURNACE, Ic2Blocks.ELECTRIC_FURNACE.asItem());
	}

	// advanced machines return the advanced machine casing
	@GameTest(template = EMPTY)
	public static void inductionFurnaceDropsAdvancedCasing(GameTestHelper helper)
	{
		assertBreakingDrops(helper, Ic2Blocks.INDUCTION_FURNACE, Ic2Blocks.ADVANCED_MACHINE.asItem());
	}

	private static void assertBreakingDrops(GameTestHelper helper, Block machine, Item expectedDrop)
	{
		helper.setBlock(MACHINE_POS, machine);
		// GameTestHelper.destroyBlock suppresses drops, so break through the level instead
		helper.getLevel().destroyBlock(helper.absolutePos(MACHINE_POS), true);

		helper.succeedWhen(() -> helper.assertItemEntityPresent(expectedDrop, MACHINE_POS, 2.0));
	}

	// wrench removal returns the machine itself instead of the casing
	@GameTest(template = EMPTY)
	public static void wrenchedMaceratorDropsItselfNotCasing(GameTestHelper helper)
	{
		assertWrenchingDrops(helper, Ic2Blocks.MACERATOR, Ic2Blocks.MACERATOR.asItem(), Ic2Blocks.MACHINE.asItem());
	}

	@GameTest(template = EMPTY)
	public static void wrenchedInductionFurnaceDropsItselfNotCasing(GameTestHelper helper)
	{
		assertWrenchingDrops(helper, Ic2Blocks.INDUCTION_FURNACE, Ic2Blocks.INDUCTION_FURNACE.asItem(), Ic2Blocks.ADVANCED_MACHINE.asItem());
	}

	private static void assertWrenchingDrops(GameTestHelper helper, Block machine, Item expectedDrop, Item forbiddenDrop)
	{
		helper.setBlock(MACHINE_POS, machine);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();

		BlockPos absolutePos = helper.absolutePos(MACHINE_POS);
		// clicking the side the machine already faces skips the rotation step and removes it
		Direction facing = ((Ic2TileEntityBlock) machine).getFacing(helper.getLevel(), absolutePos);
		ItemToolWrench.WrenchResult result = ItemToolWrench.wrenchBlock(helper.getLevel(), absolutePos, facing, player, true);

		helper.assertValueEqual(result, ItemToolWrench.WrenchResult.Removed, "wrench result");
		helper.succeedWhen(() ->
		{
			helper.assertBlockPresent(Blocks.AIR, MACHINE_POS);
			helper.assertItemEntityPresent(expectedDrop, MACHINE_POS, 2.0);
			helper.assertItemEntityNotPresent(forbiddenDrop, MACHINE_POS, 2.0);
		});
	}

	@GameTest(template = EMPTY)
	public static void pickedUpWrenchedBatboxPreservesEnergy(GameTestHelper helper)
	{
		assertPickedUpWrenchDropPreservesEnergy(helper, Ic2Blocks.BATBOX, TileEntityElectricBlock.class);
	}

	@GameTest(template = EMPTY)
	public static void pickedUpWrenchedCesuPreservesEnergy(GameTestHelper helper)
	{
		assertPickedUpWrenchDropPreservesEnergy(helper, Ic2Blocks.CESU, TileEntityElectricBlock.class);
	}

	private static <T extends TileEntityElectricBlock> void assertPickedUpWrenchDropPreservesEnergy(GameTestHelper helper, Block machine, Class<T> type)
	{
		helper.setBlock(MACHINE_POS, machine);
		T original = getTe(helper, MACHINE_POS, type);
		original.energy.addEnergy(STORED_ENERGY);

		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		BlockPos absolutePos = helper.absolutePos(MACHINE_POS);
		Direction facing = ((Ic2TileEntityBlock) machine).getFacing(helper.getLevel(), absolutePos);
		ItemToolWrench.WrenchResult result = ItemToolWrench.wrenchBlock(helper.getLevel(), absolutePos, facing, player, true);

		helper.assertValueEqual(result, ItemToolWrench.WrenchResult.Removed, "wrench result");
		helper.assertBlockPresent(Blocks.AIR, MACHINE_POS);

		helper.runAtTickTime(2, () ->
		{
			ItemEntity drop = getDroppedItem(helper, machine.asItem());
			drop.setNoPickUpDelay();
			drop.playerTouch(player);

			helper.assertTrue(drop.isRemoved(), "wrenched drop should be picked up");
			ItemStack pickedStack = getInventoryStack(player, machine.asItem());
			helper.assertTrue(!StackUtil.isEmpty(pickedStack), "player should pick up the wrenched drop");

			double expectedEnergy = STORED_ENERGY * IC2Config.balance.energyRetainedInStorageBlockDrops.get();
			helper.assertValueEqual(StackUtil.getOrCreateNbtData(pickedStack).getDouble("energy"), expectedEnergy, "picked-up stack energy");

			InteractionResult placeResult = placePickedStack(helper, player, pickedStack);
			helper.assertTrue(placeResult.consumesAction(), "placing picked-up stack should succeed");
			T restored = getTe(helper, MACHINE_POS, type);
			Ic2GameTestAssertions.assertNear(helper, restored.energy.getEnergy(), expectedEnergy, "restored block energy");
			helper.succeed();
		});
	}

	private static ItemEntity getDroppedItem(GameTestHelper helper, Item item)
	{
		BlockPos absolutePos = helper.absolutePos(MACHINE_POS);
		List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(
			ItemEntity.class,
			new AABB(absolutePos).inflate(2.0),
			entity -> entity.getItem().is(item)
		);
		helper.assertTrue(!drops.isEmpty(), "expected dropped item entity for " + item);
		return drops.get(0);
	}

	private static ItemStack getInventoryStack(ServerPlayer player, Item item)
	{
		for (int i = 0; i < player.getInventory().getContainerSize(); i++)
		{
			ItemStack stack = player.getInventory().getItem(i);
			if (!StackUtil.isEmpty(stack) && stack.is(item))
			{
				return stack;
			}
		}

		return StackUtil.emptyStack;
	}

	private static InteractionResult placePickedStack(GameTestHelper helper, ServerPlayer player, ItemStack stack)
	{
		if (!(stack.getItem() instanceof BlockItem blockItem))
		{
			return InteractionResult.FAIL;
		}

		BlockPos placePos = helper.absolutePos(MACHINE_POS);
		player.setPos(placePos.getX() + 0.5, placePos.getY() + 3.0, placePos.getZ() + 0.5);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(placePos), Direction.UP, placePos, false);
		return blockItem.place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit));
	}

	private static <T extends BlockEntity> T getTe(GameTestHelper helper, BlockPos pos, Class<T> type)
	{
		BlockEntity be = helper.getBlockEntity(pos);
		if (!type.isInstance(be))
		{
			helper.fail("expected " + type.getSimpleName() + " at " + pos + ", found " + be);
		}

		return type.cast(be);
	}
}
