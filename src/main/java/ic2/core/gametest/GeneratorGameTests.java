package ic2.core.gametest;

import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Fluids;
import ic2.core.block.generator.tileentity.TileEntityGenerator;
import ic2.core.block.generator.tileentity.TileEntityGeoGenerator;
import ic2.core.block.generator.tileentity.TileEntityRTGenerator;
import ic2.core.block.generator.tileentity.TileEntitySemifluidGenerator;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.block.generator.tileentity.TileEntityWaterGenerator;
import ic2.core.block.generator.tileentity.TileEntityWindGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntitySolidHeatGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.wiring.tileentity.TileEntityElectricBatBox;
import ic2.core.block.wiring.tileentity.TileEntityElectricCESU;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class GeneratorGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	// generator: coal is 1600 furnace ticks -> 400 fuel ticks at 10 EU/t, exactly filling the 4000 EU buffer
	@GameTest(template = EMPTY, timeoutTicks = 500)
	public static void generatorBurnsCoalIntoEu(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.GENERATOR);
		TileEntityGenerator generator = getTe(helper, new BlockPos(1, 1, 1), TileEntityGenerator.class);
		generator.fuelSlot.put(0, new ItemStack(Items.COAL));

		helper.succeedWhen(() ->
		{
			helper.assertTrue(generator.fuelSlot.isEmpty(), "generator should consume the coal");
			Ic2GameTestAssertions.assertNear(helper, getEnergy(generator), 4000.0, "generator buffer after one coal");
		});
	}

	// geothermal: a lava bucket empties into the tank, leaves a bucket behind and fills the 2400 EU buffer at 20 EU/t
	@GameTest(template = EMPTY, timeoutTicks = 250)
	public static void geoGeneratorRunsOnBucketedLava(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.GEO_GENERATOR);
		TileEntityGeoGenerator geo = getTe(helper, new BlockPos(1, 1, 1), TileEntityGeoGenerator.class);
		geo.fluidSlot.put(0, new ItemStack(Items.LAVA_BUCKET));

		helper.succeedWhen(() ->
		{
			helper.assertTrue(geo.outputSlot.get(0).getItem() == Items.BUCKET, "geo generator should return an empty bucket, has " + geo.outputSlot.get(0));
			Ic2GameTestAssertions.assertNear(helper, getEnergy(geo), 2400.0, "geo generator buffer at capacity");
		});
	}

	// solar: with the sun up it trickles 1 EU/t into an adjacent batbox
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void solarGeneratorChargesBatboxInDaylight(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.SOLAR_GENERATOR);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.BATBOX);
		TileEntitySolarGenerator solar = getTe(helper, new BlockPos(1, 2, 1), TileEntitySolarGenerator.class);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 1, 1), TileEntityElectricBatBox.class);

		ServerLevel level = helper.getLevel();
		level.setDayTime(6000L);
		level.setWeatherParameters(100000, 0, false, false);
		solar.updateSunVisibility();
		helper.assertTrue(solar.isSunlight(), "solar generator should see the noon sun");

		helper.succeedWhen(() -> helper.assertTrue(batbox.energy.getEnergy() >= 1.0, "batbox should receive solar energy, has " + batbox.energy.getEnergy()));
	}

	// water mill manual mode: a water bucket is worth 500 fuel ticks at 1 EU/t
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void waterGeneratorRunsOnBucketedWater(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.WATER_GENERATOR);
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.BATBOX);
		TileEntityWaterGenerator water = getTe(helper, new BlockPos(1, 1, 1), TileEntityWaterGenerator.class);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 0, 1), TileEntityElectricBatBox.class);
		water.fuelSlot.put(0, new ItemStack(Items.WATER_BUCKET));

		helper.succeedWhen(() ->
		{
			helper.assertTrue(water.fuel > 0, "water generator should have gained fuel from the bucket");
			helper.assertTrue(batbox.energy.getEnergy() >= 10.0, "batbox should receive water mill energy, has " + batbox.energy.getEnergy());
		});
	}

	// water mill passive mode: a single enclosed water source block in the 3x3x3 neighborhood yields 1 EU per 100 ticks
	@GameTest(template = EMPTY, timeoutTicks = 300)
	public static void waterGeneratorGathersAmbientWater(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.WATER_GENERATOR);
		// stone ring so the water source above the mill cannot flow anywhere
		for (int x = 0; x < 3; x++)
		{
			for (int z = 0; z < 3; z++)
			{
				if (x != 1 || z != 1)
				{
					helper.setBlock(new BlockPos(x, 2, z), Blocks.STONE);
				}
			}
		}

		helper.setBlock(new BlockPos(1, 2, 1), Blocks.WATER);
		TileEntityWaterGenerator water = getTe(helper, new BlockPos(1, 1, 1), TileEntityWaterGenerator.class);
		water.updateWaterCount();
		helper.assertValueEqual(water.water, 1, "water blocks seen by the water mill");

		helper.succeedWhen(() -> helper.assertTrue(getEnergy(water) > 0.0, "water mill should generate from ambient water, has " + getEnergy(water)));
	}

	// wind mill: output depends on altitude, so run it high above the test platform where the wind sim is strong
	@GameTest(template = EMPTY, timeoutTicks = 400)
	public static void windGeneratorProducesAtAltitude(GameTestHelper helper)
	{
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1)).atY(180);
		level.setBlockAndUpdate(pos, Ic2Blocks.WIND_GENERATOR.defaultBlockState());
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof TileEntityWindGenerator wind))
		{
			throw new IllegalStateException("expected TileEntityWindGenerator at " + pos + ", found " + be);
		}

		helper.succeedWhen(() ->
		{
			helper.assertTrue(getEnergy(wind) > 0.0, "wind mill at y=" + pos.getY() + " should generate energy, has " + getEnergy(wind));
			level.removeBlock(pos, false);
		});
	}

	// RTG: output doubles per additional pellet, 2^(n-1) times the base rate
	@GameTest(template = EMPTY, timeoutTicks = 150)
	public static void rtGeneratorOutputScalesWithPellets(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(0, 1, 1), Ic2Blocks.RT_GENERATOR);
		helper.setBlock(new BlockPos(2, 1, 1), Ic2Blocks.RT_GENERATOR);
		TileEntityRTGenerator onePellet = getTe(helper, new BlockPos(0, 1, 1), TileEntityRTGenerator.class);
		TileEntityRTGenerator twoPellets = getTe(helper, new BlockPos(2, 1, 1), TileEntityRTGenerator.class);
		onePellet.fuelSlot.put(0, new ItemStack(Ic2Items.RTG_PELLET));
		twoPellets.fuelSlot.put(0, new ItemStack(Ic2Items.RTG_PELLET));
		twoPellets.fuelSlot.put(1, new ItemStack(Ic2Items.RTG_PELLET));

		helper.runAtTickTime(100, () ->
		{
			double one = getEnergy(onePellet);
			double two = getEnergy(twoPellets);
			helper.assertTrue(one > 0.0, "RTG with one pellet should generate energy");
			Ic2GameTestAssertions.assertNear(helper, two, 2.0 * one, "RTG output with two pellets vs one");
			helper.succeed();
		});
	}

	// semifluid generator: biogas burns 10 mB at a time for 16 EU/t, so 100 mB comes out to exactly 1600 EU
	@GameTest(template = EMPTY, timeoutTicks = 300)
	public static void semifluidGeneratorBurnsBiogas(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.SEMIFLUID_GENERATOR);
		TileEntitySemifluidGenerator semifluid = getTe(helper, new BlockPos(1, 1, 1), TileEntitySemifluidGenerator.class);
		Ic2FluidTank tank = semifluid.getComponent(Fluids.class).getAllTanks().iterator().next();
		int filled = tank.fillMb(Ic2FluidStack.create(Ic2Fluids.BIOGAS.still(), 100), false);
		helper.assertValueEqual(filled, 100, "biogas accepted by the semifluid generator tank");

		helper.succeedWhen(() ->
		{
			helper.assertTrue(tank.isEmpty(), "semifluid generator should drain the biogas");
			Ic2GameTestAssertions.assertNear(helper, getEnergy(semifluid), 1600.0, "semifluid generator buffer after 100 mB biogas");
		});
	}

	// stirling generator: pulls 20 HU/t from a coal-fired solid heat generator and emits 10 EU/t
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void stirlingGeneratorConvertsHeatToEu(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(0, 1, 1), Ic2Blocks.SOLID_HEAT_GENERATOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.EAST));
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.STIRLING_GENERATOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.WEST));
		helper.setBlock(new BlockPos(2, 1, 1), Ic2Blocks.CESU);
		TileEntitySolidHeatGenerator heatSource = getTe(helper, new BlockPos(0, 1, 1), TileEntitySolidHeatGenerator.class);
		TileEntityElectricCESU cesu = getTe(helper, new BlockPos(2, 1, 1), TileEntityElectricCESU.class);
		heatSource.fuelSlot.put(0, new ItemStack(Items.COAL));

		helper.succeedWhen(() -> helper.assertTrue(cesu.energy.getEnergy() >= 100.0, "CESU should receive stirling energy, has " + cesu.energy.getEnergy()));
	}

	// kinetic generator: an electric kinetic generator makes KU from EU, the kinetic generator turns it back at 4 KU per EU
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void kineticGeneratorConvertsKuToEu(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(0, 1, 1), Ic2Blocks.ELECTRIC_KINETIC_GENERATOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.EAST));
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.KINETIC_GENERATOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.WEST));
		helper.setBlock(new BlockPos(2, 1, 1), Ic2Blocks.CESU);
		TileEntityElectricKineticGenerator kuSource = getTe(helper, new BlockPos(0, 1, 1), TileEntityElectricKineticGenerator.class);
		TileEntityElectricCESU cesu = getTe(helper, new BlockPos(2, 1, 1), TileEntityElectricCESU.class);
		kuSource.slotMotor.put(0, new ItemStack(Ic2Items.ELECTRIC_MOTOR));
		kuSource.getComponent(Energy.class).addEnergy(10000.0);

		helper.succeedWhen(() -> helper.assertTrue(cesu.energy.getEnergy() >= 100.0, "CESU should receive kinetic generator energy, has " + cesu.energy.getEnergy()));
	}

	// creative generator: unconditional power, 10 packets of 32 EU per tick
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void creativeGeneratorPowersAdjacentSink(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.CREATIVE_GENERATOR);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.BATBOX);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 1, 1), TileEntityElectricBatBox.class);

		helper.succeedWhen(() -> helper.assertTrue(batbox.energy.getEnergy() >= 320.0, "batbox should charge from the creative generator, has " + batbox.energy.getEnergy()));
	}

	private static double getEnergy(Ic2TileEntity te)
	{
		return te.getComponent(Energy.class).getEnergy();
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
