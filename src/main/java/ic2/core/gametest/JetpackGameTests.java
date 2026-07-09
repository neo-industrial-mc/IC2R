package ic2.core.gametest;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.armor.ItemArmorJetpack;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.item.armor.jetpack.JetpackHandler;
import ic2.core.item.armor.jetpack.JetpackLogic;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import ic2.core.util.Keyboard;

import java.util.EnumSet;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class JetpackGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final double MAX_CHARGE = 30000.0;

	private static Player makeAirbornePlayer(GameTestHelper helper)
	{
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setOnGround(false);
		return player;
	}

	@GameTest(template = EMPTY)
	public static void electricJetpackLiftsPlayerAndDrainsCharge(GameTestHelper helper)
	{
		Player player = makeAirbornePlayer(helper);
		player.fallDistance = 3.0F;
		ItemStack stack = ElectricItemManager.getCharged(Ic2Items.JETPACK_ELECTRIC, Double.POSITIVE_INFINITY);
		IJetpack jetpack = (IJetpack) stack.getItem();

		helper.assertTrue(JetpackLogic.useJetpack(player, false, jetpack, stack), "charged jetpack should fire");

		// power 0.7 * 0.2 lift factor
		Ic2GameTestAssertions.assertNear(helper, player.getDeltaMovement().y, 0.14, "vertical motion after thrust");
		Ic2GameTestAssertions.assertNear(helper, player.fallDistance, 0.0, "fall distance must be reset");
		// non-hover flight consumes 2, drainEnergy adds a base cost of 6
		Ic2GameTestAssertions.assertNear(helper, ElectricItem.manager.getCharge(stack), MAX_CHARGE - 8.0, "charge after one thrust tick");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void jetpackWithoutChargeGivesNoThrust(GameTestHelper helper)
	{
		Player player = makeAirbornePlayer(helper);
		ItemStack stack = new ItemStack(Ic2Items.JETPACK_ELECTRIC);
		IJetpack jetpack = (IJetpack) stack.getItem();

		helper.assertFalse(JetpackLogic.useJetpack(player, false, jetpack, stack), "empty jetpack must not fire");
		Ic2GameTestAssertions.assertNear(helper, player.getDeltaMovement().y, 0.0, "no thrust without charge");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void electricJetpackPowerFadesBelowDropPercentage(GameTestHelper helper)
	{
		Player player = makeAirbornePlayer(helper);
		// 2.5% charge is half of the electric jetpack's 5% drop threshold
		ItemStack stack = ElectricItemManager.getCharged(Ic2Items.JETPACK_ELECTRIC, MAX_CHARGE * 0.025);
		IJetpack jetpack = (IJetpack) stack.getItem();

		helper.assertTrue(JetpackLogic.useJetpack(player, false, jetpack, stack), "low jetpack should still fire");
		Ic2GameTestAssertions.assertNear(helper, player.getDeltaMovement().y, 0.07, "thrust at half of drop percentage should be halved");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void hoverModeHoldsAltitude(GameTestHelper helper)
	{
		Player player = makeAirbornePlayer(helper);
		ItemStack stack = ElectricItemManager.getCharged(Ic2Items.JETPACK_ELECTRIC, Double.POSITIVE_INFINITY);
		IJetpack jetpack = (IJetpack) stack.getItem();

		helper.assertTrue(JetpackLogic.useJetpack(player, true, jetpack, stack), "hovering jetpack should fire");

		Ic2GameTestAssertions.assertNear(helper, player.getDeltaMovement().y, 0.0, "hover mode without keys must hold altitude");
		// hover mode consumes 1 instead of 2
		Ic2GameTestAssertions.assertNear(helper, ElectricItem.manager.getCharge(stack), MAX_CHARGE - 7.0, "charge after one hover tick");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void hoverModeDescendsWhileSneaking(GameTestHelper helper)
	{
		Player player = makeAirbornePlayer(helper);
		player.setDeltaMovement(0.0, -0.2, 0.0);
		player.setShiftKeyDown(true);
		ItemStack stack = ElectricItemManager.getCharged(Ic2Items.JETPACK_ELECTRIC, Double.POSITIVE_INFINITY);
		IJetpack jetpack = (IJetpack) stack.getItem();

		helper.assertTrue(JetpackLogic.useJetpack(player, true, jetpack, stack), "hovering jetpack should fire");

		// descent is limited to the electric jetpack's 0.1 hover multiplier
		Ic2GameTestAssertions.assertNear(helper, player.getDeltaMovement().y, -0.1, "sneaking in hover mode descends at the hover multiplier");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void jetpackGivesNoLiftAboveMaxFlightHeight(GameTestHelper helper)
	{
		Player player = makeAirbornePlayer(helper);
		ItemStack stack = ElectricItemManager.getCharged(Ic2Items.JETPACK_ELECTRIC, Double.POSITIVE_INFINITY);
		IJetpack jetpack = (IJetpack) stack.getItem();

		int maxFlightHeight = (int) (helper.getLevel().getMaxBuildHeight() / jetpack.getWorldHeightDivisor(stack));
		player.setPos(player.getX(), maxFlightHeight + 30, player.getZ());

		helper.assertTrue(JetpackLogic.useJetpack(player, false, jetpack, stack), "jetpack still fires above the flight ceiling");
		Ic2GameTestAssertions.assertNear(helper, player.getDeltaMovement().y, 0.0, "no lift above the flight ceiling");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void forwardKeyAddsHorizontalThrust(GameTestHelper helper)
	{
		Player player = makeAirbornePlayer(helper);
		player.setYRot(0.0F);
		ItemStack stack = ElectricItemManager.getCharged(Ic2Items.JETPACK_ELECTRIC, Double.POSITIVE_INFINITY);
		IJetpack jetpack = (IJetpack) stack.getItem();

		IC2.keyboard.processKeyUpdate(player, Keyboard.Key.toInt(EnumSet.of(Keyboard.Key.forward)));
		try
		{
			helper.assertTrue(JetpackLogic.useJetpack(player, false, jetpack, stack), "jetpack should fire");
		} finally
		{
			IC2.keyboard.removePlayerReferences(player);
		}

		// forwarder = 0.7 * 0.15 * 2, thrust = 0.4 * forwarder * 0.02, applied along +Z at yaw 0
		Ic2GameTestAssertions.assertNear(helper, player.getDeltaMovement().z, 0.00168, "forward thrust along the view direction");
		Ic2GameTestAssertions.assertNear(helper, player.getDeltaMovement().x, 0.0, "no sideways thrust at yaw 0");
		Ic2GameTestAssertions.assertNear(helper, player.getDeltaMovement().y, 0.14, "vertical thrust is unaffected by the forward key");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void biogasJetpackDrainsFuelOnlyOffGround(GameTestHelper helper)
	{
		ItemArmorJetpack item = (ItemArmorJetpack) Ic2Items.JETPACK;
		ItemStack stack = new ItemStack(item);
		item.fillTank(stack);
		Ic2GameTestAssertions.assertNear(helper, item.getCharge(stack), 30000.0, "filled tank");

		Player grounded = helper.makeMockPlayer(GameType.SURVIVAL);
		grounded.setOnGround(true);
		helper.assertTrue(JetpackLogic.useJetpack(grounded, false, item, stack), "biogas jetpack should fire");
		// full power 1.0 * 0.2 lift factor
		Ic2GameTestAssertions.assertNear(helper, grounded.getDeltaMovement().y, 0.2, "biogas jetpack thrust");
		Ic2GameTestAssertions.assertNear(helper, item.getCharge(stack), 30000.0, "no fuel used while on the ground");

		Player airborne = makeAirbornePlayer(helper);
		helper.assertTrue(JetpackLogic.useJetpack(airborne, false, item, stack), "biogas jetpack should fire");
		Ic2GameTestAssertions.assertNear(helper, item.getCharge(stack), 29998.0, "2 mB of biogas per airborne thrust tick");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void biogasJetpackNeverDrainsPartially(GameTestHelper helper)
	{
		Player player = makeAirbornePlayer(helper);
		ItemArmorJetpack item = (ItemArmorJetpack) Ic2Items.JETPACK;
		ItemStack stack = new ItemStack(item);
		item.fillMb(stack, Ic2FluidStack.create(Ic2Fluids.BIOGAS.still(), 1), false, null);
		Ic2GameTestAssertions.assertNear(helper, item.getCharge(stack), 1.0, "tank holding less than one tick of fuel");

		helper.assertTrue(JetpackLogic.useJetpack(player, false, item, stack), "jetpack fires as long as any fuel remains");
		Ic2GameTestAssertions.assertNear(helper, item.getCharge(stack), 1.0, "a drain larger than the remaining fuel must take nothing");

		helper.succeed();
	}

	@GameTest(template = EMPTY)
	public static void jetpackAttachmentMakesChestplateFly(GameTestHelper helper)
	{
		ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
		helper.assertFalse(JetpackHandler.hasJetpack(chestplate), "plain chestplate is not a jetpack");

		JetpackHandler.setJetpackAttached(chestplate, true);
		helper.assertTrue(JetpackHandler.hasJetpackAttached(chestplate), "attachment marker should be set");
		helper.assertTrue(JetpackHandler.hasJetpack(chestplate), "chestplate with attachment counts as a jetpack");

		ElectricItem.manager.charge(chestplate, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, false);
		Ic2GameTestAssertions.assertNear(helper, ElectricItem.manager.getCharge(chestplate), MAX_CHARGE, "attachment stores up to the electric jetpack capacity");

		Player player = makeAirbornePlayer(helper);
		IJetpack jetpack = JetpackHandler.getJetpack(chestplate);
		helper.assertTrue(JetpackLogic.useJetpack(player, false, jetpack, chestplate), "attached jetpack should fire");
		Ic2GameTestAssertions.assertNear(helper, player.getDeltaMovement().y, 0.14, "attached jetpack thrusts like the electric jetpack");
		Ic2GameTestAssertions.assertNear(helper, ElectricItem.manager.getCharge(chestplate), MAX_CHARGE - 8.0, "attached jetpack drains its stored charge");

		JetpackHandler.setJetpackAttached(chestplate, false);
		helper.assertFalse(JetpackHandler.hasJetpack(chestplate), "detached chestplate is no longer a jetpack");

		helper.succeed();
	}
}
