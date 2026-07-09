package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.core.entity.LaserBulletEntity;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.tool.ItemToolMiningLaser;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import ic2.core.util.Vector3;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class MiningLaserGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final double MAX_CHARGE = 300000.0;

	private static ItemToolMiningLaser laser()
	{
		return (ItemToolMiningLaser) Ic2Items.MINING_LASER;
	}

	@GameTest(template = EMPTY)
	public static void laserModeSwitchCyclesAllModes(GameTestHelper helper)
	{
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		ItemStack stack = ElectricItemManager.getCharged(Ic2Items.MINING_LASER, Double.POSITIVE_INFINITY);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		Ic2GameTestUtil.pressModeSwitchKey(player);
		try
		{
			for (int use = 1; use <= 8; use++)
			{
				laser().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
				helper.assertValueEqual(StackUtil.getOrCreateNbtData(stack).getInt("laser_setting"), use % 8, "mode after " + use + " switches");
			}
		} finally
		{
			Ic2GameTestUtil.releaseModeSwitchKey(player);
		}

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void laserMiningModeShot(GameTestHelper helper)
	{
		assertShot(helper, 0, 1250.0, 1);
	}

	@GameTest(template = EMPTY)
	public static void laserLowFocusModeShot(GameTestHelper helper)
	{
		assertShot(helper, 1, 100.0, 1);
	}

	@GameTest(template = EMPTY)
	public static void laserLongRangeModeShot(GameTestHelper helper)
	{
		assertShot(helper, 2, 5000.0, 1);
	}

	@GameTest(template = EMPTY)
	public static void laserSuperHeatModeShot(GameTestHelper helper)
	{
		assertShot(helper, 4, 2500.0, 1);
	}

	// scatter mode fires a 5x5 fan of beams
	@GameTest(template = EMPTY)
	public static void laserScatterModeShot(GameTestHelper helper)
	{
		assertShot(helper, 5, 10000.0, 25);
	}

	@GameTest(template = EMPTY)
	public static void laserExplosiveModeShot(GameTestHelper helper)
	{
		assertShot(helper, 6, 5000.0, 1);
	}

	/**
	 * Fires the laser straight up (so the beams never reach neighbouring test structures),
	 * then checks the energy cost and the number of spawned beams before discarding them.
	 */
	private static void assertShot(GameTestHelper helper, int mode, double cost, int expectedBeams)
	{
		ServerPlayer player = makeShooter(helper, -90.0F);
		ItemStack stack = prepareLaser(player, mode);

		laser().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

		helper.assertValueEqual(ElectricItem.manager.getCharge(stack), MAX_CHARGE - cost, "charge after firing mode " + mode);
		helper.assertValueEqual(discardBeams(helper, player), expectedBeams, "beams fired in mode " + mode);
		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void laserWithoutChargeDoesNotFire(GameTestHelper helper)
	{
		ServerPlayer player = makeShooter(helper, -90.0F);
		ItemStack stack = new ItemStack(Ic2Items.MINING_LASER);
		StackUtil.getOrCreateNbtData(stack).putInt("laser_setting", 0);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		laser().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

		helper.assertValueEqual(discardBeams(helper, player), 0, "beams fired without charge");
		helper.succeed();
	}

	// horizontal mode fires a single levelled beam when used on a block
	@GameTest(template = EMPTY)
	public static void laserHorizontalModeShot(GameTestHelper helper)
	{
		ServerPlayer player = makeShooter(helper, 0.0F);
		ItemStack stack = prepareLaser(player, 3);

		helper.setBlock(new BlockPos(1, 1, 2), Blocks.DIRT);
		laser().onItemUseFirst(stack, Ic2GameTestUtil.useOn(helper, player, new BlockPos(1, 1, 2), Direction.NORTH));

		helper.assertValueEqual(ElectricItem.manager.getCharge(stack), MAX_CHARGE - 3000.0, "charge after horizontal shot");
		helper.assertValueEqual(discardBeams(helper, player), 1, "beams fired in horizontal mode");
		helper.succeed();
	}

	// horizontal mode refuses to fire when aiming too steeply
	@GameTest(template = EMPTY)
	public static void laserHorizontalModeRejectsSteepAim(GameTestHelper helper)
	{
		ServerPlayer player = makeShooter(helper, -90.0F);
		ItemStack stack = prepareLaser(player, 3);

		helper.setBlock(new BlockPos(1, 1, 2), Blocks.DIRT);
		laser().onItemUseFirst(stack, Ic2GameTestUtil.useOn(helper, player, new BlockPos(1, 1, 2), Direction.NORTH));

		helper.assertValueEqual(ElectricItem.manager.getCharge(stack), MAX_CHARGE, "charge after rejected steep shot");
		helper.assertValueEqual(discardBeams(helper, player), 0, "beams fired with steep aim");
		helper.succeed();
	}

	// 3x3 mode fires a 3x3 wall of nine beams
	@GameTest(template = EMPTY)
	public static void laser3x3ModeShotHorizontal(GameTestHelper helper)
	{
		ServerPlayer player = makeShooter(helper, 0.0F);
		ItemStack stack = prepareLaser(player, 7);

		helper.setBlock(new BlockPos(1, 1, 2), Blocks.DIRT);
		laser().onItemUseFirst(stack, Ic2GameTestUtil.useOn(helper, player, new BlockPos(1, 1, 2), Direction.NORTH));

		helper.assertValueEqual(ElectricItem.manager.getCharge(stack), MAX_CHARGE - 3000.0, "charge after 3x3 shot");
		helper.assertValueEqual(discardBeams(helper, player), 9, "beams fired in 3x3 mode");
		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void laser3x3ModeShotVertical(GameTestHelper helper)
	{
		ServerPlayer player = makeShooter(helper, 90.0F);
		ItemStack stack = prepareLaser(player, 7);

		// aim at the structure's center column so all nine beams spawn within the test area
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.DIRT);
		laser().onItemUseFirst(stack, Ic2GameTestUtil.useOn(helper, player, new BlockPos(1, 1, 1), Direction.UP));

		helper.assertValueEqual(ElectricItem.manager.getCharge(stack), MAX_CHARGE - 3000.0, "charge after vertical 3x3 shot");
		helper.assertValueEqual(discardBeams(helper, player), 9, "beams fired in vertical 3x3 mode");
		helper.succeed();
	}

	// an actual beam in flight destroys the block it hits
	@GameTest(template = EMPTY)
	public static void laserBeamBreaksBlock(GameTestHelper helper)
	{
		ServerPlayer player = makeShooter(helper, 0.0F);
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.DIRT);
		helper.setBlock(new BlockPos(1, 1, 2), Blocks.OBSIDIAN);

		// low focus parameters: short range, single block break
		laser().shootLaser(helper.getLevel(), new Vector3(helper.absoluteVec(new Vec3(1.5, 1.5, 0.2))), new Vector3(0.0, 0.0, 1.0), player, 4.0F, 5.0F, 1, false, false);

		helper.succeedWhen(() ->
		{
			helper.assertBlockPresent(Blocks.AIR, new BlockPos(1, 1, 1));
			helper.assertBlockPresent(Blocks.OBSIDIAN, new BlockPos(1, 1, 2));
		});
	}

	// the super heat beam smelts what it breaks, placing the smelted block
	@GameTest(template = EMPTY)
	public static void laserSuperHeatBeamSmeltsSand(GameTestHelper helper)
	{
		ServerPlayer player = makeShooter(helper, 0.0F);
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.SAND);
		helper.setBlock(new BlockPos(1, 1, 2), Blocks.OBSIDIAN);

		laser().shootLaser(helper.getLevel(), new Vector3(helper.absoluteVec(new Vec3(1.5, 1.5, 0.2))), new Vector3(0.0, 0.0, 1.0), player, 8.0F, 8.0F, 1, false, true);

		helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.GLASS, new BlockPos(1, 1, 1)));
	}

	private static ServerPlayer makeShooter(GameTestHelper helper, float xRot)
	{
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		Vec3 pos = helper.absoluteVec(new Vec3(1.5, 1.0, 0.5));
		player.moveTo(pos.x, pos.y, pos.z, 0.0F, xRot);
		return player;
	}

	private static ItemStack prepareLaser(Player player, int mode)
	{
		ItemStack stack = ElectricItemManager.getCharged(Ic2Items.MINING_LASER, Double.POSITIVE_INFINITY);
		StackUtil.getOrCreateNbtData(stack).putInt("laser_setting", mode);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		return stack;
	}

	/** Counts the laser beams around the shooter and removes them before they can go anywhere. */
	private static int discardBeams(GameTestHelper helper, Player player)
	{
		List<LaserBulletEntity> beams = helper.getLevel().getEntitiesOfClass(LaserBulletEntity.class, new AABB(player.blockPosition()).inflate(6.0));
		beams.forEach(LaserBulletEntity::discard);
		return beams.size();
	}
}
