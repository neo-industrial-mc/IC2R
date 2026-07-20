package me.halfcooler.ic2r.core.gametest;

import me.halfcooler.ic2r.core.block.comp.Energy;
import me.halfcooler.ic2r.core.block.machine.tileentity.TileEntityTesla;
import me.halfcooler.ic2r.core.ref.Ic2rBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r")
@PrefixGameTestTemplate(false)
public final class TeslaCoilGameTests
{
	private static final String EMPTY = "gametest/empty3x3x3";
	private static final BlockPos COIL_POS = new BlockPos(1, 1, 1);

	private TeslaCoilGameTests()
	{
	}

	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void teslaCoilShocksNearbyMobWhenPowered(GameTestHelper helper)
	{
		for (int x = 0; x < 3; x++)
		{
			for (int z = 0; z < 3; z++)
			{
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}

		helper.setBlock(COIL_POS, Ic2rBlocks.TESLA_COIL.get());
		helper.setBlock(new BlockPos(0, 1, 1), Blocks.REDSTONE_BLOCK);
		TileEntityTesla tesla = (TileEntityTesla) helper.getBlockEntity(COIL_POS);
		tesla.getComponent(Energy.class).addEnergy(8000.0);
		Pig pig = helper.spawn(EntityType.PIG, new BlockPos(2, 1, 1));

		helper.succeedWhen(() -> helper.assertTrue(!pig.isAlive(), "powered Tesla coil should shock nearby mob"));
	}
}
