package ic2.core.gametest;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.item.tool.ContainerMeter;
import ic2.core.item.tool.HandHeldMeter;
import ic2.core.item.tool.ItemToolMeter;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class MeterGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";

	private static final BlockPos TARGET_POS = new BlockPos(1, 1, 1);

	// the meter finds the clicked energy net tile and provides its readout container
	// (mock players can't receive the menu-open payload, so the container is created directly)
	@GameTest(template = EMPTY)
	public static void meterReadsEnergyTile(GameTestHelper helper)
	{
		helper.setBlock(TARGET_POS, Ic2Blocks.GENERATOR);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		ItemStack stack = new ItemStack(Ic2Items.METER);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		// give the freshly placed block entity a tick to join the energy net
		helper.runAfterDelay(2, () ->
		{
			IEnergyTile tile = EnergyNet.instance.getTile(helper.getLevel(), helper.absolutePos(TARGET_POS));
			helper.assertTrue(tile instanceof IEnergySource, "the generator should be an energy net source");

			HandHeldMeter inventory = (HandHeldMeter) ((ItemToolMeter) Ic2Items.METER).getInventory(player, InteractionHand.MAIN_HAND, stack);
			AbstractContainerMenu menu = inventory.createServerScreenHandler(1, player);
			helper.assertTrue(menu instanceof ContainerMeter, "the meter should provide its container");
			((ContainerMeter) menu).setUut(tile);
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY)
	public static void meterDoesNotOpenOnPlainBlock(GameTestHelper helper)
	{
		helper.setBlock(TARGET_POS, Blocks.STONE);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		ItemStack stack = new ItemStack(Ic2Items.METER);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		stack.getItem().onItemUseFirst(stack, Ic2GameTestUtil.useOn(helper, player, TARGET_POS, Direction.NORTH));

		helper.assertFalse(player.containerMenu instanceof ContainerMeter, "meter must not open on a plain block");
		helper.succeed();
	}
}
