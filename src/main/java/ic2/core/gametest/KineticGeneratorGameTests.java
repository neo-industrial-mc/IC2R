package ic2.core.gametest;

import ic2.core.IC2;
import ic2.core.block.comp.Fluids;
import ic2.core.block.heatgenerator.tileentity.TileEntitySolidHeatGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityManualKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.wiring.tileentity.TileEntityElectricCESU;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class KineticGeneratorGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	// manual kinetic generator: every right click stores a fixed amount of KU, at most 10 clicks per tick count
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void manualKineticGeneratorStoresKuFromClicks(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.MANUAL_KINETIC_GENERATOR);
		TileEntityManualKineticGenerator manual = getTe(helper, new BlockPos(1, 1, 1), TileEntityManualKineticGenerator.class);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();

		clickManualGenerator(helper, player);
		int perClick = manual.drawKineticEnergy(Direction.UP, Integer.MAX_VALUE, true);
		helper.assertTrue(perClick > 0, "one click should store some KU, has " + perClick);

		// 11 more clicks in the same tick: only the first 10 clicks of a tick may count, and the buffer caps at 1000 KU
		for (int i = 0; i < 11; i++)
		{
			clickManualGenerator(helper, player);
		}

		int expected = Math.min(1000, 10 * perClick);
		helper.assertValueEqual(manual.drawKineticEnergy(Direction.UP, Integer.MAX_VALUE, true), expected, "buffer after 12 clicks in one tick");

		helper.assertValueEqual(manual.drawKineticEnergy(Direction.UP, Integer.MAX_VALUE, false), expected, "KU drained from the manual generator");
		helper.assertValueEqual(manual.drawKineticEnergy(Direction.UP, Integer.MAX_VALUE, true), 0, "buffer after draining");
		helper.succeed();
	}

	// steam turbine: steam is worth 2 KU/mB, the kinetic generator block turns that into EU at 4 KU per EU
	// and 10% of the processed steam condenses into distilled water inside the turbine
	@GameTest(template = EMPTY, timeoutTicks = 400)
	public static void steamKineticGeneratorSpinsTurbineOnSteam(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(0, 1, 1), Ic2Blocks.STEAM_KINETIC_GENERATOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.EAST));
		// the condenser absorbs the exhaust steam, otherwise the turbine vents and randomly explodes
		helper.setBlock(new BlockPos(0, 2, 1), Ic2Blocks.CONDENSER);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.KINETIC_GENERATOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.WEST));
		helper.setBlock(new BlockPos(2, 1, 1), Ic2Blocks.CESU);
		TileEntitySteamKineticGenerator steam = getTe(helper, new BlockPos(0, 1, 1), TileEntitySteamKineticGenerator.class);
		TileEntityElectricCESU cesu = getTe(helper, new BlockPos(2, 1, 1), TileEntityElectricCESU.class);
		steam.turbineSlot.put(0, new ItemStack(Ic2Items.STEAM_TURBINE));
		Ic2FluidTank steamTank = steam.getComponent(Fluids.class).getFluidTank("steamTank");

		int[] fed = {0};
		helper.succeedWhen(() ->
		{
			if (fed[0] < 2000)
			{
				fed[0] += steamTank.fillMb(Ic2FluidStack.create(Ic2Fluids.STEAM.still(), 100), false);
			}

			helper.assertTrue(steam.getDistilledWaterTankFill() >= 1, "turbine should condense distilled water from the steam, has " + steam.getDistilledWaterTankFill());
			helper.assertTrue(cesu.energy.getEnergy() >= 100.0, "CESU should receive EU converted from turbine KU, has " + cesu.energy.getEnergy());
		});
	}

	// steam turbine: without a turbine item the generator neither consumes steam nor outputs KU
	@GameTest(template = EMPTY, timeoutTicks = 150)
	public static void steamKineticGeneratorIdlesWithoutTurbine(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.STEAM_KINETIC_GENERATOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.EAST));
		TileEntitySteamKineticGenerator steam = getTe(helper, new BlockPos(1, 1, 1), TileEntitySteamKineticGenerator.class);
		Ic2FluidTank steamTank = steam.getComponent(Fluids.class).getFluidTank("steamTank");
		int filled = steamTank.fillMb(Ic2FluidStack.create(Ic2Fluids.STEAM.still(), 100), false);
		helper.assertValueEqual(filled, 100, "steam accepted by the turbine tank");

		helper.runAtTickTime(80, () ->
		{
			helper.assertValueEqual(steamTank.getFluidAmount(), 100, "steam left in the tank without a turbine");
			helper.assertValueEqual(steam.getKUoutput(), 0, "KU output without a turbine");
			helper.assertFalse(steam.getActive(), "turbineless generator must not run");
			helper.succeed();
		});
	}

	// stirling kinetic generator: draws HU from the adjacent heat source, buffers 3 KU per HU
	// and heats the water in its input tank into hot water at 1 HU/mB
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void stirlingKineticGeneratorMakesKuFromHeat(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(0, 1, 1), Ic2Blocks.SOLID_HEAT_GENERATOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.EAST));
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.STIRLING_KINETIC_GENERATOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.EAST));
		TileEntitySolidHeatGenerator heatSource = getTe(helper, new BlockPos(0, 1, 1), TileEntitySolidHeatGenerator.class);
		TileEntityStirlingKineticGenerator stirling = getTe(helper, new BlockPos(1, 1, 1), TileEntityStirlingKineticGenerator.class);
		heatSource.fuelSlot.put(0, new ItemStack(Items.COAL));
		int filled = stirling.inputTank.fillMb(Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000), false);
		helper.assertValueEqual(filled, 1000, "water accepted by the stirling input tank");

		helper.succeedWhen(() ->
		{
			helper.assertTrue(stirling.drawKineticEnergy(Direction.EAST, Integer.MAX_VALUE, true) > 0, "stirling kinetic generator should buffer KU on its facing side");
			helper.assertTrue(stirling.outputTank.getFluidAmount() > 0, "stirling kinetic generator should produce heated fluid");
			helper.assertTrue(stirling.outputTank.hasExactFluid(Ic2Fluids.HOT_WATER.still()), "heated output should be hot water");
		});
	}

	// water kinetic generator, river mode: the flow rate is fixed by the distance to the nearest non-water biome,
	// so unlike the tidal ocean mode it produces regardless of the time of day
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void waterKineticGeneratorSpinsInRiverMode(GameTestHelper helper)
	{
		ServerLevel level = helper.getLevel();
		BlockPos gen = helper.absolutePos(new BlockPos(1, 1, 1)).atY(150);
		level.setBlockAndUpdate(gen, Ic2Blocks.WATER_KINETIC_GENERATOR.defaultBlockState().setValue(Ic2TileEntityBlock.horizontalFacingProperty, Direction.EAST));
		TileEntityWaterKineticGenerator water = getTeAt(level, gen, TileEntityWaterKineticGenerator.class);
		// the test world has no river biome to detect, so preset the mode before the first update tick
		water.type = TileEntityWaterKineticGenerator.BiomeState.RIVER;
		water.rotorSlot.put(0, new ItemStack(Ic2Items.IRON_ROTOR));
		// in a river the iron rotor spins as a 5x5 rotor
		List<BlockPos> placed = buildWaterEnclosure(level, gen, 2);

		helper.succeedWhen(() ->
		{
			helper.assertTrue(water.getActive(), "river water mill should spin with a clear 5x5 water plane");
			helper.assertTrue(water.getKuOutput() > 0, "river water mill should output KU, has " + water.getKuOutput());
			helper.assertTrue(water.rotorSlot.get().getDamageValue() >= 1, "river duty should wear the rotor");
			removeEnclosure(level, gen, placed);
		});
	}

	// water kinetic generator, ocean mode: the output follows the tide, a sine of the day time peaking at 3000
	@GameTest(template = EMPTY, timeoutTicks = 300, batch = "ic2Tide")
	public static void waterKineticGeneratorFollowsOceanTide(GameTestHelper helper)
	{
		ServerLevel level = helper.getLevel();
		level.setDayTime(3000L);
		BlockPos gen = helper.absolutePos(new BlockPos(1, 1, 1)).atY(170);
		level.setBlockAndUpdate(gen, Ic2Blocks.WATER_KINETIC_GENERATOR.defaultBlockState().setValue(Ic2TileEntityBlock.horizontalFacingProperty, Direction.EAST));
		TileEntityWaterKineticGenerator water = getTeAt(level, gen, TileEntityWaterKineticGenerator.class);
		water.type = TileEntityWaterKineticGenerator.BiomeState.OCEAN;
		water.rotorSlot.put(0, new ItemStack(Ic2Items.IRON_ROTOR));
		// in an ocean the iron rotor spins at its full 7x7 diameter
		List<BlockPos> placed = buildWaterEnclosure(level, gen, 3);

		helper.succeedWhen(() ->
		{
			// keep the tide at its peak in case another batch shifted the clock
			level.setDayTime(3000L);
			helper.assertTrue(water.getActive(), "ocean water mill should spin with a clear 7x7 water plane");
			helper.assertTrue(water.getKuOutput() > 0, "ocean water mill should output KU at high tide, has " + water.getKuOutput());
			helper.assertTrue(water.rotorSlot.get().getDamageValue() >= 2, "ocean duty should wear the rotor 2 points per cycle");
			removeEnclosure(level, gen, placed);
		});
	}

	// wind kinetic generator: at the wind sim's peak altitude even the weakest possible wind (base strength 5)
	// exceeds the wooden rotor's minimum wind strength of 10, so the turbine always produces and wears its rotor
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void windKineticGeneratorProducesKuAtAltitude(GameTestHelper helper)
	{
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1)).atY(windPeakY(level));
		level.setBlockAndUpdate(pos, Ic2Blocks.WIND_KINETIC_GENERATOR.defaultBlockState());
		TileEntityWindKineticGenerator wind = getTeAt(level, pos, TileEntityWindKineticGenerator.class);
		wind.rotorSlot.put(0, new ItemStack(Ic2Items.WOODEN_ROTOR));

		helper.succeedWhen(() ->
		{
			helper.assertTrue(wind.getActive(), "turbine at y=" + pos.getY() + " should be active");
			helper.assertTrue(wind.isWindStrongEnough(), "wind at y=" + pos.getY() + " should exceed the wooden rotor minimum");
			helper.assertTrue(wind.getKuOutput() > 0, "turbine should output KU, has " + wind.getKuOutput());
			helper.assertTrue(wind.rotorSlot.get().getDamageValue() >= 1, "a producing turbine should wear its rotor");
			level.removeBlock(pos, false);
		});
	}

	// wind rotor lifetime: a rotor at the end of its durability is destroyed by the next wear cycle
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void windKineticGeneratorDestroysWornOutRotor(GameTestHelper helper)
	{
		ServerLevel level = helper.getLevel();
		// offset from the peak so simultaneously running wind tests cannot obstruct each other
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1)).atY(windPeakY(level) - 12);
		level.setBlockAndUpdate(pos, Ic2Blocks.WIND_KINETIC_GENERATOR.defaultBlockState());
		TileEntityWindKineticGenerator wind = getTeAt(level, pos, TileEntityWindKineticGenerator.class);
		ItemStack rotor = new ItemStack(Ic2Items.WOODEN_ROTOR);
		rotor.setDamageValue(rotor.getMaxDamage() - 1);
		wind.rotorSlot.put(0, rotor);

		helper.succeedWhen(() ->
		{
			helper.assertTrue(wind.rotorSlot.isEmpty(), "worn out rotor should break");
			helper.assertFalse(wind.getActive(), "turbine should stop once the rotor is gone");
			level.removeBlock(pos, false);
		});
	}

	// regression: a spinning rotor must keep its rotation speed and active state across an NBT
	// save/load round trip (world re-entry), instead of reporting a stopped rotor until the next
	// wind update happens to change the speed. The client-side half of the fix (the rotor render
	// angle advancing per client tick instead of per wall-clock and freezing while the game is
	// paused) is pure rendering and cannot be covered by a server-side gametest.
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void windKineticGeneratorSpinSurvivesSaveAndLoad(GameTestHelper helper)
	{
		ServerLevel level = helper.getLevel();
		// offset from the peak so simultaneously running wind tests cannot obstruct each other
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1)).atY(windPeakY(level) + 24);
		level.setBlockAndUpdate(pos, Ic2Blocks.WIND_KINETIC_GENERATOR.defaultBlockState());
		TileEntityWindKineticGenerator wind = getTeAt(level, pos, TileEntityWindKineticGenerator.class);
		wind.rotorSlot.put(0, new ItemStack(Ic2Items.WOODEN_ROTOR));

		// the turbine updates every 32 ticks, so it has spun up by tick 70: even the weakest wind
		// (base strength 5, an effective strength of ~12 here) exceeds the wooden rotor minimum of 10
		helper.runAtTickTime(70, () ->
		{
			helper.assertTrue(wind.getActive(), "turbine at y=" + pos.getY() + " should be active before the reload");
			float speed = wind.getRotorAnimationSpeed();
			helper.assertTrue(speed > 0.0F, "turbine should be spinning before the reload, speed " + speed);

			CompoundTag nbt = wind.saveWithFullMetadata(level.registryAccess());
			level.removeBlock(pos, false);
			level.setBlockAndUpdate(pos, Ic2Blocks.WIND_KINETIC_GENERATOR.defaultBlockState());
			TileEntityWindKineticGenerator restored = getTeAt(level, pos, TileEntityWindKineticGenerator.class);
			restored.loadWithComponents(nbt, level.registryAccess());

			helper.assertValueEqual(restored.getRotorAnimationSpeed(), speed, "rotation speed after the reload");
			helper.assertTrue(restored.getActive(), "restored turbine should still be active");
			helper.assertFalse(restored.rotorSlot.isEmpty(), "restored turbine should keep its rotor");
			level.removeBlock(pos, false);
			helper.succeed();
		});
	}

	private static void clickManualGenerator(GameTestHelper helper, ServerPlayer player)
	{
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		ServerLevel level = helper.getLevel();
		BlockState state = level.getBlockState(pos);
		((Ic2TileEntityBlock) state.getBlock()).useWithoutItem(state, level, pos, player, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
	}

	// a watertight box around the rotor plane: glass behind (around the generator), in front and rimming the
	// water plane itself, so the sources cannot flow anywhere while the test runs
	private static List<BlockPos> buildWaterEnclosure(ServerLevel level, BlockPos gen, int planeRadius)
	{
		List<BlockPos> placed = new ArrayList<>();
		int shell = planeRadius + 1;

		for (int dy = -shell; dy <= shell; dy++)
		{
			for (int dz = -shell; dz <= shell; dz++)
			{
				if (dy != 0 || dz != 0)
				{
					placed.add(setAndTrack(level, gen.offset(0, dy, dz), Blocks.GLASS.defaultBlockState()));
				}

				placed.add(setAndTrack(level, gen.offset(2, dy, dz), Blocks.GLASS.defaultBlockState()));
				boolean rim = Math.abs(dy) == shell || Math.abs(dz) == shell;
				placed.add(setAndTrack(level, gen.offset(1, dy, dz), (rim ? Blocks.GLASS : Blocks.WATER).defaultBlockState()));
			}
		}

		return placed;
	}

	private static BlockPos setAndTrack(ServerLevel level, BlockPos pos, BlockState state)
	{
		level.setBlockAndUpdate(pos, state);
		return pos;
	}

	private static void removeEnclosure(ServerLevel level, BlockPos gen, List<BlockPos> placed)
	{
		// freeze the water first so nothing flows while the shell comes down
		for (BlockPos pos : placed)
		{
			if (level.getBlockState(pos).getBlock() == Blocks.WATER)
			{
				level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
			}
		}

		for (BlockPos pos : placed)
		{
			level.removeBlock(pos, false);
		}

		level.removeBlock(gen, false);
	}

	// the altitude where the wind sim's height multiplier peaks at 1.0, mirroring WindSim's coefficient setup
	static int windPeakY(ServerLevel level)
	{
		int height = Math.max(1, IC2.getWorldMaxHeight(level));
		int seaLevel = Math.max(0, IC2.getSeaLevel(level));
		double baseHeight = seaLevel < height ? seaLevel : height * 0.5;
		return (int) Math.round(baseHeight + (height - baseHeight) / 2.0);
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

	private static <T extends BlockEntity> T getTeAt(ServerLevel level, BlockPos pos, Class<T> type)
	{
		BlockEntity be = level.getBlockEntity(pos);
		if (!type.isInstance(be))
		{
			throw new IllegalStateException("expected " + type.getSimpleName() + " at " + pos + ", found " + be);
		}

		return type.cast(be);
	}
}
