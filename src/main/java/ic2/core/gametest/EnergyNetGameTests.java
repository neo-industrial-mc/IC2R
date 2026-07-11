package ic2.core.gametest;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.info.ILocatable;
import ic2.core.block.machine.tileentity.TileEntityMacerator;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.wiring.AbstractDetectorCableBlock;
import ic2.core.block.wiring.AbstractSplitterCableBlock;
import ic2.core.block.wiring.tileentity.TileEntityElectricBatBox;
import ic2.core.block.wiring.tileentity.TileEntityElectricCESU;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFE;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFSU;
import ic2.core.block.wiring.tileentity.TileEntityLuminator;
import ic2.core.block.wiring.tileentity.TileEntityTransformer;
import ic2.core.ref.Ic2Blocks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class EnergyNetGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";
	private static final String TALL = "gametest/empty3x9x3";

	// classic IC2 voltage tiers: LV 32, MV 128, HV 512, EV 2048
	@GameTest(template = EMPTY)
	public static void powerTierTableMatchesClassicValues(GameTestHelper helper)
	{
		Ic2GameTestAssertions.assertNear(helper, EnergyNet.instance.getPowerFromTier(1), 32.0, "tier 1 (LV) power");
		Ic2GameTestAssertions.assertNear(helper, EnergyNet.instance.getPowerFromTier(2), 128.0, "tier 2 (MV) power");
		Ic2GameTestAssertions.assertNear(helper, EnergyNet.instance.getPowerFromTier(3), 512.0, "tier 3 (HV) power");
		Ic2GameTestAssertions.assertNear(helper, EnergyNet.instance.getPowerFromTier(4), 2048.0, "tier 4 (EV) power");
		helper.assertValueEqual(EnergyNet.instance.getTierFromPower(32.0), 1, "tier of a 32 EU packet");
		helper.assertValueEqual(EnergyNet.instance.getTierFromPower(512.0), 3, "tier of a 512 EU packet");
		helper.succeed();
	}

	// a batbox (LV source) fills an adjacent LV machine at 32 EU/packet with no loss
	@GameTest(template = EMPTY)
	public static void batboxPowersAdjacentLvMachine(GameTestHelper helper)
	{
		// freshly placed storage blocks emit from their bottom face
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.BATBOX);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.MACERATOR);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricBatBox.class);
		TileEntityMacerator macerator = getTe(helper, new BlockPos(1, 1, 1), TileEntityMacerator.class);

		// 18 full 32 EU packets, ending exactly at the macerator's 600 EU buffer minus 24
		batbox.energy.addEnergy(576.0);

		helper.succeedWhen(() ->
		{
			Ic2GameTestAssertions.assertNear(helper, macerator.getEnergy(), 576.0, "macerator buffer");
			Ic2GameTestAssertions.assertNear(helper, batbox.energy.getEnergy(), 0.0, "batbox buffer");
		});
	}

	// a 512 EU (HV) packet into a tier 1 sink must blow the machine up
	@GameTest(template = EMPTY, batch = "ic2EnetExplosion")
	public static void lvMachineExplodesOnHvPacket(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.MFE);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.MACERATOR);
		TileEntityElectricMFE mfe = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricMFE.class);
		mfe.energy.addEnergy(10000.0);

		helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.AIR, new BlockPos(1, 1, 1)));
	}

	// CESU (MV, 128 EU) -> LV transformer -> LV machine: stepped-down packets arrive without an explosion
	@GameTest(template = EMPTY)
	public static void lvTransformerStepsDownMvForLvMachine(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.CESU);
		// step-down mode accepts the higher tier on the facing side, so face the CESU above
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.LV_TRANSFORMER.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.UP));
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.MACERATOR);
		TileEntityElectricCESU cesu = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricCESU.class);
		TileEntityMacerator macerator = getTe(helper, new BlockPos(1, 0, 1), TileEntityMacerator.class);

		cesu.energy.addEnergy(1024.0);

		helper.runAtTickTime(80, () ->
		{
			helper.assertBlockPresent(Ic2Blocks.MACERATOR, new BlockPos(1, 0, 1));
			helper.assertTrue(macerator.getEnergy() >= 512.0, "macerator should have received stepped-down energy, has " + macerator.getEnergy());
			helper.succeed();
		});
	}

	// path loss is floored per packet (roundEnetLoss), so a short copper run is lossless
	@GameTest(template = EMPTY)
	public static void shortCopperRunIsLossFree(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.BATBOX);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.COPPER_CABLE);
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.MACERATOR);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricBatBox.class);
		TileEntityMacerator macerator = getTe(helper, new BlockPos(1, 0, 1), TileEntityMacerator.class);

		batbox.energy.addEnergy(320.0);

		helper.succeedWhen(() ->
		{
			Ic2GameTestAssertions.assertNear(helper, macerator.getEnergy(), 320.0, "macerator buffer after short run");
			Ic2GameTestAssertions.assertNear(helper, batbox.energy.getEnergy(), 0.0, "batbox buffer");
		});
	}

	// 6 copper cables accumulate 1.202 EU path loss, floored to a whole 1 EU per packet
	@GameTest(template = TALL)
	public static void longCopperRunLosesWholeEuPerPacket(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 7, 1), Ic2Blocks.BATBOX);
		for (int y = 1; y <= 6; y++)
		{
			helper.setBlock(new BlockPos(1, y, 1), Ic2Blocks.COPPER_CABLE);
		}
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.MACERATOR);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 7, 1), TileEntityElectricBatBox.class);
		TileEntityMacerator macerator = getTe(helper, new BlockPos(1, 0, 1), TileEntityMacerator.class);

		// 10 packets of 32 EU, each arriving as 31 EU
		batbox.energy.addEnergy(320.0);

		helper.succeedWhen(() ->
		{
			Ic2GameTestAssertions.assertNear(helper, macerator.getEnergy(), 310.0, "macerator buffer after long run");
			Ic2GameTestAssertions.assertNear(helper, batbox.energy.getEnergy(), 0.0, "batbox buffer");
		});
	}

	// tin can only carry 32 EU; an MV packet melts the cable but spares the tier 2 sink
	@GameTest(template = EMPTY)
	public static void tinCableMeltsAboveItsCapacity(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.CESU);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.TIN_CABLE);
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.CESU);
		TileEntityElectricCESU source = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricCESU.class);
		source.energy.addEnergy(1024.0);

		helper.succeedWhen(() ->
		{
			helper.assertBlockPresent(Blocks.AIR, new BlockPos(1, 1, 1));
			helper.assertBlockPresent(Ic2Blocks.CESU, new BlockPos(1, 0, 1));
		});
	}

	// a redstone signal flips a transformer into step-up mode: LV in, MV out of the facing side
	@GameTest(template = EMPTY)
	public static void redstoneFlipsTransformerToStepUp(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.BATBOX);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.LV_TRANSFORMER.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.DOWN));
		helper.setBlock(new BlockPos(0, 1, 1), Blocks.REDSTONE_BLOCK);
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.CESU);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricBatBox.class);
		TileEntityTransformer transformer = getTe(helper, new BlockPos(1, 1, 1), TileEntityTransformer.class);
		TileEntityElectricCESU cesu = getTe(helper, new BlockPos(1, 0, 1), TileEntityElectricCESU.class);

		batbox.energy.addEnergy(512.0);

		helper.succeedWhen(() ->
		{
			helper.assertTrue(transformer.getActive(), "transformer should be in step-up mode next to redstone");
			helper.assertTrue(cesu.energy.getEnergy() >= 128.0, "CESU should have received stepped-up energy, has " + cesu.energy.getEnergy());
		});
	}

	// MFSU (EV, 2048 EU) -> HV transformer -> MFE (tier 3): the top tiers step down cleanly
	@GameTest(template = EMPTY)
	public static void hvTransformerStepsDownEvForMfe(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.MFSU);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.HV_TRANSFORMER.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.UP));
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.MFE);
		TileEntityElectricMFSU mfsu = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricMFSU.class);
		TileEntityElectricMFE mfe = getTe(helper, new BlockPos(1, 0, 1), TileEntityElectricMFE.class);

		mfsu.energy.addEnergy(4096.0);

		helper.runAtTickTime(80, () ->
		{
			helper.assertBlockPresent(Ic2Blocks.MFE, new BlockPos(1, 0, 1));
			helper.assertTrue(mfe.energy.getEnergy() >= 512.0, "MFE should have received stepped-down energy, has " + mfe.energy.getEnergy());
			helper.succeed();
		});
	}

	// bare copper absorbs 32 EU, so LV current is safe but an MV packet shocks entities next to the cable
	@GameTest(template = EMPTY)
	public static void bareCableShocksNearbyEntitiesOnMvCurrent(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.CESU);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.COPPER_CABLE);
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.CESU);
		TileEntityElectricCESU source = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricCESU.class);
		Pig pig = helper.spawn(EntityType.PIG, new BlockPos(0, 1, 1));

		source.energy.addEnergy(1280.0);

		helper.succeedWhen(() -> helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "pig next to a live bare cable should take shock damage"));
	}

	// MFE (HV, 512 EU) -> MV transformer -> CESU (tier 2): stepped-down packets arrive without an explosion
	@GameTest(template = EMPTY)
	public static void mvTransformerStepsDownHvForCesu(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.MFE);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.MV_TRANSFORMER.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.UP));
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.CESU);
		TileEntityElectricMFE mfe = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricMFE.class);
		TileEntityElectricCESU cesu = getTe(helper, new BlockPos(1, 0, 1), TileEntityElectricCESU.class);

		mfe.energy.addEnergy(2048.0);

		helper.runAtTickTime(80, () ->
		{
			helper.assertBlockPresent(Ic2Blocks.CESU, new BlockPos(1, 0, 1));
			helper.assertTrue(cesu.energy.getEnergy() >= 1024.0, "CESU should have received stepped-down energy, has " + cesu.energy.getEnergy());
			helper.succeed();
		});
	}

	// MFSU -> EV transformer (redstone, step-up to 8192 EU) -> EV transformer (step-down) -> MFSU:
	// the 8192 EU packets pass between the transformers and arrive back at EV without an explosion
	@GameTest(template = TALL)
	public static void evTransformerStepsUpAndBackDown(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 7, 1), Ic2Blocks.MFSU);
		helper.setBlock(new BlockPos(1, 6, 1), Ic2Blocks.EV_TRANSFORMER.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.DOWN));
		helper.setBlock(new BlockPos(0, 6, 1), Blocks.REDSTONE_BLOCK);
		helper.setBlock(new BlockPos(1, 5, 1), Ic2Blocks.EV_TRANSFORMER.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.UP));
		helper.setBlock(new BlockPos(1, 4, 1), Ic2Blocks.MFSU);
		TileEntityElectricMFSU source = getTe(helper, new BlockPos(1, 7, 1), TileEntityElectricMFSU.class);
		TileEntityTransformer stepUp = getTe(helper, new BlockPos(1, 6, 1), TileEntityTransformer.class);
		TileEntityElectricMFSU sink = getTe(helper, new BlockPos(1, 4, 1), TileEntityElectricMFSU.class);

		source.energy.addEnergy(16384.0);

		helper.runAtTickTime(80, () ->
		{
			helper.assertTrue(stepUp.getActive(), "EV transformer next to redstone should be in step-up mode");
			helper.assertBlockPresent(Ic2Blocks.EV_TRANSFORMER, new BlockPos(1, 5, 1));
			helper.assertBlockPresent(Ic2Blocks.MFSU, new BlockPos(1, 4, 1));
			helper.assertTrue(sink.energy.getEnergy() >= 8192.0, "MFSU should have received twice-transformed energy, has " + sink.energy.getEnergy());
			helper.succeed();
		});
	}

	// gold carries HV; bare, insulated and double insulated segments in series lose 3 * 0.4 EU, floored to 1 EU per packet
	@GameTest(template = TALL)
	public static void goldCablesCarryHvThroughAllInsulationLevels(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 5, 1), Ic2Blocks.MFE);
		helper.setBlock(new BlockPos(1, 4, 1), Ic2Blocks.GOLD_CABLE);
		helper.setBlock(new BlockPos(1, 3, 1), Ic2Blocks.INSULATED_GOLD_CABLE);
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.DOUBLE_INSULATED_GOLD_CABLE);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.MFE);
		TileEntityElectricMFE source = getTe(helper, new BlockPos(1, 5, 1), TileEntityElectricMFE.class);
		TileEntityElectricMFE sink = getTe(helper, new BlockPos(1, 1, 1), TileEntityElectricMFE.class);

		// 4 packets of 512 EU, each arriving as 511 EU
		source.energy.addEnergy(2048.0);

		helper.succeedWhen(() ->
		{
			Ic2GameTestAssertions.assertNear(helper, sink.energy.getEnergy(), 2044.0, "MFE buffer after the gold run");
			Ic2GameTestAssertions.assertNear(helper, source.energy.getEnergy(), 0.0, "source MFE buffer");
		});
	}

	// iron carries EV; all four insulation levels in series lose 4 * 0.8 EU, floored to 3 EU per packet
	@GameTest(template = TALL)
	public static void ironCablesCarryEvThroughAllInsulationLevels(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 7, 1), Ic2Blocks.MFSU);
		helper.setBlock(new BlockPos(1, 6, 1), Ic2Blocks.IRON_CABLE);
		helper.setBlock(new BlockPos(1, 5, 1), Ic2Blocks.INSULATED_IRON_CABLE);
		helper.setBlock(new BlockPos(1, 4, 1), Ic2Blocks.DOUBLE_INSULATED_IRON_CABLE);
		helper.setBlock(new BlockPos(1, 3, 1), Ic2Blocks.TRIPLE_INSULATED_IRON_CABLE);
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.MFSU);
		TileEntityElectricMFSU source = getTe(helper, new BlockPos(1, 7, 1), TileEntityElectricMFSU.class);
		TileEntityElectricMFSU sink = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricMFSU.class);

		// 4 packets of 2048 EU, each arriving as 2045 EU
		source.energy.addEnergy(8192.0);

		helper.succeedWhen(() ->
		{
			Ic2GameTestAssertions.assertNear(helper, sink.energy.getEnergy(), 8180.0, "MFSU buffer after the iron run");
			Ic2GameTestAssertions.assertNear(helper, source.energy.getEnergy(), 0.0, "source MFSU buffer");
		});
	}

	// glass fibre's 0.025 EU loss floors to nothing on a short run
	@GameTest(template = EMPTY)
	public static void glassFibreCableCarriesHvLosslessly(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.MFE);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.GLASS_FIBRE_CABLE);
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.MFE);
		TileEntityElectricMFE source = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricMFE.class);
		TileEntityElectricMFE sink = getTe(helper, new BlockPos(1, 0, 1), TileEntityElectricMFE.class);

		source.energy.addEnergy(2048.0);

		helper.succeedWhen(() ->
		{
			Ic2GameTestAssertions.assertNear(helper, sink.energy.getEnergy(), 2048.0, "MFE buffer after the glass fibre run");
			Ic2GameTestAssertions.assertNear(helper, source.energy.getEnergy(), 0.0, "source MFE buffer");
		});
	}

	// insulated tin still only carries LV, and a short run is lossless
	@GameTest(template = EMPTY)
	public static void insulatedTinCableCarriesLv(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.BATBOX);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.INSULATED_TIN_CABLE);
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.MACERATOR);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricBatBox.class);
		TileEntityMacerator macerator = getTe(helper, new BlockPos(1, 0, 1), TileEntityMacerator.class);

		batbox.energy.addEnergy(320.0);

		helper.succeedWhen(() ->
		{
			Ic2GameTestAssertions.assertNear(helper, macerator.getEnergy(), 320.0, "macerator buffer after the insulated tin run");
			Ic2GameTestAssertions.assertNear(helper, batbox.energy.getEnergy(), 0.0, "batbox buffer");
		});
	}

	// one layer of insulation absorbs 128 EU, so MV current is safe for entities next to the cable
	@GameTest(template = EMPTY)
	public static void insulatedCopperCableDoesNotShockOnMvCurrent(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.CESU);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.INSULATED_COPPER_CABLE);
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.CESU);
		TileEntityElectricCESU source = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricCESU.class);
		TileEntityElectricCESU sink = getTe(helper, new BlockPos(1, 0, 1), TileEntityElectricCESU.class);
		Pig pig = helper.spawn(EntityType.PIG, new BlockPos(0, 1, 1));

		source.energy.addEnergy(1280.0);

		helper.runAtTickTime(80, () ->
		{
			helper.assertBlockPresent(Ic2Blocks.INSULATED_COPPER_CABLE, new BlockPos(1, 1, 1));
			Ic2GameTestAssertions.assertNear(helper, pig.getHealth(), pig.getMaxHealth(), "pig next to an insulated cable must not take shock damage");
			Ic2GameTestAssertions.assertNear(helper, sink.energy.getEnergy(), 1280.0, "CESU buffer after the insulated copper run");
			helper.succeed();
		});
	}

	// a detector cable emits a full redstone signal while current passes through it
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void detectorCableEmitsRedstoneWhileCurrentFlows(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.BATBOX);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.DETECTOR_CABLE);
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.CESU);
		helper.setBlock(new BlockPos(0, 1, 1), Blocks.REDSTONE_LAMP);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricBatBox.class);

		// enough energy to keep the current flowing over the detector's whole 32 tick poll interval
		batbox.energy.addEnergy(40000.0);

		helper.succeedWhen(() ->
		{
			helper.assertBlockProperty(new BlockPos(1, 1, 1), AbstractDetectorCableBlock.active, true);
			helper.assertBlockProperty(new BlockPos(0, 1, 1), RedstoneLampBlock.LIT, true);
		});
	}

	// a splitter cable without a redstone signal conducts like any other cable
	@GameTest(template = EMPTY)
	public static void splitterCableConductsWithoutRedstone(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.BATBOX);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.SPLITTER_CABLE);
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.MACERATOR);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricBatBox.class);
		TileEntityMacerator macerator = getTe(helper, new BlockPos(1, 0, 1), TileEntityMacerator.class);

		batbox.energy.addEnergy(320.0);

		helper.succeedWhen(() ->
		{
			Ic2GameTestAssertions.assertNear(helper, macerator.getEnergy(), 320.0, "macerator buffer behind an unpowered splitter");
			Ic2GameTestAssertions.assertNear(helper, batbox.energy.getEnergy(), 0.0, "batbox buffer");
		});
	}

	// a redstone signal cuts the current through a splitter cable
	@GameTest(template = EMPTY)
	public static void redstoneCutsSplitterCableCurrent(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.BATBOX);
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.SPLITTER_CABLE);
		helper.setBlock(new BlockPos(1, 0, 1), Ic2Blocks.MACERATOR);
		// placed after the splitter so the neighbor update flips it to blocking
		helper.setBlock(new BlockPos(0, 1, 1), Blocks.REDSTONE_BLOCK);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricBatBox.class);
		TileEntityMacerator macerator = getTe(helper, new BlockPos(1, 0, 1), TileEntityMacerator.class);

		batbox.energy.addEnergy(320.0);

		helper.runAtTickTime(80, () ->
		{
			helper.assertBlockProperty(new BlockPos(1, 1, 1), AbstractSplitterCableBlock.active, true);
			Ic2GameTestAssertions.assertNear(helper, macerator.getEnergy(), 0.0, "macerator must not receive energy through a powered splitter");
			Ic2GameTestAssertions.assertNear(helper, batbox.energy.getEnergy(), 320.0, "batbox must keep its energy");
			helper.succeed();
		});
	}

	// the luminator lights on redstone + EU and goes dark once the redstone signal is gone
	// (the ACTIVE block state and thus the light level only sync on the client, so assert the tile state)
	@GameTest(template = EMPTY)
	public static void luminatorLightsOnRedstoneAndCurrent(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.BATBOX);
		// mounted below the batbox, drawing EU through its mounting face
		helper.setBlock(new BlockPos(1, 1, 1), Ic2Blocks.LUMINATOR_FLAT.defaultBlockState().setValue(Ic2TileEntityBlock.anyFacingProperty, Direction.DOWN));
		helper.setBlock(new BlockPos(0, 1, 1), Blocks.REDSTONE_BLOCK);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricBatBox.class);
		TileEntityLuminator luminator = getTe(helper, new BlockPos(1, 1, 1), TileEntityLuminator.class);

		batbox.energy.addEnergy(400.0);

		helper.runAtTickTime(40, () ->
		{
			helper.assertTrue(luminator.getActive(), "luminator with redstone and EU should be lit");
			helper.assertTrue(batbox.energy.getEnergy() < 400.0, "luminator should draw EU from the batbox");
		});
		helper.runAtTickTime(45, () -> helper.setBlock(new BlockPos(0, 1, 1), Blocks.AIR));
		helper.runAtTickTime(80, () ->
		{
			helper.assertFalse(luminator.getActive(), "luminator without redstone must go dark");
			helper.succeed();
		});
	}

	// each grid is calculated exactly once per tick and only on the server thread; the
	// old async re-run injected a second round of energy off-thread (upstream f516c24f)
	@GameTest(template = EMPTY)
	public static void energyTransferRunsOncePerTickOnServerThread(GameTestHelper helper)
	{
		helper.setBlock(new BlockPos(1, 2, 1), Ic2Blocks.BATBOX);
		TileEntityElectricBatBox batbox = getTe(helper, new BlockPos(1, 2, 1), TileEntityElectricBatBox.class);
		// a bare enet sink below the batbox, demanding 1 EU per calculation so a
		// duplicate calculation leaves the source with energy for a second packet
		RecordingSink sink = new RecordingSink(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)));
		EnergyNet.instance.addLocatableTile(sink);

		batbox.energy.addEnergy(400.0);

		// the tick right after a topology change runs a transfer calc at both tick start
		// and tick end, so only record once the freshly built grid has settled
		helper.runAtTickTime(20, () -> sink.armed = true);
		helper.runAtTickTime(60, () ->
		{
			try
			{
				helper.assertFalse(sink.offThreadInjection.get(), "injectEnergy must only be called on the server thread");
				helper.assertTrue(sink.injectionsByTick.size() >= 10, "sink should have been powered for several ticks, got " + sink.injectionsByTick.size());

				for (Map.Entry<Long, Double> entry : sink.injectionsByTick.entrySet())
				{
					Ic2GameTestAssertions.assertNear(helper, entry.getValue(), 1.0, "energy injected during tick " + entry.getKey());
				}
			} finally
			{
				EnergyNet.instance.removeTile(sink);
			}

			helper.succeed();
		});
	}

	private static final class RecordingSink implements ILocatable, IEnergySink
	{
		final Map<Long, Double> injectionsByTick = new ConcurrentHashMap<>();
		final AtomicBoolean offThreadInjection = new AtomicBoolean();
		volatile boolean armed;
		private final ServerLevel world;
		private final BlockPos pos;

		RecordingSink(ServerLevel world, BlockPos pos)
		{
			this.world = world;
			this.pos = pos;
		}

		@Override
		public BlockPos getPosition()
		{
			return this.pos;
		}

		@Override
		public Level getWorldObj()
		{
			return this.world;
		}

		@Override
		public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction dir)
		{
			return true;
		}

		@Override
		public double getDemandedEnergy()
		{
			return 1.0;
		}

		@Override
		public int getSinkTier()
		{
			return 1;
		}

		@Override
		public double injectEnergy(Direction directionFrom, double amount, double voltage)
		{
			if (!this.world.getServer().isSameThread())
			{
				this.offThreadInjection.set(true);
			}

			if (this.armed)
			{
				this.injectionsByTick.merge(this.world.getGameTime(), amount, Double::sum);
			}

			return 0.0;
		}
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
