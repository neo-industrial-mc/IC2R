package ic2.core.gametest;

import ic2.core.entity.block.ITntEntity;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Entities;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2")
@PrefixGameTestTemplate(false)
public class ExplosiveGameTests
{
	private static final String EMPTY_LARGE = "gametest/empty7x7x7";

	/** ITNT block that gets caught in the blast, near the structure center. */
	private static final BlockPos TARGET_POS = new BlockPos(3, 4, 3);
	/** Where the triggering blast is centered, two blocks away from the target. */
	private static final BlockPos BLAST_POS = new BlockPos(3, 4, 1);

	// an ITNT caught in an IC2 explosion must chain-detonate (spawn a primed entity) instead of dropping as an item
	@GameTest(template = EMPTY_LARGE, batch = "ic2Explosive")
	public static void itntCaughtInIc2ExplosionChainDetonates(GameTestHelper helper)
	{
		helper.setBlock(TARGET_POS, Ic2Blocks.ITNT);

		// a real primed ITNT entity, but with the blast turned down so it stays inside the structure
		Vec3 spawn = Vec3.atCenterOf(helper.absolutePos(BLAST_POS));
		ITntEntity primer = new ITntEntity(helper.getLevel(), spawn.x, spawn.y, spawn.z);
		primer.explosivePower = 3.0F;
		primer.dropRate = 0.0F;
		primer.setFuse(1);
		helper.getLevel().addFreshEntity(primer);

		// the primer blows up on tick 1-2, the chained ITNT re-fuses for at least 7 ticks
		helper.runAtTickTime(5, () -> assertChainedAndDefuse(helper));
	}

	// the same must happen for vanilla explosions, which go through Block.onBlockExploded
	@GameTest(template = EMPTY_LARGE, batch = "ic2Explosive")
	public static void itntCaughtInVanillaExplosionChainDetonates(GameTestHelper helper)
	{
		helper.setBlock(TARGET_POS, Ic2Blocks.ITNT);

		Vec3 center = Vec3.atCenterOf(helper.absolutePos(new BlockPos(3, 4, 2)));
		helper.getLevel().explode(null, center.x, center.y, center.z, 2.0F, Level.ExplosionInteraction.TNT);

		helper.runAfterDelay(2, () -> assertChainedAndDefuse(helper));
	}

	/** Asserts the target ITNT turned into a short-fused primed entity without dropping, then defuses it before it can blow up the test arena. */
	private static void assertChainedAndDefuse(GameTestHelper helper)
	{
		helper.assertBlockPresent(Blocks.AIR, TARGET_POS);
		helper.assertItemEntityNotPresent(Ic2Blocks.ITNT.asItem(), TARGET_POS, 5.0);

		List<ITntEntity> primed = helper.getEntities(Ic2Entities.ITNT, TARGET_POS, 3.0);
		helper.assertTrue(primed.size() == 1, "the exploded ITNT should chain into exactly one primed ITNT entity, found " + primed.size());

		ITntEntity chained = primed.get(0);
		helper.assertTrue(chained.getFuse() < 30, "the chained ITNT should use a shortened fuse, has " + chained.getFuse());

		chained.discard();
		helper.succeed();
	}
}
