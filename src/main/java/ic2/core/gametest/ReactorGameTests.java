package ic2.core.gametest;

import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFSU;
import ic2.core.item.reactor.AbstractDamageableReactorComponent;
import ic2.core.item.reactor.ItemReactorHeatStorage;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ReactorGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";
	private static final BlockPos REACTOR_POS = new BlockPos(1, 1, 1);

	// the reactor only runs fuel while it has a redstone signal
	@GameTest(template = EMPTY)
	public static void reactorIdlesWithoutRedstoneSignal(GameTestHelper helper)
	{
		TileEntityNuclearReactorElectric reactor = placeReactor(helper);
		reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));

		// 60 ticks covers at least two reactor work ticks (one every 20)
		helper.runAtTickTime(60, () ->
		{
			Ic2GameTestAssertions.assertNear(helper, reactor.getReactorEnergyOutput(), 0.0, "output without redstone");
			helper.assertValueEqual(reactor.getHeat(), 0, "hull heat without redstone");
			helper.succeed();
		});
	}

	// an isolated single uranium rod pulses itself once: 1 output unit = 5 EU/t, 4 hull heat per second
	@GameTest(template = EMPTY)
	public static void singleUraniumRodOutputsOnePulse(GameTestHelper helper)
	{
		TileEntityNuclearReactorElectric reactor = placeReactor(helper);
		helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
		reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));

		helper.succeedWhen(() ->
		{
			Ic2GameTestAssertions.assertNear(helper, reactor.getReactorEnergyOutput(), 1.0, "single rod output");
			Ic2GameTestAssertions.assertNear(helper, reactor.getOfferedEnergy(), 5.0, "single rod EU/t");
			helper.assertTrue(reactor.getHeat() >= 4 && reactor.getHeat() % 4 == 0,
				"uncooled single rod should add 4 hull heat per work tick, hull has " + reactor.getHeat());
		});
	}

	// two adjacent rods pulse each other: 2 output units per rod instead of 1
	@GameTest(template = EMPTY)
	public static void adjacentRodsPulseEachOther(GameTestHelper helper)
	{
		TileEntityNuclearReactorElectric reactor = placeReactor(helper);
		helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
		reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
		reactor.reactorSlot.put(1, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));

		helper.succeedWhen(() -> Ic2GameTestAssertions.assertNear(helper, reactor.getReactorEnergyOutput(), 4.0, "output of two adjacent rods"));
	}

	// a neutron reflector bounces the rod's pulse back, doubling its output without extra fuel
	@GameTest(template = EMPTY)
	public static void neutronReflectorDoublesRodOutput(GameTestHelper helper)
	{
		TileEntityNuclearReactorElectric reactor = placeReactor(helper);
		helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
		reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
		reactor.reactorSlot.put(1, 0, new ItemStack(Ic2Items.NEUTRON_REFLECTOR));

		helper.succeedWhen(() -> Ic2GameTestAssertions.assertNear(helper, reactor.getReactorEnergyOutput(), 2.0, "reflected rod output"));
	}

	// an adjacent coolant cell soaks up the rod's 4 heat per work tick, keeping the hull at 0
	@GameTest(template = EMPTY)
	public static void coolantCellAbsorbsRodHeat(GameTestHelper helper)
	{
		TileEntityNuclearReactorElectric reactor = placeReactor(helper);
		helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
		reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));
		reactor.reactorSlot.put(1, 0, new ItemStack(Ic2Items.REACTOR_COOLANT_CELL));

		ItemReactorHeatStorage cellItem = (ItemReactorHeatStorage) Ic2Items.REACTOR_COOLANT_CELL;

		helper.succeedWhen(() ->
		{
			Ic2GameTestAssertions.assertNear(helper, reactor.getReactorEnergyOutput(), 1.0, "rod output next to coolant cell");
			int cellHeat = cellItem.getCurrentHeat(reactor.getItemAt(1, 0), reactor, 1, 0);
			helper.assertTrue(cellHeat >= 4 && cellHeat % 4 == 0, "coolant cell should hold the rod's heat, holds " + cellHeat);
			helper.assertValueEqual(reactor.getHeat(), 0, "hull heat with coolant cell");
		});
	}

	// a reactor heat vent pulls 5 heat per work tick out of the hull and vents it away
	@GameTest(template = EMPTY)
	public static void reactorHeatVentDrainsHullHeat(GameTestHelper helper)
	{
		TileEntityNuclearReactorElectric reactor = placeReactor(helper);
		reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.REACTOR_HEAT_VENT));
		reactor.setHeat(10);

		ItemReactorHeatStorage ventItem = (ItemReactorHeatStorage) Ic2Items.REACTOR_HEAT_VENT;

		helper.succeedWhen(() ->
		{
			helper.assertValueEqual(reactor.getHeat(), 0, "hull heat after venting");
			helper.assertValueEqual(ventItem.getCurrentHeat(reactor.getItemAt(0, 0), reactor, 0, 0), 0, "vent's own stored heat");
		});
	}

	// each reactor plating adds 1000 heat capacity and dampens heat effects by 5%
	@GameTest(template = EMPTY)
	public static void platingRaisesHeatCapacity(GameTestHelper helper)
	{
		TileEntityNuclearReactorElectric reactor = placeReactor(helper);
		reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.REACTOR_PLATING));

		helper.succeedWhen(() ->
		{
			helper.assertValueEqual(reactor.getMaxHeat(), 11000, "hull heat capacity with one plating");
			Ic2GameTestAssertions.assertNear(helper, reactor.getHeatEffectModifier(), 0.95, "heat effect modifier with one plating");
		});
	}

	// a rod on its last work tick turns into a depleted rod instead of vanishing
	@GameTest(template = EMPTY)
	public static void uraniumRodDepletesIntoDepletedRod(GameTestHelper helper)
	{
		TileEntityNuclearReactorElectric reactor = placeReactor(helper);
		helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);

		ItemStack rod = new ItemStack(Ic2Items.URANIUM_FUEL_ROD);
		// uranium rods last 20000 work ticks, fast-forward to the very last one
		((AbstractDamageableReactorComponent) Ic2Items.URANIUM_FUEL_ROD).setUse(rod, 19999);
		reactor.reactorSlot.put(0, 0, rod);

		helper.succeedWhen(() ->
		{
			ItemStack stack = reactor.getItemAt(0, 0);
			helper.assertTrue(stack != null && stack.getItem() == Ic2Items.DEPLETED_URANIUM_FUEL_ROD,
				"spent rod should become a depleted uranium fuel rod, slot has " + stack);
		});
	}

	// items that aren't reactor components get thrown out of the grid
	@GameTest(template = EMPTY)
	public static void nonComponentItemsAreEjected(GameTestHelper helper)
	{
		TileEntityNuclearReactorElectric reactor = placeReactor(helper);
		reactor.reactorSlot.put(0, 0, new ItemStack(Items.STICK));

		helper.succeedWhen(() ->
		{
			ItemStack stack = reactor.getItemAt(0, 0);
			helper.assertTrue(stack == null || stack.isEmpty(), "stick should have been ejected from the reactor, slot has " + stack);
			helper.assertItemEntityPresent(Items.STICK, REACTOR_POS, 2.0);
		});
	}

	// each attached reactor chamber adds a grid column and joins the reactor's energy net delegate
	@GameTest(template = EMPTY)
	public static void chambersExpandReactorGrid(GameTestHelper helper)
	{
		TileEntityNuclearReactorElectric reactor = placeReactor(helper);
		helper.setBlock(REACTOR_POS.west(), Ic2Blocks.REACTOR_CHAMBER);
		helper.setBlock(REACTOR_POS.east(), Ic2Blocks.REACTOR_CHAMBER);

		helper.succeedWhen(() ->
		{
			helper.assertValueEqual(reactor.getReactorSize(), 5, "grid columns with two chambers");
			helper.assertValueEqual(reactor.reactorSlot.size(), 30, "usable grid slots with two chambers");
			helper.assertValueEqual(reactor.getSubTiles().size(), 3, "energy net sub-tiles with two chambers");
		});
	}

	// the running reactor is an energy net source: an adjacent MFSU banks its 5 EU/t
	@GameTest(template = EMPTY)
	public static void reactorPowersAdjacentStorage(GameTestHelper helper)
	{
		TileEntityNuclearReactorElectric reactor = placeReactor(helper);
		helper.setBlock(REACTOR_POS.above(), Blocks.REDSTONE_BLOCK);
		helper.setBlock(REACTOR_POS.below(), Ic2Blocks.MFSU);
		TileEntityElectricMFSU mfsu = getTe(helper, REACTOR_POS.below(), TileEntityElectricMFSU.class);

		reactor.reactorSlot.put(0, 0, new ItemStack(Ic2Items.URANIUM_FUEL_ROD));

		helper.succeedWhen(() -> helper.assertTrue(mfsu.energy.getEnergy() >= 5.0,
			"MFSU should have banked reactor EU, has " + mfsu.energy.getEnergy()));
	}

	// heat beyond the hull's capacity melts the reactor down; containment plating shrinks the blast
	@GameTest(template = EMPTY, batch = "ic2ReactorMeltdown")
	public static void overheatedReactorMeltsDown(GameTestHelper helper)
	{
		TileEntityNuclearReactorElectric reactor = placeReactor(helper);

		// 18 containment platings raise capacity to 19000 but damp the explosion to well below one TNT
		for (int y = 0; y < 6; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				reactor.reactorSlot.put(x, y, new ItemStack(Ic2Items.CONTAINMENT_REACTOR_PLATING));
			}
		}

		reactor.setHeat(100000);

		helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.AIR, REACTOR_POS));
	}

	private static TileEntityNuclearReactorElectric placeReactor(GameTestHelper helper)
	{
		helper.setBlock(REACTOR_POS, Ic2Blocks.NUCLEAR_REACTOR);
		return getTe(helper, REACTOR_POS, TileEntityNuclearReactorElectric.class);
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
