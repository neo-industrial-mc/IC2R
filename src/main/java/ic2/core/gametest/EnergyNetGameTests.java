package ic2.core.gametest;

import ic2.api.energy.EnergyNet;
import ic2.core.block.machine.tileentity.TileEntityMacerator;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.wiring.tileentity.TileEntityElectricBatBox;
import ic2.core.block.wiring.tileentity.TileEntityElectricCESU;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFE;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFSU;
import ic2.core.block.wiring.tileentity.TileEntityTransformer;
import ic2.core.ref.Ic2Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class EnergyNetGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

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
	@GameTest(template = "gametest/empty3x9x3")
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
