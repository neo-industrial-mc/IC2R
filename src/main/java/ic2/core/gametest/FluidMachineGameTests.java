package ic2.core.gametest;

import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Fluids;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.block.machine.tileentity.TileEntityCondenser;
import ic2.core.block.machine.tileentity.TileEntityFluidBottler;
import ic2.core.block.machine.tileentity.TileEntityFluidDistributor;
import ic2.core.block.machine.tileentity.TileEntityFluidRegulator;
import ic2.core.block.machine.tileentity.TileEntitySolarDistiller;
import ic2.core.block.machine.tileentity.TileEntityTank;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class FluidMachineGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final BlockPos MACHINE_POS = new BlockPos(1, 1, 1);

	// fluid bottler, drain side: a water cell in the top slot is emptied into the internal tank
	@GameTest(template = EMPTY, timeoutTicks = 300)
	public static void fluidBottlerDrainsWaterCellIntoTank(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.FLUID_BOTTLER);
		TileEntityFluidBottler te = getTe(helper, MACHINE_POS, TileEntityFluidBottler.class);
		te.dischargeSlot.put(0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
		te.drainInputSlot.put(0, new ItemStack(Ic2Items.WATER_CELL));

		helper.succeedWhen(() ->
		{
			assertTankContains(helper, te.fluidTank, net.minecraft.world.level.material.Fluids.WATER, 1000, "fluid bottler tank");
			ItemStack output = te.outputSlot.get(0);
			helper.assertTrue(output.getItem() == Ic2Items.EMPTY_CELL && output.getCount() == 1, "fluid bottler should return 1 empty cell, has " + output);
			helper.assertTrue(te.drainInputSlot.get(0).isEmpty(), "fluid bottler water cell should be consumed");
		});
	}

	// fluid bottler, fill side: an empty cell in the bottom slot is filled from the internal tank
	@GameTest(template = EMPTY, timeoutTicks = 300)
	public static void fluidBottlerFillsEmptyCellFromTank(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.FLUID_BOTTLER);
		TileEntityFluidBottler te = getTe(helper, MACHINE_POS, TileEntityFluidBottler.class);
		te.dischargeSlot.put(0, ElectricItemManager.getCharged(Ic2Items.RE_BATTERY, Double.POSITIVE_INFINITY));
		int filled = te.fluidTank.fillMb(Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000), false);
		helper.assertValueEqual(filled, 1000, "water accepted by the fluid bottler tank");
		te.fillInputSlot.put(0, new ItemStack(Ic2Items.EMPTY_CELL));

		helper.succeedWhen(() ->
		{
			ItemStack output = te.outputSlot.get(0);
			helper.assertTrue(output.getItem() == Ic2Items.WATER_CELL && output.getCount() == 1, "fluid bottler should produce 1 water cell, has " + output);
			helper.assertTrue(te.fillInputSlot.get(0).isEmpty(), "fluid bottler empty cell should be consumed");
			helper.assertTrue(te.fluidTank.isEmpty(), "fluid bottler water should be consumed");
		});
	}

	// condenser, passive: with no vents it condenses 100 mB steam per tick for free,
	// producing 1 mB distilled water per 100 mB steam.
	@GameTest(template = EMPTY, timeoutTicks = 300)
	public static void condenserCondensesSteamIntoDistilledWater(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.CONDENSER);
		TileEntityCondenser te = getTe(helper, MACHINE_POS, TileEntityCondenser.class);
		int filled = te.getInputTank().fillMb(Ic2FluidStack.create(Ic2Fluids.STEAM.still(), 10000), false);
		helper.assertValueEqual(filled, 10000, "steam accepted by the condenser input tank");

		helper.succeedWhen(() ->
		{
			assertTankContains(helper, te.getOutputTank(), Ic2Fluids.DISTILLED_WATER.still(), 100, "condenser output tank");
			helper.assertValueEqual(te.getOutputTank().getFluidAmount(), 100, "distilled water from 10000 mB steam");
			helper.assertTrue(te.getInputTank().isEmpty(), "condenser steam should be consumed");
		});
	}

	// condenser, vented: 4 heat vents add 100 mB/t each for 2 EU/t each, so 10000 mB steam
	// drains at 500 mB/t over 20 ticks and costs exactly 160 EU.
	@GameTest(template = EMPTY, timeoutTicks = 150)
	public static void condenserVentsSpeedUpCondensationUsingEnergy(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.CONDENSER);
		TileEntityCondenser te = getTe(helper, MACHINE_POS, TileEntityCondenser.class);
		for (int i = 0; i < te.ventSlots.size(); i++)
		{
			te.ventSlots.put(i, new ItemStack(Ic2Items.HEAT_VENT));
		}

		Energy energy = te.getComponent(Energy.class);
		energy.addEnergy(1000.0);
		int filled = te.getInputTank().fillMb(Ic2FluidStack.create(Ic2Fluids.STEAM.still(), 10000), false);
		helper.assertValueEqual(filled, 10000, "steam accepted by the condenser input tank");

		helper.runAtTickTime(100, () ->
		{
			assertTankContains(helper, te.getOutputTank(), Ic2Fluids.DISTILLED_WATER.still(), 100, "condenser output tank");
			helper.assertTrue(te.getInputTank().isEmpty(), "condenser steam should be consumed");
			Ic2GameTestAssertions.assertNear(helper, energy.getEnergy(), 1000.0 - 160.0, "condenser energy after venting 10000 mB steam");
			helper.succeed();
		});
	}

	// solar distiller: under the noon sun it converts water into distilled water 1 mB at a time
	@GameTest(template = EMPTY, timeoutTicks = 400)
	public static void solarDistillerDistillsWaterInSunlight(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.SOLAR_DISTILLER);
		TileEntitySolarDistiller te = getTe(helper, MACHINE_POS, TileEntitySolarDistiller.class);

		ServerLevel level = helper.getLevel();
		level.setDayTime(6000L);
		level.setWeatherParameters(100000, 0, false, false);
		float skyLight = TileEntitySolarGenerator.getSkyLight(level, helper.absolutePos(MACHINE_POS).above());
		helper.assertTrue(skyLight > 0.5F, "solar distiller should see the noon sun, sky light is " + skyLight);

		int filled = te.inputTank.fillMb(Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000), false);
		helper.assertValueEqual(filled, 1000, "water accepted by the solar distiller input tank");

		helper.succeedWhen(() ->
		{
			assertTankContains(helper, te.outputTank, Ic2Fluids.DISTILLED_WATER.still(), 1, "solar distiller output tank");
			helper.assertValueEqual(
				te.inputTank.getFluidAmount() + te.outputTank.getFluidAmount(), 1000,
				"solar distiller water to distilled water conversion is 1:1"
			);
		});
	}

	// tank: 24000 mB capacity, excess fluid is rejected and partial drains leave the rest behind
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void tankStoresFluidUpToCapacity(GameTestHelper helper)
	{
		helper.setBlock(MACHINE_POS, Ic2Blocks.TANK);
		Ic2FluidTank tank = getTankBlockTank(helper, MACHINE_POS);
		int filled = tank.fillMb(Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 30000), false);
		helper.assertValueEqual(filled, 24000, "water accepted by an empty tank");
		int overflow = tank.fillMb(Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000), false);
		helper.assertValueEqual(overflow, 0, "water accepted by a full tank");

		Ic2FluidStack drained = tank.drainMb(4000, false);
		helper.assertTrue(
			drained != null && drained.getFluid() == net.minecraft.world.level.material.Fluids.WATER && drained.getAmountMb() == 4000,
			"tank should drain 4000 mB water, drained " + drained
		);

		helper.runAtTickTime(10, () ->
		{
			assertTankContains(helper, tank, net.minecraft.world.level.material.Fluids.WATER, 20000, "tank after partial drain");
			helper.assertValueEqual(tank.getFluidAmount(), 20000, "tank content after partial drain");
			helper.succeed();
		});
	}

	// fluid distributor, distribute mode (inactive): fluid is split evenly between all sides except the facing
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void fluidDistributorSplitsEvenlyAcrossSides(GameTestHelper helper)
	{
		BlockPos eastPos = MACHINE_POS.east();
		BlockPos westPos = MACHINE_POS.west();
		helper.setBlock(MACHINE_POS, Ic2Blocks.FLUID_DISTRIBUTOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.UP));
		helper.setBlock(eastPos, Ic2Blocks.TANK);
		helper.setBlock(westPos, Ic2Blocks.TANK);
		TileEntityFluidDistributor te = getTe(helper, MACHINE_POS, TileEntityFluidDistributor.class);
		int filled = te.fluidTank.fillMb(Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000), false);
		helper.assertValueEqual(filled, 1000, "water accepted by the fluid distributor");

		Ic2FluidTank eastTank = getTankBlockTank(helper, eastPos);
		Ic2FluidTank westTank = getTankBlockTank(helper, westPos);
		helper.succeedWhen(() ->
		{
			assertTankContains(helper, eastTank, net.minecraft.world.level.material.Fluids.WATER, 500, "tank east of the distributor");
			assertTankContains(helper, westTank, net.minecraft.world.level.material.Fluids.WATER, 500, "tank west of the distributor");
			helper.assertValueEqual(eastTank.getFluidAmount(), 500, "east tank share");
			helper.assertValueEqual(westTank.getFluidAmount(), 500, "west tank share");
			helper.assertTrue(te.fluidTank.isEmpty(), "fluid distributor should be drained");
		});
	}

	// fluid distributor, concentrate mode (active): all fluid goes to the facing side only
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void fluidDistributorConcentratesTowardFacing(GameTestHelper helper)
	{
		BlockPos eastPos = MACHINE_POS.east();
		BlockPos westPos = MACHINE_POS.west();
		helper.setBlock(MACHINE_POS, Ic2Blocks.FLUID_DISTRIBUTOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.EAST));
		helper.setBlock(eastPos, Ic2Blocks.TANK);
		helper.setBlock(westPos, Ic2Blocks.TANK);
		TileEntityFluidDistributor te = getTe(helper, MACHINE_POS, TileEntityFluidDistributor.class);
		te.setActive(true);
		int filled = te.fluidTank.fillMb(Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 1000), false);
		helper.assertValueEqual(filled, 1000, "water accepted by the fluid distributor");

		Ic2FluidTank eastTank = getTankBlockTank(helper, eastPos);
		Ic2FluidTank westTank = getTankBlockTank(helper, westPos);
		helper.succeedWhen(() ->
		{
			assertTankContains(helper, eastTank, net.minecraft.world.level.material.Fluids.WATER, 1000, "tank on the distributor's facing side");
			helper.assertTrue(westTank.isEmpty(), "tank opposite the facing side must stay empty, has " + westTank.getFluidAmount() + " mB");
			helper.assertTrue(te.fluidTank.isEmpty(), "fluid distributor should be drained");
		});
	}

	// fluid regulator: pushes the configured amount (100 mB) into the facing tank once per second,
	// using 10 EU per operation, so 500 mB takes 5 operations and 50 EU.
	@GameTest(template = EMPTY, timeoutTicks = 300)
	public static void fluidRegulatorPushesConfiguredAmountPerSecond(GameTestHelper helper)
	{
		BlockPos tankPos = MACHINE_POS.east();
		helper.setBlock(MACHINE_POS, Ic2Blocks.FLUID_REGULATOR.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.EAST));
		helper.setBlock(tankPos, Ic2Blocks.TANK);
		TileEntityFluidRegulator te = getTe(helper, MACHINE_POS, TileEntityFluidRegulator.class);
		Energy energy = te.getComponent(Energy.class);
		energy.addEnergy(1000.0);
		te.onNetworkEvent(null, 100);
		helper.assertValueEqual(te.getOutputMb(), 100, "fluid regulator configured output");
		int filled = te.getFluidTank().fillMb(Ic2FluidStack.create(net.minecraft.world.level.material.Fluids.WATER, 500), false);
		helper.assertValueEqual(filled, 500, "water accepted by the fluid regulator");

		Ic2FluidTank targetTank = getTankBlockTank(helper, tankPos);
		helper.succeedWhen(() ->
		{
			assertTankContains(helper, targetTank, net.minecraft.world.level.material.Fluids.WATER, 500, "tank facing the fluid regulator");
			helper.assertTrue(te.getFluidTank().isEmpty(), "fluid regulator should be drained");
			Ic2GameTestAssertions.assertNear(helper, energy.getEnergy(), 1000.0 - 50.0, "fluid regulator energy after 5 operations");
		});
	}

	private static void assertTankContains(GameTestHelper helper, Ic2FluidTank tank, Fluid fluid, int minAmountMb, String what)
	{
		Ic2FluidStack stack = tank.getFluidStack();
		helper.assertTrue(
			stack != null && !stack.isEmpty() && stack.getFluid() == fluid && stack.getAmountMb() >= minAmountMb,
			what + " should contain at least " + minAmountMb + " mB of " + fluid + ", has " + (stack == null ? "nothing" : stack.getAmountMb() + " mB of " + stack.getFluid())
		);
	}

	private static Ic2FluidTank getTankBlockTank(GameTestHelper helper, BlockPos pos)
	{
		BlockEntity be = helper.getBlockEntity(pos);
		if (!(be instanceof TileEntityTank tank))
		{
			throw new IllegalStateException("expected a fluid tank at " + pos + ", found " + be);
		}

		return tank.getComponent(Fluids.class).getAllTanks().iterator().next();
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
