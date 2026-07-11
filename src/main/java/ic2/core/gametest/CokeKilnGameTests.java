package ic2.core.gametest;

import ic2.core.block.comp.Fluids;
import ic2.core.block.steam.TileEntityCokeKiln;
import ic2.core.block.steam.TileEntityCokeKilnGrate;
import ic2.core.block.steam.TileEntityCokeKilnHatch;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class CokeKilnGameTests
{
	private static final String EMPTY_LARGE = "gametest/empty7x7x7";

	// controller position; it faces north, so the 3x3x3 kiln body extends one block south
	private static final BlockPos KILN_POS = new BlockPos(3, 2, 2);
	private static final BlockPos CENTER_POS = new BlockPos(3, 2, 3);

	// coke kiln, upstream 851f0084 regression: while a recipe is merely in progress, the periodic
	// canWork checks must only simulate filling the grate tank - with the non-simulated fill each
	// 20-tick evaluation inserted the full 500 mB of creosote long before any coal was consumed
	@GameTest(template = EMPTY_LARGE, timeoutTicks = 400)
	public static void cokeKilnDoesNotProduceCreosoteWhileOperating(GameTestHelper helper)
	{
		buildKiln(helper);
		TileEntityCokeKiln te = getTe(helper, KILN_POS, TileEntityCokeKiln.class);
		TileEntityCokeKilnHatch hatch = getTe(helper, CENTER_POS.above(), TileEntityCokeKilnHatch.class);
		Ic2FluidTank grateTank = getGrateTank(helper, CENTER_POS.below());
		hatch.setItem(0, new ItemStack(Items.COAL, 64));

		// the kiln evaluates canWork every 20 ticks, so by tick 60 it has to be running
		helper.runAtTickTime(60, () -> helper.assertTrue(te.getActive(), "coke kiln should be processing coal"));

		// ~15 more evaluations later (the recipe itself takes 1800 ticks) the tank must still be empty
		helper.runAtTickTime(380, () ->
		{
			helper.assertTrue(te.getActive(), "coke kiln should still be processing coal");
			helper.assertTrue(
				grateTank.isEmpty(),
				"no creosote may appear before the operation finishes, grate has " + grateTank.getFluidAmount() + " mB"
			);
			helper.assertValueEqual(hatch.getItem(0).getCount(), 64, "coal consumed before the operation finishes");
			helper.succeed();
		});
	}

	// coke kiln grate, upstream 851f0084/6713356d: the grate tank is manually drainable, a GUI click
	// with an empty cell extracts 1000 mB of creosote into the cell
	@GameTest(template = EMPTY_LARGE, timeoutTicks = 100)
	public static void cokeKilnGrateGuiClickExtractsCreosote(GameTestHelper helper)
	{
		BlockPos gratePos = new BlockPos(3, 1, 3);
		helper.setBlock(gratePos, Ic2Blocks.COKE_KILN_GRATE);
		TileEntityCokeKilnGrate te = getTe(helper, gratePos, TileEntityCokeKilnGrate.class);
		Ic2FluidTank tank = getGrateTank(helper, gratePos);
		int filled = tank.fillMb(Ic2FluidStack.create(Ic2Fluids.CREOSOTE.still(), 2000), false);
		helper.assertValueEqual(filled, 2000, "creosote accepted by the grate tank");

		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.containerMenu.setCarried(new ItemStack(Ic2Items.EMPTY_CELL));
		te.onNetworkEvent(player, 0);

		helper.assertValueEqual(tank.getFluidAmount(), 1000, "grate tank content after filling one cell");
		helper.assertValueEqual(countItems(player, Ic2Items.CREOSOTE_CELL), 1, "creosote cells given to the player");
		helper.succeed();
	}

	private static void buildKiln(GameTestHelper helper)
	{
		// facing north means the structure's center column is one block south of the controller
		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				for (int y = -1; y <= 1; y++)
				{
					BlockPos pos = CENTER_POS.offset(x, y, z);
					if (x == 0 && z == 0)
					{
						if (y == -1)
						{
							helper.setBlock(pos, Ic2Blocks.COKE_KILN_GRATE);
						}
						else if (y == 1)
						{
							helper.setBlock(pos, Ic2Blocks.COKE_KILN_HATCH);
						}
						// y == 0: the hollow center stays air
					}
					else if (pos.equals(KILN_POS))
					{
						helper.setBlock(pos, Ic2Blocks.COKE_KILN.defaultBlockState().setValue(Ic2TileEntityBlock.horizontalFacingProperty, Direction.NORTH));
					}
					else
					{
						helper.setBlock(pos, Ic2Blocks.REFRACTORY_BRICKS);
					}
				}
			}
		}
	}

	private static Ic2FluidTank getGrateTank(GameTestHelper helper, BlockPos pos)
	{
		TileEntityCokeKilnGrate grate = getTe(helper, pos, TileEntityCokeKilnGrate.class);
		return grate.getComponent(Fluids.class).getAllTanks().iterator().next();
	}

	private static int countItems(Player player, Item item)
	{
		int count = 0;
		for (ItemStack stack : player.getInventory().items)
		{
			if (stack.getItem() == item)
			{
				count += stack.getCount();
			}
		}

		return count;
	}

	private static <T extends BlockEntity> T getTe(GameTestHelper helper, BlockPos pos, Class<T> type)
	{
		BlockEntity be = helper.getBlockEntity(pos);
		if (!type.isInstance(be))
		{
			throw new IllegalStateException("expected " + type.getSimpleName() + " at " + pos + ", found " + be);
		}

		return type.cast(be);
	}
}
